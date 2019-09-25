package com.wenyang.im.rpc.mqtt.session;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
@Getter
public class SessionDB {

    private SessionDB() {
    }

    public static SessionDB sessionDB = new SessionDB();

    public static SessionDB getInstance() {
        return sessionDB;
    }

    private final Map<String, ClientSession> clientSessionMap = new ConcurrentHashMap<>();
    private final Map<String, ConcurrentSkipListSet<String>> username_clientList = new ConcurrentHashMap<>();


    public ClientSession createNewSession(String clientID,
                                          String username) {
        ClientSession clientSession = clientSessionMap.get(clientID);
        if (clientSession != null) {
            log.error("already exists a session for client <{}>, bad condition", clientID);
            throw new IllegalArgumentException("无法创建一个新的session 因为已经存在" + clientID);
        }
        clientSession = new ClientSession(clientID,username);
        clientSessionMap.put(clientID, clientSession);
        //存储一下一个用户存在多个客户端
        ConcurrentSkipListSet<String> sessionSet = username_clientList.get(username);
        if (sessionSet != null) {
            sessionSet.add(clientID);
        } else {
            sessionSet = new ConcurrentSkipListSet<>();
            username_clientList.put(username, sessionSet);
        }
        return clientSession;
    }

}
