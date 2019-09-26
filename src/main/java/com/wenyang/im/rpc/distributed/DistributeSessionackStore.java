package com.wenyang.im.rpc.distributed;

import com.wenyang.im.rpc.mqtt.session.ClientSession;
import com.wenyang.im.rpc.mqtt.session.ServerSession;
import com.wenyang.im.rpc.mqtt.spi.IMessagesStore;
import com.wenyang.im.rpc.netty.NettyUtils;
import com.wenyang.im.rpc.security.AESUtils;
import com.wenyang.im.rpc.security.TokenCheckUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @Author wen.yang
 */

@Slf4j
public class DistributeSessionackStore extends BaseSessionackVo {

    private Map<String, Integer> userStateMap = new ConcurrentHashMap<String, Integer>();
    private final Map<String, ServerSession> serverSessionMap = new ConcurrentHashMap<>();
    private final Map<String, ConcurrentSkipListSet<String>> username_clientList = new ConcurrentHashMap<>();


    DBSessionStore isessionMapper;

    public DistributeSessionackStore(DBSessionStore isessionDBSessionStore) {
        this.isessionMapper = isessionDBSessionStore;
    }

    /**
     * 客户端登录处理
     *
     * @param channel
     * @param mqttConnectMessage
     * @param clientId
     * @return
     */
    public boolean login(Channel channel, MqttConnectMessage mqttConnectMessage, final String clientId) {
        //校验登录 session 管理
        boolean hasUserName = mqttConnectMessage.variableHeader().hasUserName();
        if (hasUserName) {
            String userName = mqttConnectMessage.payload().userName();
            Integer state = userStateMap.get(userName);
            if (state != null && state == 2) {
                failedBlocked(channel);
                return false;
            }
        }
        //密码校验
        boolean hasPassword = mqttConnectMessage.variableHeader().hasPassword();
        if (hasPassword) {
            byte[] password = mqttConnectMessage.payload().passwordInBytes();
            ServerSession serverSession = getSession(clientId);
            if (serverSession == null) {
                String userName = mqttConnectMessage.payload().userName();
                serverSession = this.createNewSession(clientId, userName);
                //用户名或者密码错误
                if (serverSession == null || !StringUtils.equals(serverSession.getUsername(), mqttConnectMessage.payload().userName())) {
                    failedCredentials(channel);
                    return false;
                }
                byte[] passwordDecode = AESUtils.AESDecrypt(password, serverSession.getSecret(), true);
                if (passwordDecode == null) {
                    failedCredentials(channel);
                    return false;
                }
                //账号密码校验
                boolean access = TokenCheckUtils.checkValid(clientId, userName, password);
                if (!access) {
                    failedCredentials(channel);
                    return false;
                }
                NettyUtils.userName(channel, mqttConnectMessage.payload().userName());
            }
        }
        //匿名方式是否不支持
//        if (false) {
//            failedCredentials(channel);
//            channel.close();
//            return false;
//        }
        return true;
    }

    /**
     * 根据客户端获取session
     */
    public ServerSession getSession(String clientID) {
        ServerSession session = serverSessionMap.get(clientID);
        if (session == null) {
            log.error("Can't find the session for client <{}>", clientID);
        }
        return session;
    }


    /**
     * 创建新的会话
     *
     * @param clientID
     * @param username
     * @return
     */
    public ServerSession createNewSession(String clientID,
                                          String username) {
        ServerSession serverSession = serverSessionMap.get(clientID);
        if (serverSession != null) {
            log.error("already exists a session for client <{}>, bad condition", clientID);
            throw new IllegalArgumentException("无法创建一个新的session 因为已经存在" + clientID);
        }
        ClientSession clientSession = new ClientSession(clientID, this);
        serverSession = isessionMapper.getSession(username, clientID, clientSession);
        if (serverSession == null) {
            serverSession = isessionMapper.createSession(clientID, username, clientSession);
        }
        //缓存到分布式
        serverSessionMap.put(clientID, serverSession);
        //存储一下一个用户存在多个客户端
        ConcurrentSkipListSet<String> sessionSet = username_clientList.get(username);
        if (sessionSet != null) {
            sessionSet.add(clientID);
        } else {
            sessionSet = new ConcurrentSkipListSet<>();
            username_clientList.put(username, sessionSet);
        }
        return serverSession;
    }


    public IMessagesStore.StoredMessage inFlightAck(String clientID, int messageID) {
        return getSession(clientID).getOutboundFlightMessages().remove(messageID);
    }


    public void inFlight(String clientID, int messageID, IMessagesStore.StoredMessage msg) {
        getSession(clientID).getOutboundFlightMessages().put(messageID, msg);
    }


    public IMessagesStore.StoredMessage inboundInflight(String clientID, int messageID) {
        return getSession(clientID).getInboundFlightMessages().get(messageID);
    }

    public void markAsInboundInflight(String clientID, int messageID, IMessagesStore.StoredMessage msg) {
        if (!serverSessionMap.containsKey(clientID))
            log.error("Can't find the session for client <{}>", clientID);
        serverSessionMap.get(clientID).getInboundFlightMessages().put(messageID, msg);
    }


    public IMessagesStore.StoredMessage secondPhaseAcknowledged(String clientID, int messageID) {
        log.info("Acknowledged message in second phase, clientID <{}> messageID {}", clientID, messageID);
        return getSession(clientID).getSecondPhaseStore().remove(messageID);
    }


    /**
     * 返回给定客户端会话的下一个有效的packetIdentifier
     */
    public int nextPacketID(String clientID) {
        if (!serverSessionMap.containsKey(clientID)) {
            log.error("Can't find the session for client <{}>", clientID);
            return -1;
        }
        Map<Integer, IMessagesStore.StoredMessage> storedMessage
                = serverSessionMap.get(clientID).getOutboundFlightMessages();
        int maxId = storedMessage.keySet().isEmpty() ? 0 : Collections.max(storedMessage.keySet());
        int nextPacketId = (maxId + 1) % 0xFFFF;
        storedMessage.put(nextPacketId, null);
        return nextPacketId;
    }

}
