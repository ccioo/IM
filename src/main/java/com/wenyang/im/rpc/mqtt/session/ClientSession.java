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

package com.wenyang.im.rpc.mqtt.session;

import com.wenyang.im.rpc.distributed.DistributeSessionackStore;
import com.wenyang.im.rpc.distributed.IMessagesStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model a Session like describe on page 25 of MQTT 3.1.1 specification:
 * The Session state in the BootstrapServer consists of:
 * <ul>
 * <li>The existence of a Session, even if the rest of the Session state is empty.</li>
 * <li>The Client’s subscriptions.</li>
 * <li>QoS 1 and QoS 2 messages which have been sent to the Client, but have not been
 * completely acknowledged.</li>
 * <li>QoS 1 and QoS 2 messages pending transmission to the Client.</li>
 * <li>QoS 2 messages which have been received from the Client, but have not been
 * completely acknowledged.</li>
 * <li>Optionally, QoS 0 messages pending transmission to the Client.</li>
 * </ul>
 */
public class ClientSession {

    class OutboundFlightZone {

        /**
         * Save the binding messageID, clientID - message
         *
         * @param messageID the packet ID used in transmission
         * @param msg       the message to put in flight zone
         */
        void waitingAck(int messageID, IMessagesStore.StoredMessage msg) {
            if (LOG.isTraceEnabled()) {
            }
            distributeSessionackStore.inFlight(ClientSession.this.clientID, messageID, msg);
        }

        IMessagesStore.StoredMessage acknowledged(int messageID) {
            if (LOG.isTraceEnabled())
                LOG.trace("Acknowledging inflight, clientID <{}> messageID {}", ClientSession.this.clientID, messageID);
            return distributeSessionackStore.inFlightAck(ClientSession.this.clientID, messageID);
        }
    }

    class InboundFlightZone {

        public IMessagesStore.StoredMessage lookup(int messageID) {
            return distributeSessionackStore.inboundInflight(clientID, messageID);
        }

        public void waitingRel(int messageID, IMessagesStore.StoredMessage msg) {
            distributeSessionackStore.markAsInboundInflight(clientID, messageID, msg);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ClientSession.class);

    public final String clientID;

    private final DistributeSessionackStore distributeSessionackStore;

    private final OutboundFlightZone outboundFlightZone;
    private final InboundFlightZone inboundFlightZone;

    public ClientSession(String clientID, DistributeSessionackStore sessionsStore) {
        this.clientID = clientID;
        this.distributeSessionackStore = sessionsStore;
        this.outboundFlightZone = new OutboundFlightZone();
        this.inboundFlightZone = new InboundFlightZone();
    }


    @Override
    public String toString() {
        return "ClientSession{clientID='" + clientID + '\'' + "}";
    }


    public void disconnect(boolean cleanSession) {
        if (cleanSession) {
            LOG.info("Client disconnected. Removing its subscriptions. ClientId={}", clientID);
        }
    }


    public IMessagesStore.StoredMessage inFlightAcknowledged(int messageID) {
        return outboundFlightZone.acknowledged(messageID);
    }

    /**
     * Mark the message identified by guid as publish in flight.
     *
     * @return the packetID for the message in flight.
     */
    public int inFlightAckWaiting(IMessagesStore.StoredMessage msg) {
        LOG.debug("Adding message ot inflight zone. ClientId={}", clientID);
        int messageId = ClientSession.this.nextPacketId();
        outboundFlightZone.waitingAck(messageId, msg);
        return messageId;
    }

    public int nextPacketId() {
        return this.distributeSessionackStore.nextPacketID(this.clientID);
    }
}


