package com.kit.filetransmitter.component;

import com.kit.filetransmitter.util.ValidateUtil;
import com.kit.filetransmitter.view.ChatView;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * @author Kit
 * @date: 2019/10/3 22:35
 */
public class MqttManager {

    private final ChatView chatView;

    private MqttClient client;

    private String host = "tcp://:1883";
    private String username = "";
    private String password = "";

    private static volatile MqttManager instance;

    private MqttManager(ChatView chatView) {
        this.chatView = chatView;
    }

    public static MqttManager getInstance(ChatView chatView) {
        if (instance == null) {
            synchronized (MqttManager.class) {
                if (instance == null) {
                    instance = new MqttManager(chatView);
                }
            }
        }
        return instance;
    }

    /**
     * 创建连接
     * @throws MqttException 连接失败
     */
    public void createConnect() throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(true);
        options.setMaxInflight(100);
        options.setKeepAliveInterval(10);
        options.setConnectionTimeout(20000);
        options.setAutomaticReconnect(false);

        MqttCallback mqttCallback = new MyCallback();

        client = new MqttClient(host, ValidateUtil.getClientID(), new MemoryPersistence());
        client.setCallback(mqttCallback);
        client.connect(options);
    }

    /**
     * 创建和配置一个消息并发布
     * @param topicName 消息主题
     * @param payload 消息内容
     */
    public void publish(String topicName, byte[] payload) {
        if (client != null && client.isConnected()) {
            MqttMessage message = new MqttMessage(payload);
            message.setQos(0);
            try {
                client.publish(topicName, message);
            } catch (MqttException e) {
                chatView.onException("mqtt publish err: " + e.toString());
            }
        }
    }

    /**
     * 订阅消息
     * @param topicName 消息主题
     */
    public void subscribe(String topicName) {
        if (client != null && client.isConnected()) {
            try {
                IMqttToken token = client.subscribeWithResponse(topicName, 0);
                System.out.println(token.getResponse().toString());
            } catch (MqttException e) {
                chatView.onException("mqtt subscribe err: " + e.toString());
            }
        }
    }

    /**
     * 关闭连接
     */
    public void disConnect() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (Exception e) {
                chatView.onException(e.toString());
            }
        }
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * MQTT回调类，定义特定的回调操作
     */
    class MyCallback implements MqttCallbackExtended {

        /**
         * 连接中断
         */
        @Override
        public void connectionLost(Throwable cause) {
            cause.printStackTrace();
            chatView.onException(cause.toString());
        }


        /**
         * 消息到达
         */
        @Override
        public void messageArrived(String topic, MqttMessage message){
            chatView.onMessageArrived(topic, message.getPayload());
        }

        /**
         * 发布成功
         */
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            try {
                chatView.onDeliveryComplete(token.getTopics()[0], token.getMessage().getPayload());
            } catch (MqttException e) {
                chatView.onException(e.toString());
            }
        }

        /**
         * 连接成功
         */
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            System.out.println("mqtt connected to " + serverURI);
        }
    }


}
