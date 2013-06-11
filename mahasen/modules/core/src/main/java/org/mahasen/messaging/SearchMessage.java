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
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.p2p.past.messaging.ContinuationMessage;

import java.io.IOException;

public class SearchMessage extends ContinuationMessage {

    private Id id;

    /**
     * @param uid
     * @param id
     * @param source
     * @param dest
     */
    public SearchMessage(int uid, Id id, NodeHandle source, Id dest) {
        super(uid, source, dest);

        this.id = id;
    }

    /**
     * @param buf
     * @throws IOException
     */
    @Override
    public void serialize(OutputBuffer buf) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * @return
     */
    public short getType() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * @return
     */
    public Id getId() {
        return id;
    }

}
