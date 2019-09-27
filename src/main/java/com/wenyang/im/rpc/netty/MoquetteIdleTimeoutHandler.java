package com.wenyang.im.rpc.netty;

import com.common.core.utils.NettyUtils;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable
public class MoquetteIdleTimeoutHandler extends ChannelDuplexHandler {


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState e = ((IdleStateEvent) evt).state();
            if (e == IdleState.READER_IDLE) {
                log.info("Firing channel inactive event. MqttClientId = {}.", NettyUtils.clientID(ctx.channel()));
                ctx.fireChannelInactive();
                ctx.close();
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Firing Netty event. MqttClientId = {}, eventClass = {}.", NettyUtils.clientID(ctx.channel()), evt.getClass().getName());
            }
            super.userEventTriggered(ctx, evt);
        }
    }
}
