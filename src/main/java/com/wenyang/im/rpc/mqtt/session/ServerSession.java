package com.wenyang.im.rpc.mqtt.session;

import com.wenyang.im.rpc.mqtt.spi.IMessagesStore;
import io.netty.handler.codec.mqtt.MqttVersion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerSession {

    String clientID;
    String username;
    private String appName;
    private String deviceToken;
    private String voipDeviceToken;
    private String secret;
    private String dbSecret;
    private long lastActiveTime;
    private long lastChatroomActiveTime;
    private volatile int unReceivedMsgs;
    private MqttVersion mqttVersion = MqttVersion.MQTT_3_1_1;


    private int pushType;
    private int platform;
    private String deviceName;
    private String deviceVersion;
    private String phoneName;
    private String language;
    private String carrierName;
    private long updateDt;
    ClientSession clientSession;

    final Map<Integer, IMessagesStore.StoredMessage> outboundFlightMessages = new ConcurrentHashMap<>();
    final Map<Integer, IMessagesStore.StoredMessage> inboundFlightMessages = new ConcurrentHashMap<>();
    final Map<Integer, IMessagesStore.StoredMessage> secondPhaseStore = new ConcurrentHashMap<>();


    public ServerSession(String username,
                         String clientID,
                         ClientSession clientSession) {
        this.clientID = clientID;
        this.username = username;
        this.clientSession = clientSession;
    }

}
