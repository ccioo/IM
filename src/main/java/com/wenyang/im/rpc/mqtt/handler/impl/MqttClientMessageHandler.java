package com.wenyang.im.rpc.mqtt.handler.impl;

import com.wenyang.im.rpc.distributed.DistributeSessionackStore;
import com.wenyang.im.rpc.distributed.MqttClientSessionStore;
import com.wenyang.im.rpc.mqtt.connection.ConnectionDescriptor;
import com.wenyang.im.rpc.mqtt.handler.MqttMessageHandler;
import com.wenyang.im.rpc.netty.NettyUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION;

/**
 * @Author wen.yang
 */
@Slf4j
public class MqttClientMessageHandler implements MqttMessageHandler {


    MqttClientSessionStore mqttClientSessionStore;

    DistributeSessionackStore distributeSessionStore;


    public MqttClientMessageHandler(DistributeSessionackStore distributeSessionStore,
                                    MqttClientSessionStore mqttClientSessionStore) {
        this.distributeSessionStore = distributeSessionStore;
        this.mqttClientSessionStore = mqttClientSessionStore;
    }

    @Override
    public void processConnect(Channel channel, MqttMessage msg) {
        MqttConnectMessage mqttConnectMessage = (MqttConnectMessage) msg;
        MqttConnectPayload mqttConnectPayload = ((MqttConnectMessage) msg).payload();
        //版本校验
        if (MqttVersion.MQTT_3_1.protocolLevel() != mqttConnectMessage.variableHeader().version()
                && MqttVersion.MQTT_3_1_1.protocolLevel() != mqttConnectMessage.variableHeader().version()
                && (byte) 5 != mqttConnectMessage.variableHeader().version()) {
            MqttConnAckMessage mqttConnAckMessage
                    = distributeSessionStore.collectionAck(CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION);
            channel.writeAndFlush(mqttConnAckMessage);
            channel.close();
            return;
        }

        //客户端参数校验
        String clientId = mqttConnectPayload.clientIdentifier();

        if (clientId == null || clientId.length() == 0) {
            MqttConnAckMessage mqttConnAckMessage
                    = distributeSessionStore.collectionAck(CONNECTION_REFUSED_IDENTIFIER_REJECTED);
            channel.writeAndFlush(mqttConnAckMessage);
            channel.close();
            return;
        }
        //鉴权
        boolean access = distributeSessionStore.login(channel, mqttConnectMessage, clientId);
        if (!access) {
            channel.close();
            return;
        }
        //hashMap 管理channel
        ConnectionDescriptor descriptor = new ConnectionDescriptor(clientId, channel);
        ConnectionDescriptor existing = this.mqttClientSessionStore.saveConnection(descriptor);
        if (existing != null) {
            log.info("当前客户端存在一个客户端连接 它将被关闭：{}", clientId);
            this.mqttClientSessionStore.removeConnection(existing);
            existing.abort();
            this.mqttClientSessionStore.saveConnection(descriptor);
        }
        initializeKeepAliveTimeout(channel, mqttConnectMessage, clientId);

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


    private void initializeKeepAliveTimeout(Channel channel, MqttConnectMessage msg, final String clientId) {
        int keepAlive = msg.variableHeader().keepAliveTimeSeconds();
        NettyUtils.keepAlive(channel, keepAlive);
        NettyUtils.cleanSession(channel, msg.variableHeader().isCleanSession());
        NettyUtils.clientID(channel, clientId);
        int idleTime = Math.round(keepAlive * 1.5f);
        setIdleTime(channel.pipeline(), idleTime);
        log.info("The connection has been configured CId={}, keepAlive={}, cleanSession={}, idleTime={}",
                clientId, keepAlive, msg.variableHeader().isCleanSession(), idleTime);
    }

    private void setIdleTime(ChannelPipeline pipeline, int idleTime) {
        if (pipeline.names().contains("idleStateHandler")) {
            pipeline.remove("idleStateHandler");
        }
        pipeline.addFirst("idleStateHandler", new IdleStateHandler(idleTime, 0, 0));
    }
}
