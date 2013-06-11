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

import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.p2p.past.messaging.ContinuationMessage;
import rice.p2p.past.rawserialization.PastContentDeserializer;
import rice.p2p.past.rawserialization.RawPastContent;

import java.io.IOException;

public class MahasenSearchMessage extends ContinuationMessage {

    public static final short TYPE = 7;

    private Id id;

    private boolean isRangeBased = false;

    private boolean isNumOfResults = false;

    private String propertyName;

    private String propertyValue;

    private String intialVal;

    private String lastVal;

    /**
     * @param uid
     * @param id
     * @param source
     * @param dest
     */
    public MahasenSearchMessage(int uid, Id id, NodeHandle source, Id dest) {
        super(uid, source, dest);
        this.id = id;
    }

    /**
     * @param buf
     * @throws IOException
     */
    @Override
    public void serialize(OutputBuffer buf) throws IOException {

        System.out.println("Serializing search message :" + toString());

        buf.writeByte((byte) 0); // version
        if (response != null && response instanceof RawPastContent) {
            super.serialize(buf, false);
            RawPastContent rpc = (RawPastContent) response;
            buf.writeShort(rpc.getType());
            rpc.serialize(buf);
        } else {
            super.serialize(buf, true);
        }
        buf.writeShort(id.getType());
        id.serialize(buf);
        buf.writeBoolean(isRangeBased());
        buf.writeBoolean(isNumOfResults());

        buf.writeBoolean(getPropertyName() != null);
        if (propertyName != null)
            buf.writeUTF(getPropertyName());

        buf.writeBoolean(getPropertyValue() != null);
        if (propertyValue != null)
            buf.writeUTF(propertyValue);

        buf.writeBoolean(getIntialVal() != null);
        if (intialVal != null)
            buf.writeUTF(intialVal);

        buf.writeBoolean(getLastVal() != null);
        if (lastVal != null)
            buf.writeUTF(lastVal);


    }

    /**
     * @return
     */
    public short getType() {
        return TYPE;
    }

    /**
     * @return
     */
    public Id getId() {
        return id;
    }

    /**
     * @param buf
     * @param endpoint
     * @param pcd
     * @throws IOException
     */
    public static MahasenSearchMessage build(InputBuffer buf, Endpoint endpoint, PastContentDeserializer pcd)
            throws IOException {
        byte version = buf.readByte();
        switch (version) {
            case 0:
                return new MahasenSearchMessage(buf, endpoint, pcd);
            default:
                throw new IOException("Unknown Version: " + version);
        }
    }

    /**
     * @param buf
     * @param endpoint
     * @param pcd
     * @throws IOException
     */
    private MahasenSearchMessage(InputBuffer buf, Endpoint endpoint, PastContentDeserializer pcd) throws IOException {
        super(buf, endpoint);
        if (serType == S_SUB) {
            short contentType = buf.readShort();
            response = pcd.deserializePastContent(buf, endpoint, contentType);
        }

        try {
            id = endpoint.readId(buf, buf.readShort());
        } catch (IllegalArgumentException iae) {
            System.out.println(iae + " " + this + " serType:" + serType + " UID:" + getUID() + " d:" +
                    dest + " s:" + source);
            throw iae;
        }

        setRangeBased(buf.readBoolean());
        setNumOfResults(buf.readBoolean());
        if (buf.readBoolean())
            setPropertyName(buf.readUTF());
        if (buf.readBoolean())
            setPropertyValue(buf.readUTF());
        if (buf.readBoolean())
            setIntialVal(buf.readUTF());
        if (buf.readBoolean())
            setLastVal(buf.readUTF());

        System.out.println("Building search message :" + toString());
    }

    /**
     * @return
     */
    public boolean isRangeBased() {
        return isRangeBased;
    }

    /**
     * @param rangeBased
     */
    public void setRangeBased(boolean rangeBased) {
        isRangeBased = rangeBased;
    }

    /**
     * @return
     */
    public boolean isNumOfResults() {
        return isNumOfResults;
    }

    /**
     * @param numOfResults
     */
    public void setNumOfResults(boolean numOfResults) {
        isNumOfResults = numOfResults;
    }

    /**
     * @return
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @param propertyName
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @return
     */
    public String getPropertyValue() {
        return propertyValue;
    }

    /**
     * @param propertyValue
     */
    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    /**
     * @return
     */
    public String getIntialVal() {
        return intialVal;
    }

    /**
     * @param intialVal
     */
    public void setIntialVal(String intialVal) {
        this.intialVal = intialVal;
    }

    /**
     * @return
     */
    public String getLastVal() {
        return lastVal;
    }

    /**
     * @param lastVal
     */
    public void setLastVal(String lastVal) {
        this.lastVal = lastVal;
    }

    @Override
    public String toString() {
        return "search message [" +
                "property value :" + propertyValue + "\n"
                + "property name :" + propertyName + "\n"
                + "initial value :" + intialVal + "\n"
                + "last value    :" + lastVal + "\n"
                + "isRanged based:" + isRangeBased + "\n"
                + "isNoOfResults :" + isNumOfResults + "]";

    }
}
