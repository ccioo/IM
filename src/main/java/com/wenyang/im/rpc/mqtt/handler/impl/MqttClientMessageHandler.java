package com.wenyang.im.rpc.mqtt.handler.impl;

import com.moque.third.BrokerInterceptor;
import com.wenyang.im.rpc.distributed.BaseSessionackVo;
import com.wenyang.im.rpc.distributed.DistributeSessionackStore;
import com.wenyang.im.rpc.distributed.MqttClientSessionStore;
import com.wenyang.im.rpc.distributed.ServiceMessageStore;
import com.wenyang.im.rpc.mqtt.connection.ConnectionDescriptor;
import com.wenyang.im.rpc.mqtt.handler.MqttMessageHandler;
import com.wenyang.im.rpc.mqtt.protocol.WFCMessage;
import com.wenyang.im.rpc.netty.NettyUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

import static com.wenyang.im.rpc.mqtt.connection.ConnectionDescriptor.ConnectionState.DISCONNECTED;
import static com.wenyang.im.rpc.mqtt.connection.ConnectionDescriptor.ConnectionState.SENDACK;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.*;

/**
 * @Author wen.yang
 */
@Slf4j
public class MqttClientMessageHandler extends BaseSessionackVo implements MqttMessageHandler {


    BrokerInterceptor m_interceptor;
    ServiceMessageStore serviceMessageStore;
    MqttClientSessionStore mqttClientSessionStore;
    DistributeSessionackStore distributeSessionStore;


    public MqttClientMessageHandler(DistributeSessionackStore distributeSessionStore,
                                    MqttClientSessionStore mqttClientSessionStore) {
        this.distributeSessionStore = distributeSessionStore;
        this.mqttClientSessionStore = mqttClientSessionStore;
        this.serviceMessageStore = new ServiceMessageStore();
        this.m_interceptor = new BrokerInterceptor(10, new ArrayList<>());
    }

    @Override
    public void processConnect(Channel channel, MqttMessage msg) {
        MqttConnectMessage mqttConnectMessage = (MqttConnectMessage) msg;
        MqttConnectPayload mqttConnectPayload = ((MqttConnectMessage) msg).payload();
        //版本校验
        if (MqttVersion.MQTT_3_1.protocolLevel() != mqttConnectMessage.variableHeader().version()
                && MqttVersion.MQTT_3_1_1.protocolLevel() != mqttConnectMessage.variableHeader().version()
                && (byte) 5 != mqttConnectMessage.variableHeader().version()) {
            MqttMessage mqttConnAckMessage
                    = distributeSessionStore.collectionAck(CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION);
            channel.writeAndFlush(mqttConnAckMessage);
            channel.close();
            return;
        }

        //客户端参数校验
        String clientId = mqttConnectPayload.clientIdentifier();

        if (clientId == null || clientId.length() == 0) {
            MqttMessage mqttConnAckMessage
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
        //存活时间
        initializeKeepAliveTimeout(channel, mqttConnectMessage, clientId);
        //通知客户端连接成功 基于事件-这里没有实现类
        m_interceptor.notifyClientConnected(mqttConnectMessage);
        // 通知客户端ACK连接事件


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


    /**
     * @param channel
     * @param msg
     * @param clientId
     */
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


    private boolean sendAck(ConnectionDescriptor descriptor, MqttConnectMessage msg, final String clientId) {
        log.info("Sending connect ACK. CId={}", clientId);
        //断言状态
        final boolean success = descriptor.assignState(DISCONNECTED, SENDACK);
        if (!success) {
            return false;
        }
        //在此断言下 是否准备好
        boolean isSessionAlreadyStored = distributeSessionStore.getSession(clientId) != null;
        //用户离线 之后重连 可以读取到消息
        String user = msg.payload().userName();
        long messageHead = serviceMessageStore.getMessageHead(user);
        long friendHead = serviceMessageStore.getFriendHead(user);
        long friendRqHead = serviceMessageStore.getFriendRqHead(user);
        long settingHead = serviceMessageStore.getSettingHead(user);
        WFCMessage.ConnectAckPayload payload = WFCMessage.ConnectAckPayload.newBuilder()
                .setMsgHead(messageHead)
                .setFriendHead(friendHead)
                .setFriendRqHead(friendRqHead)
                .setSettingHead(settingHead)
                .setServerTime(System.currentTimeMillis())
                .build();

        //返回
        MqttMessage okResponse;
        if (!msg.variableHeader().isCleanSession() && isSessionAlreadyStored) {
            okResponse = connAckWithSessionPresent(CONNECTION_ACCEPTED, payload.toByteArray());
        } else {
            okResponse = connAckWithSessionPresent(CONNECTION_ACCEPTED, payload.toByteArray());
        }
        descriptor.writeAndFlush(okResponse);
        log.info("The connect ACK has been send. clientId={}", clientId);
        return true;
    }

    private MqttMessage connAckWithSessionPresent(MqttConnectReturnCode returnCode, byte[] data) {
        return collectionAck(returnCode, true, data);
    }
}
