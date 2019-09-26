package com.wenyang.im.rpc.distributed;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.mqtt.MqttConnectReturnCode.*;

/**
 * @Author wen.yang
 */
@Slf4j
public class BaseSessionackVo {


    /**
     * 客户端提示账号密码错误：错误吗：4
     */
    public void failedCredentials(Channel session) {
        session.writeAndFlush(collectionAck(CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD));
        session.close();
    }

    /**
     * 客户端提示：鉴权错误 错误码：2
     */
    public void failedBlocked(Channel session) {
        session.writeAndFlush(collectionAck(CONNECTION_REFUSED_IDENTIFIER_REJECTED));
        session.close();
    }

    /**
     * 客户端提示：鉴权错误 错误码：5
     */
    public void failedNoSession(Channel session) {
        session.writeAndFlush(collectionAck(CONNECTION_REFUSED_NOT_AUTHORIZED));
        log.info("Client {} failed to connect with bad username or password.", session);
        session.close();
    }


    /**
     * 连接回执消息
     *
     * @param mqttConnectReturnCode
     * @return
     */
    public MqttConnAckMessage collectionAck(MqttConnectReturnCode mqttConnectReturnCode) {
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
}
