package com.wenyang.im.rpc.distributed;

import com.wenyang.im.rpc.mqtt.connection.ConnectionDescriptor;
import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author wen.yang
 */
@Data
public class MqttClientSessionStore {

    private final ConcurrentMap<String, ConnectionDescriptor> connectionDescriptors = new ConcurrentHashMap<>();


    /**
     * 保存连接
     */
    public ConnectionDescriptor saveConnection(ConnectionDescriptor descriptor) {
        return connectionDescriptors.putIfAbsent(descriptor.clientID, descriptor);
    }

    /**
     * 移除连接
     */
    public boolean removeConnection(ConnectionDescriptor descriptor) {
        return connectionDescriptors.remove(descriptor.clientID, descriptor);
    }
}
