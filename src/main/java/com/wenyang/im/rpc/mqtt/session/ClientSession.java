package com.wenyang.im.rpc.mqtt.session;

import io.netty.handler.codec.mqtt.MqttVersion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientSession {
    String username;
    String clientID;
    private String appName;
    private String deviceToken;
    private String voipDeviceToken;
    private String secret;
    private String dbSecret;
    private long lastActiveTime;

    private long lastChatroomActiveTime;

    private volatile int unReceivedMsgs;

    private MqttVersion mqttVersion = MqttVersion.MQTT_3_1_1;

    public ClientSession(String clientID,String username) {
        this.clientID = clientID;
        this.username = username;
    }
}
