/*
 * Copyright (c) 2012-2017 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package com.wenyang.im.rpc.constants;

/**
 * Server constants keeper
 */
public final class Constants {

    public static final String ATTR_CLIENTID = "ClientID";
    public static final String CLEAN_SESSION = "cleanSession";
    public static final String KEEP_ALIVE = "keepAlive";
    //客户端获取到的历史消息数目
    public static final int MAX_MESSAGE_QUEUE = 1024;
    //客户端获取到的历史聊天室消息
    public static final int MAX_CHATROOM_MESSAGE_QUEUE = 256;

    private Constants() {
    }
}
