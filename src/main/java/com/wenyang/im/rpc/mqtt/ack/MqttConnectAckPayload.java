/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.wenyang.im.rpc.mqtt.ack;

import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.util.internal.StringUtil;

/**
 * Payload of {@link MqttConnectMessage}
 */
public final class MqttConnectAckPayload {

    private final byte[] data;

    public MqttConnectAckPayload(
            byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return new StringBuilder(StringUtil.simpleClassName(this))
            .append('[')
            .append("data=").append(data)
            .append(']')
            .toString();
    }
}
