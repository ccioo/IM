package com.wenyang.im.rpc.distributed;


import com.wenyang.im.rpc.mqtt.session.ClientSession;
import com.wenyang.im.rpc.mqtt.session.ServerSession;

/**
 * @Author wen.yang
 */
public interface DBSessionStore {


    /**
     * 获取session
     *
     * @param uid
     * @param clientId
     * @param clientSession
     * @return
     */
    ServerSession getSession(String uid, String clientId, ClientSession clientSession);

    /**
     *
     *
     * @param clientId
     * @param uid
     * @param clientSession
     * @return
     */
    ServerSession createSession(String clientId, String uid, ClientSession clientSession);


}
