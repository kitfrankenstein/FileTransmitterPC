package com.kit.filetransmitter.util;

import com.kit.filetransmitter.component.MqttManager;

import java.lang.management.ManagementFactory;

/**
 * @author Kit
 * @date: 2019/10/3 22:41
 */
public class ValidateUtil {

    public static boolean isValid(MqttManager mqttManager) {
        return mqttManager != null && mqttManager.isConnected();
    }

    public static boolean isValid(String message) {
        return message != null && !message.equals("");
    }

    public static String getClientID() {
        return "client" + ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    }

}
