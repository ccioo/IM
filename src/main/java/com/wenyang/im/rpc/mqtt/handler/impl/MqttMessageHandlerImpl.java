package com.wenyang.im.rpc.mqtt.handler.impl;

import com.wenyang.im.rpc.distributed.DistributeSessionackStore;
import com.wenyang.im.rpc.mqtt.handler.MqttMessageHandler;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION;

/**
 * @Author wen.yang
 */
@Slf4j
public class MqttMessageHandlerImpl implements MqttMessageHandler {


    DistributeSessionackStore distributeSessionStore;

    public MqttMessageHandlerImpl(DistributeSessionackStore distributeSessionStore) {
        this.distributeSessionStore = distributeSessionStore;
    }

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
                    = distributeSessionStore.collectionAck(CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION);
            channel.writeAndFlush(mqttConnAckMessage);
            channel.close();
            return;
        }

        //客户端校验
        String clientId = mqttConnectPayload.clientIdentifier();

        if (clientId == null || clientId.length() == 0) {
            MqttConnAckMessage mqttConnAckMessage
                    = distributeSessionStore.collectionAck(CONNECTION_REFUSED_IDENTIFIER_REJECTED);
            channel.writeAndFlush(mqttConnAckMessage);
            channel.close();
            return;
        }

        String userName1 = mqttConnectMessage.payload().userName();
        String password1 = mqttConnectMessage.payload().password();
        boolean login = distributeSessionStore.login(channel, mqttConnectMessage, clientId);
        if (login) {

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
