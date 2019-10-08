package com.kit.filetransmitter.view;

import com.kit.filetransmitter.component.MqttManager;
import com.kit.filetransmitter.entity.StorePath;
import com.kit.filetransmitter.util.Topics;
import com.kit.filetransmitter.util.ValidateUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * @author Kit
 * @date: 2019/10/3 22:42
 */
public class ChatView {

    private Stage stage;
    private Scene scene;
    private TextArea inputArea, recvArea;
    private Button openButton, selButton, sendButton;
    private StorePath storePath;
    private MqttManager mqttManager;

    public ChatView(Stage primaryStage) {
        this.stage = primaryStage;
        this.mqttManager = MqttManager.getInstance(this);
        init();
    }

    private void init() {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(StorePath.LOCATION))) {
            storePath = (StorePath) inputStream.readObject();
            if (!storePath.isValid()) {
                throw new ClassNotFoundException();
            }
        } catch (IOException | ClassNotFoundException e) {
            storePath = new StorePath();
        } finally {
            storePath.createPathIfNotExist();
        }

        MenuItem settingMenuItem = new MenuItem("设置");
        settingMenuItem.setOnAction(e -> new SettingView(storePath));
        Menu settingMenu = new Menu("设置", null, settingMenuItem);
        MenuBar menuBar = new MenuBar(settingMenu);

        recvArea = new TextArea();
        recvArea.setWrapText(true);
        recvArea.setEditable(false);
        VBox.setVgrow(recvArea, Priority.ALWAYS);

        openButton = new Button("打开文件位置");
        openButton.setOnAction(e -> openButtonAction());
        selButton = new Button("选择文件");
        selButton.setOnAction(e -> selButtonAction());
        sendButton = new Button("发送");
        sendButton.setOnAction(e -> sendButtonAction());
        HBox hBox = new HBox(openButton, selButton, sendButton);
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setSpacing(5);
        hBox.setPadding(new Insets(3));

        inputArea = new TextArea();
        inputArea.setWrapText(true);

        VBox vBox = new VBox(menuBar, recvArea, hBox, inputArea);
        vBox.setAlignment(Pos.CENTER);

        scene = new Scene(vBox);
        stage.setScene(scene);
        stage.setTitle("File Transmitter");
        stage.setOnCloseRequest(e -> {
            if (ValidateUtil.isValid(mqttManager)) {
                mqttManager.disConnect();
            }
            Platform.exit();
            System.exit(0);
        });
        stage.show();

        try {
            mqttManager.createConnect();
            mqttManager.subscribe(Topics.MOBILE + Topics.SEPARATOR + Topics.EXTRA_MORE);
        } catch (MqttException e) {
            showException(e.toString());
        }
    }

    /**
     * 发送消息
     */
    private void sendButtonAction() {
        String message = inputArea.getText();
        if (ValidateUtil.isValid(message) && ValidateUtil.isValid(mqttManager)) {
            new Thread(() -> mqttManager.publish(Topics.PC_TEXT, message.getBytes(Charset.forName("UTF-8")))).start();
        }
    }

    /**
     * 发送文件
     */
    private void selButtonAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("pdf", "*.pdf"),
                new FileChooser.ExtensionFilter("MS Office",
                        "*.doc", "*.docx",
                        "*.xls", "*.xlsx",
                        "*.ppt", "*.pptx"),
                new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.png"),
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );
        fileChooser.setInitialDirectory(new File(storePath.getOpenPath()));
        File file = fileChooser.showOpenDialog(stage);
        if (null != file) {
            try {
                byte[] payload = FileUtils.readFileToByteArray(file);
                new Thread(() -> mqttManager.publish(Topics.PC_FILE + file.getName(), payload)).start();
            } catch (IOException e) {
                showException(e.toString());
            }
        }
    }

    /**
     * 打开文件位置
     */
    private void openButtonAction() {
        try {
            Runtime.getRuntime().exec("explorer " + storePath.getFilePath());
        } catch (IOException e) {
            showException(e.toString());
        }
    }

    /**
     * 创建新消息
     * @param message 消息内容
     * @param header 是否接收的信息
     * @return TextArea 添加到UI
     */
    private void newMessage(String message, String header) {
        recvArea.appendText(header + ": \n");
        recvArea.appendText(message + "\n" + new Date().toString() + "\n\n");
    }

    private void showException(String message) {
        newMessage(message, Topics.ERR);
    }

    public void onMessageArrived(String topic, byte[] payload) {
        Platform.runLater(() -> {
            String[] topics = topic.split(Topics.SEPARATOR);
            String message;
            if (topics.length == 3) {
                String fileName = topics[2];
                System.out.println("storePath = " + storePath.getFilePath());
                File file = new File(storePath.getFilePath() + File.separator + fileName);
                try {
                    FileUtils.writeByteArrayToFile(file, payload);
                    message = fileName + Topics.RECV_HINT;//接收成功
                } catch (IOException e) {
                    showException(e.toString());
                    message = fileName + Topics.RECV_FAIL;//接受失败
                }
            } else {
                message = new String(payload, Charset.forName("UTF-8"));
            }
            newMessage(message, Topics.MOBILE);
        });
    }

    public void onDeliveryComplete(String topic, byte[] payload) {
        Platform.runLater(() -> {
            String[] topics = topic.split(Topics.SEPARATOR);
            String message;
            if (topics.length == 3) {
                String fileName = topics[2];
                message = fileName + Topics.SEND_HINT;
            } else {
                message = new String(payload, Charset.forName("UTF-8"));
                inputArea.clear();
            }
            newMessage(message, Topics.PC);
        });
    }

    public void onException(String message) {
        Platform.runLater(() -> {
            showException(message);
        });
    }

}
