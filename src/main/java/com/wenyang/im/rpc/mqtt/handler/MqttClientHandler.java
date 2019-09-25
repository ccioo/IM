package com.wenyang.im.rpc.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.mqtt.MqttQoS.AT_MOST_ONCE;

/**
 * @Author wen.yang
 * 重写4个方法：
 * channelRead()
 * channelInactive()
 * exceptionCaught()
 * channelWritabilityChanged()
 */
@Slf4j
public class MqttClientHandler extends ChannelInboundHandlerAdapter {

    MqttMessageHandler mqttMessageHandler;

    public MqttClientHandler(MqttMessageHandler mqttMessageHandler) {
        this.mqttMessageHandler = mqttMessageHandler;
    }

    /**
     * 读取MQTT消息内容
     *
     * @param ctx
     * @param message
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        if (!(message instanceof MqttMessage)) {
            return;
        }
        MqttMessage mqttMessage = (MqttMessage) message;
        MqttMessageType messageType = mqttMessage.fixedHeader().messageType();

        switch (messageType) {
            case CONNECT:
                mqttMessageHandler.processConnect(ctx.channel(), mqttMessage);
                break;
            case SUBSCRIBE:
                mqttMessageHandler.processSubscribe(ctx.channel(), mqttMessage);
                break;
            case UNSUBSCRIBE:
                break;
            case PUBLISH:
                break;
            case PUBREC:
                break;
            case PUBCOMP:
                break;
            case PUBREL:
                break;
            case DISCONNECT:
                break;
            case PUBACK:
                break;
            case PINGREQ:
                MqttFixedHeader pingHeader = new MqttFixedHeader(
                        MqttMessageType.PINGRESP,
                        false,
                        AT_MOST_ONCE,
                        false,
                        0);
                MqttMessage pingResp = new MqttMessage(pingHeader);
                ctx.writeAndFlush(pingResp);
                break;
            default:
                break;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {

    }

}
