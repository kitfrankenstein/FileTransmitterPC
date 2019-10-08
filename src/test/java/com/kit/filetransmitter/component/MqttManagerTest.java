package com.kit.filetransmitter.component;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Test;

/**
 * @author Kit
 * @date: 2019/10/8 21:32
 */
public class MqttManagerTest {

    @Test
    public void createConnect() throws MqttException {
        MqttManager mqttManager = MqttManager.getInstance(null);
        mqttManager.createConnect();
    }
}