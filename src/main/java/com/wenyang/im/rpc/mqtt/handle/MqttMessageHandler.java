package com.wenyang.im.rpc.mqtt.handle;

import com.wenyang.im.rpc.mqtt.codec.MQTTMessage;
import io.netty.channel.Channel;

/**
 * @Author wen.yang
 */
public interface MqttMessageHandler {

    /**
     * 处理连接
     *
     * @param channel
     * @param msg
     */
    void processConnect(Channel channel, MQTTMessage msg);

    /**
     * 处理订阅
     *
     * @param channel
     * @param msg
     */
    void processSubscribe(Channel channel, MQTTMessage msg);


    /**
     * 取消订阅
     *
     * @param channel
     * @param msg
     */
    void processUnsubscribe(Channel channel, MQTTMessage msg);


    /**
     * 处理发布
     *
     * @param channel
     * @param msg
     */
    void processPublish(Channel channel, MQTTMessage msg);

    /**
     * 处理发布回执
     *
     * @param channel
     * @param msg
     */
    void processPubRec(Channel channel, MQTTMessage msg);


    /**
     * 处理发布完成
     *
     * @param channel
     * @param msg
     */
    void processPubComp(Channel channel, MQTTMessage msg);














}
