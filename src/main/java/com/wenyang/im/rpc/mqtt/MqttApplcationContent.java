package com.wenyang.im.rpc.mqtt;

import com.wenyang.im.rpc.distributed.DistributeSessionackStore;
import com.wenyang.im.rpc.distributed.MqttClientSessionStore;
import com.wenyang.im.rpc.distributed.impl.DBSessionStoreImpl;
import com.wenyang.im.rpc.mqtt.handler.MqttClientHandler;
import com.wenyang.im.rpc.mqtt.handler.MqttMessageHandler;
import com.wenyang.im.rpc.mqtt.handler.impl.MqttClientMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;

/**
 * @Author wen.yang
 */
@Slf4j
public class MqttApplcationContent {

    EventLoopGroup m_bossGroup;
    EventLoopGroup m_workerGroup;
    private Class<? extends ServerSocketChannel> channelClass;

    private int nettySoBacklog = 128;
    private boolean nettySoReuseaddr = true;
    private boolean nettyTcpNodelay = true;
    private boolean nettySoKeepalive = true;
    private int nettyChannelTimeoutSeconds = 10;


    private static final String host = "127.0.0.1";
    private static final int longPort = 1883;
    private static final String protocol = "TCP MQTT";

    /**
     * 初始化MQTT 协议 连接
     */
    public void initMqttApplication() {
        if (false) {
            m_bossGroup = new EpollEventLoopGroup(1);
            m_workerGroup = new EpollEventLoopGroup();
            channelClass = EpollServerSocketChannel.class;
        } else {
            m_bossGroup = new NioEventLoopGroup(1);
            m_workerGroup = new NioEventLoopGroup();
            channelClass = NioServerSocketChannel.class;
        }
        DistributeSessionackStore distributeSessionackStore
                = new DistributeSessionackStore(new DBSessionStoreImpl());
        MqttClientSessionStore mqttClientSessionStore = new MqttClientSessionStore();
        MqttMessageHandler mqttMessageHandler = new MqttClientMessageHandler(distributeSessionackStore, mqttClientSessionStore);
        MqttClientHandler mqttClientHandler = new MqttClientHandler(mqttMessageHandler);

        initConnectionFactory(host, longPort, protocol,
                new PipelineInitializer(nettyChannelTimeoutSeconds), mqttClientHandler);
    }

    private void initConnectionFactory(String host, int port, String protocol,
                                       final PipelineInitializer pipeliner,
                                       final MqttClientHandler mqttClientHandler) {
        log.info("Initializing server. Protocol={}", protocol);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(m_bossGroup, m_workerGroup).channel(channelClass)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        try {
                            pipeliner.init(pipeline, mqttClientHandler);
                        } catch (Throwable th) {
                            log.error("Severe error during pipeline creation", th);
                            throw th;
                        }
                    }
                })
                // 服务端可连接队列大小
                .option(ChannelOption.SO_BACKLOG, 10240)
                .option(ChannelOption.SO_BACKLOG, nettySoBacklog)
                .option(ChannelOption.SO_REUSEADDR, nettySoReuseaddr)
                .childOption(ChannelOption.TCP_NODELAY, nettyTcpNodelay)
                .childOption(ChannelOption.SO_KEEPALIVE, nettySoKeepalive);
        try {
            ChannelFuture channelFuture = serverBootstrap.bind(host, port);
            channelFuture.sync().addListener(FIRE_EXCEPTION_ON_FAILURE);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
