package com.wenyang.im.rpc.mqtt;

import com.wenyang.im.rpc.mqtt.handler.MqttClientHandler;
import com.wenyang.im.rpc.mqtt.handler.impl.MqttMessageHandlerImpl;
import com.wenyang.im.rpc.netty.MoquetteIdleTimeoutHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @Author wen.yang
 */
public class PipelineInitializer {


    private int nettyChannelTimeoutSeconds = 10;

    private static final String encoder = "encoder";
    private static final String decoder = "decoder";
    private static final String idleStateHandler = "idleStateHandler";

    public PipelineInitializer(int nettyChannelTimeoutSeconds) {
        this.nettyChannelTimeoutSeconds = nettyChannelTimeoutSeconds;
    }

    public void init(ChannelPipeline pipeline, MqttClientHandler mqttClientHandler) {
        pipeline.addFirst(idleStateHandler, new IdleStateHandler(nettyChannelTimeoutSeconds,
                0, 0));
        pipeline.addAfter(idleStateHandler, "idleEventHandler", new MoquetteIdleTimeoutHandler());
        pipeline.addLast(decoder, new MqttDecoder());
        pipeline.addLast(encoder, MqttEncoder.INSTANCE);
        pipeline.addLast("handler", mqttClientHandler);
    }
}
