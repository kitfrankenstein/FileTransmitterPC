package com.kit.filetransmitter.view;

import com.kit.filetransmitter.entity.StorePath;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author Kit
 * @date: 2019/10/8 19:29
 */
public class SettingView {

    private Stage stage;
    private StorePath storePath;
    private Button filePathBtn, openPathBtn;
    private Button saveBtn, cancelBtn;
    private TextField filePathField, openPathField;

    SettingView(StorePath storePath) {
        this.storePath = storePath;

        Label filePathLabel = new Label("文件保存路径："),
                openPathLabel = new Label("默认选择文件路径：");
        filePathLabel.setPrefWidth(120);
        openPathLabel.setPrefWidth(120);

        filePathField = new TextField();
        openPathField = new TextField();
        filePathField.setText(storePath.getFilePath());
        openPathField.setText(storePath.getOpenPath());

        filePathBtn = new Button("选择");
        filePathBtn.setOnAction(e -> filePathBtnAction());
        openPathBtn = new Button("选择");
        openPathBtn.setOnAction(e -> openPathBtnAction());

        HBox filePathBox = new HBox(filePathLabel, filePathField, filePathBtn);
        filePathBox.setSpacing(5);
        HBox openPathBox = new HBox(openPathLabel, openPathField, openPathBtn);
        openPathBox.setSpacing(5);

        saveBtn = new Button("保存");
        saveBtn.setOnAction(e -> saveBtnAction());
        cancelBtn = new Button("取消");
        cancelBtn.setOnAction(e -> cancelBtnAction());
        HBox hBox = new HBox(saveBtn, cancelBtn);
        hBox.setSpacing(5);
        hBox.setAlignment(Pos.BOTTOM_RIGHT);

        VBox vBox = new VBox(filePathBox, openPathBox, hBox);
        vBox.setSpacing(5);
        vBox.setPadding(new Insets(5));

        Scene scene = new Scene(vBox);
        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Setting");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.show();
    }

    private void filePathBtnAction() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(filePathField.getText()));
        File filePathDir = directoryChooser.showDialog(filePathField.getScene().getWindow());
        if (filePathDir != null && filePathDir.isDirectory()) {
            filePathField.setText(filePathDir.getAbsolutePath());
        }
    }

    private void openPathBtnAction() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(openPathField.getText()));
        System.out.println("directoryChooser = " + directoryChooser.getInitialDirectory().getAbsolutePath());
        File openPathDir = directoryChooser.showDialog(openPathField.getScene().getWindow());
        if (openPathDir != null && openPathDir.isDirectory()) {
            openPathField.setText(openPathDir.getAbsolutePath());
        }
    }

    private void saveBtnAction() {
        storePath.setFilePath(filePathField.getText());
        storePath.setOpenPath(openPathField.getText());
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(StorePath.LOCATION))){
            outputStream.writeObject(storePath);
            saveBtn.setDisable(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cancelBtnAction() {
        stage.close();
    }

}
