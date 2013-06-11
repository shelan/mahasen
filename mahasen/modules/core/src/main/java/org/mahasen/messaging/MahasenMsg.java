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

package org.mahasen.messaging;

import rice.p2p.commonapi.Message;
import rice.pastry.NodeHandle;


public class MahasenMsg implements Message {

    private NodeHandle targetNodeHandle;

    private NodeHandle sendersNodeHandle;

    private String ip;

    private boolean isRequest = true;

    /**
     * @return
     */
    public int getPriority() {
        return Message.MEDIUM_PRIORITY;
    }

    /**
     * @return
     */
    public NodeHandle getTargetNodeHandle() {
        return targetNodeHandle;
    }

    /**
     * @param targetNodeHandle
     */
    public void setTargetNodeHandle(NodeHandle targetNodeHandle) {
        this.targetNodeHandle = targetNodeHandle;
    }

    /**
     * @return sendersNodeHandle
     */
    public NodeHandle getSendersNodeHandle() {
        return sendersNodeHandle;
    }

    /**
     * @param sendersNodeHandle
     */
    public void setSendersNodeHandle(NodeHandle sendersNodeHandle) {
        this.sendersNodeHandle = sendersNodeHandle;
    }

    /**
     * @return
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return
     */
    public boolean isRequest() {
        return isRequest;
    }

    /**
     * @param request
     */
    public void setRequest(boolean request) {
        isRequest = request;
    }
}
