package com.wenyang.im.rpc.mqtt.handle;

import com.wenyang.im.rpc.mqtt.codec.MQTTMessage;
import io.netty.channel.Channel;

/**
 * @Author wen.yang
 */
public class MqttMessageHandlerImpl implements MqttMessageHandler {


    @Override
    public void processConnect(Channel channel, MQTTMessage msg) {

    }

    @Override
    public void processSubscribe(Channel channel, MQTTMessage msg) {

    }

    @Override
    public void processUnsubscribe(Channel channel, MQTTMessage msg) {

    }

    @Override
    public void processPublish(Channel channel, MQTTMessage msg) {

    }

    @Override
    public void processPubRec(Channel channel, MQTTMessage msg) {

    }

    @Override
    public void processPubComp(Channel channel, MQTTMessage msg) {

    }
}
