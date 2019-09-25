package com.wenyang.im.rpc.mqtt;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @Author wen.yang
 */
public class MqttApplcationContent {

    EventLoopGroup m_bossGroup;
    EventLoopGroup m_workerGroup;


    public void initMqqtProtocol() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        if (false) {
            m_bossGroup = new EpollEventLoopGroup(1);
            m_workerGroup = new EpollEventLoopGroup();
        } else {
            m_bossGroup = new NioEventLoopGroup(1);
            m_workerGroup = new NioEventLoopGroup();
        }


    }
}
