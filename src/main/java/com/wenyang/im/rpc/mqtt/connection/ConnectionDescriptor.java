package com.wenyang.im.rpc.mqtt.connection;

import com.wenyang.im.rpc.netty.AutoFlushHandler;
import com.wenyang.im.rpc.netty.NettyUtils;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 连接状态
 */
@Slf4j
public class ConnectionDescriptor {


    public enum ConnectionState {
        DISCONNECTED, //1
        SENDACK,  //2
        SESSION_CREATED,//3
        MESSAGES_REPUBLISHED,
        ESTABLISHED,
        MESSAGES_DROPPED,
        INTERCEPTORS_NOTIFIED;
    }

    public final String clientID;
    private final Channel channel;
    private final AtomicReference<ConnectionState> channelState = new AtomicReference<>(ConnectionState.DISCONNECTED);

    public ConnectionDescriptor(String clientID, Channel session) {
        this.clientID = clientID;
        this.channel = session;
    }

    public void writeAndFlush(Object payload) {
        this.channel.writeAndFlush(payload);
    }

    public void setupAutoFlusher(int flushIntervalMs) {
        try {
            this.channel.pipeline().addAfter(
                    "idleEventHandler",
                    "autoFlusher",
                    new AutoFlushHandler(flushIntervalMs, TimeUnit.MILLISECONDS));
        } catch (NoSuchElementException nseex) {
            this.channel.pipeline()
                    .addFirst("autoFlusher", new AutoFlushHandler(flushIntervalMs, TimeUnit.MILLISECONDS));
        }
    }

    public boolean doesNotUseChannel(Channel channel) {
        return !(this.channel.equals(channel));
    }

    public boolean close() {
        log.info("Closing connection descriptor. MqttClientId = {}.", clientID);
        final boolean success = assignState(ConnectionState.INTERCEPTORS_NOTIFIED, ConnectionState.DISCONNECTED);
        if (!success) {
            return false;
        }
        this.channel.close();
        return true;
    }

    public String getUsername() {
        return NettyUtils.userName(this.channel);
    }

    public void abort() {
        this.channel.close();
    }

    public boolean assignState(ConnectionState expected, ConnectionState newState) {
        log.debug(
                "Updating state of connection descriptor. MqttClientId = {}, expectedState = {}, newState = {}.",
                clientID,
                expected,
                newState);
        boolean retval = channelState.compareAndSet(expected, newState);
        if (!retval) {
            log.error(
                    "Unable to update state of connection descriptor."
                            + " MqttclientId = {}, expectedState = {}, newState = {}.",
                    clientID,
                    expected,
                    newState);
        }
        return retval;
    }

    @Override
    public String toString() {
        return "ConnectionDescriptor{" + "clientID=" + clientID + ", state=" + channelState.get() + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ConnectionDescriptor that = (ConnectionDescriptor) o;

        if (clientID != null ? !clientID.equals(that.clientID) : that.clientID != null)
            return false;
        return !(channel != null ? !channel.equals(that.channel) : that.channel != null);
    }


    @Override
    public int hashCode() {
        int result = clientID != null ? clientID.hashCode() : 0;
        result = 31 * result + (channel != null ? channel.hashCode() : 0);
        return result;
    }

    public Channel getChannel() {
        return channel;
    }
}
