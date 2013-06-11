/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.mahasen;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mahasen.configuration.MahasenConfiguration;
import org.mahasen.exception.MahasenConfigurationException;
import org.mahasen.messaging.MahasenMsg;
import org.mahasen.messaging.NodeHandleRequestMessage;
import rice.p2p.commonapi.*;

import java.util.Vector;

//~--- JDK imports ------------------------------------------------------------

public class MahasenPastryApp implements Application {
    private static Log log = LogFactory.getLog(MahasenPastryApp.class);
    private Vector<String> resultIpVector = new Vector<String>();

    /**
     * The Endpoint represents the underlieing node.  By making calls on the
     * Endpoint, it assures that the message will be delivered to a MyApp on whichever
     * node the message is intended for.
     */
    protected Endpoint endpoint;

    /**
     * @param node
     */
    public MahasenPastryApp(Node node) {

        // We are only going to use one instance of this application on each PastryNode
        this.endpoint = node.buildEndpoint(this, "mahaseninstance");

        // the rest of the initialization code could go here
        // now we can receive messages
        this.endpoint.register();
    }

    /**
     * @param routeMessage
     * @return
     */
    public boolean forward(RouteMessage routeMessage) {
        return true;
    }

    /**
     * @param nodeHandle
     * @param msg
     */
    public void sendRequestForIp(NodeHandle nodeHandle, Message msg) {
        log.info("sending message to " + nodeHandle.toString());

        // this.endpoint.route(id, nodeHandleRequestMessage, null);
        this.endpoint.route(nodeHandle.getId(), msg, nodeHandle);
    }

    /**
     * @param id
     * @param message
     */
    public void deliver(Id id, Message message) {
        if (message instanceof MahasenMsg) {
            MahasenMsg msg = (MahasenMsg) message;

            if (msg.isRequest()) {
                log.info("receiving message from " + id);

                try {
                    String ip = MahasenConfiguration.getInstance().getLocalIP().toString().replace("/", "");

                    msg.setIp(ip);
                    msg.setRequest(false);
                    endpoint.route(null, msg, msg.getSendersNodeHandle());
                } catch (MahasenConfigurationException e) {
                    log.error(" Error while routing the message to " + msg.getSendersNodeHandle());
                }
            } else {
                getResultIpVector().add(msg.getIp());
            }
        } else if (message instanceof NodeHandleRequestMessage) {
            NodeHandleRequestMessage msg = (NodeHandleRequestMessage) message;

            if (msg.isRequest()) {
                msg.setRequest(false);
                msg.setReciversNodeHandle((rice.pastry.NodeHandle) endpoint.getLocalNodeHandle());
                endpoint.route(null, msg, msg.getSendersNodeHandle());
            } else {

                // recievers node handle is the original recivers nodehandle which we have to send the message directly
                endpoint.route(null, msg.getMsgTosend(), msg.getReciversNodeHandle());
            }
        }
    }

    /**
     * @param nodeHandle
     * @param joined
     */
    public void update(NodeHandle nodeHandle, boolean joined) {
        if (joined) {
            log.info("Update :" + nodeHandle.getId() + "  joined the ring");
        }

        if (!joined) {
            log.info("Update :" + nodeHandle.getId() + "  left the ring");
        }
    }

    /**
     * @return
     */
    public boolean isResultAvailable() {
        return !(getResultIpVector().isEmpty());
    }

    /**
     * @return
     */
    public int noOfResults() {
        return getResultIpVector().size();
    }

    /**
     * @return
     */
    public Vector<String> getResultIpVector() {
        return resultIpVector;
    }
}

