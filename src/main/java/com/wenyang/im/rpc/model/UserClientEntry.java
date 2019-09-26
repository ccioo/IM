package com.wenyang.im.rpc.model;

import java.io.Serializable;

public class UserClientEntry implements Serializable {

    public String userId;
    public String clientId;

    public UserClientEntry(String userId, String clientId) {
        this.userId = userId;
        this.clientId = clientId;
    }


}
