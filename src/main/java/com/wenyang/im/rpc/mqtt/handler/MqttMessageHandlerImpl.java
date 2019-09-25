package com.wenyang.im.rpc.mqtt.handler;

import com.wenyang.im.rpc.mqtt.session.ClientSession;
import com.wenyang.im.rpc.mqtt.session.SessionDB;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION;

/**
 * @Author wen.yang
 */
@Slf4j
public class MqttMessageHandlerImpl implements MqttMessageHandler {

    private Map<String, Integer> userStateMap = new ConcurrentHashMap<String, Integer>();

    private static final List<Byte> bytes
            = Arrays.asList(MqttVersion.MQTT_3_1.protocolLevel(),
            MqttVersion.MQTT_3_1_1.protocolLevel());


    @Override
    public void processConnect(Channel channel, MqttMessage msg) {
        MqttConnectMessage mqttConnectMessage = (MqttConnectMessage) msg;
        MqttConnectPayload mqttConnectPayload = ((MqttConnectMessage) msg).payload();
        //版本校验
        int version = mqttConnectMessage.variableHeader().version();
        if (!bytes.contains(version)) {
            MqttConnAckMessage mqttConnAckMessage
                    = collectionAck(CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION);
            channel.writeAndFlush(mqttConnAckMessage);
            channel.close();
            return;
        }

        //客户端校验
        String clientId = mqttConnectPayload.clientIdentifier();
        if (clientId == null || clientId.length() == 0) {
            MqttConnAckMessage mqttConnAckMessage
                    = collectionAck(CONNECTION_REFUSED_IDENTIFIER_REJECTED);
            channel.writeAndFlush(mqttConnAckMessage);
            channel.close();
            return;
        }

        //校验登录 session 管理
        boolean hasUserName = mqttConnectMessage.variableHeader().hasUserName();
        if (hasUserName) {
            String userName = mqttConnectMessage.payload().userName();
            Integer state = userStateMap.get(userName);
            if (state != null && state == 2) {
                failedBlocked(channel);
                return;
            }
        }
        //密码校验
        boolean hasPassword = mqttConnectMessage.variableHeader().hasPassword();
        if (hasPassword) {
            String password = mqttConnectMessage.payload().password();
            ClientSession clientSession
                    = SessionDB.getInstance().getClientSessionMap().get(clientId);
            if (clientSession == null) {
                String userName = mqttConnectMessage.payload().userName();
                clientSession = SessionDB.getInstance().createNewSession(clientId, userName);

            }
        }
        //用户名密码校验
        //todo
        //匿名方式是否不支持
        if (true) {
            failedCredentials(channel);
            return;
        }
    }


    @Override
    public void processSubscribe(Channel channel, MqttMessage msg) {

    }

    @Override
    public void processUnsubscribe(Channel channel, MqttMessage msg) {

    }

    @Override
    public void processPublish(Channel channel, MqttMessage msg) {

    }

    @Override
    public void processPubRec(Channel channel, MqttMessage msg) {

    }

    @Override
    public void processPubComp(Channel channel, MqttMessage msg) {

    }
}
