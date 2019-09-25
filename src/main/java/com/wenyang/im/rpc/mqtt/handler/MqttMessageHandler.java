package com.wenyang.im.rpc.mqtt.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;

import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD;
import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;

/**
 * @Author wen.yang
 */
public interface MqttMessageHandler {


    default MqttConnAckMessage collectionAck(MqttConnectReturnCode mqttConnectReturnCode) {
        //消息头部固定
        MqttFixedHeader mqttFixedHeader
                = new MqttFixedHeader(MqttMessageType.CONNACK,
                false, MqttQoS.AT_MOST_ONCE,
                false, 0);
        //消息可变头部
        MqttConnAckVariableHeader mqttConnAckVariableHeader
                = new MqttConnAckVariableHeader(mqttConnectReturnCode, false);
        return new MqttConnAckMessage(mqttFixedHeader, mqttConnAckVariableHeader);
    }

    /**
     * 客户端提示账号密码错误：错误吗：4
     */
    default void failedCredentials(Channel session) {
        session.writeAndFlush(collectionAck(CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD));
    }

    /**
     * 客户端提示：鉴权错误 错误码：2
     */
    default void failedBlocked(Channel session) {
        session.writeAndFlush(collectionAck(CONNECTION_REFUSED_IDENTIFIER_REJECTED));
    }

    /**
     * 处理连接
     *
     * @param channel
     * @param msg
     */
    void processConnect(Channel channel, MqttMessage msg);

    /**
     * 处理订阅
     *
     * @param channel
     * @param msg
     */
    void processSubscribe(Channel channel, MqttMessage msg);


    /**
     * 取消订阅
     *
     * @param channel
     * @param msg
     */
    void processUnsubscribe(Channel channel, MqttMessage msg);


    /**
     * 处理发布
     *
     * @param channel
     * @param msg
     */
    void processPublish(Channel channel, MqttMessage msg);

    /**
     * 处理发布回执
     *
     * @param channel
     * @param msg
     */
    void processPubRec(Channel channel, MqttMessage msg);


    /**
     * 处理发布完成
     *
     * @param channel
     * @param msg
     */
    void processPubComp(Channel channel, MqttMessage msg);


}
