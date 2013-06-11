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

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;

/**
 * The message type is responsible for removing the content in a remote node
 */
public class ContentRemoveMessage implements Message {

    private Id contentId;

    private NodeHandle nodeHandle;

    /**
     * @param contentId
     * @param nodeHandle
     */
    public ContentRemoveMessage(Id contentId, NodeHandle nodeHandle) {

        this.contentId = contentId;
        this.nodeHandle = nodeHandle;
    }

    /**
     * @return MEDIUM_PRIORITY
     */
    public int getPriority() {
        return MEDIUM_PRIORITY;
    }


    /**
     * @return
     */
    public String toString() {
        return "content remove message" + contentId;

    }

    /**
     * @return nodeHandle
     */
    public NodeHandle getNodeHandle() {
        return nodeHandle;
    }

    /**
     * @return contentId
     */
    public Id getId() {
        return contentId;
    }
}
