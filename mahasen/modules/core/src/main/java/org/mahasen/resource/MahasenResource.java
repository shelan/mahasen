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
package org.mahasen.resource;

import rice.p2p.commonapi.Id;

import java.io.Serializable;
import java.util.*;


public class MahasenResource implements Serializable {

    private Id resourceId;

    private Vector<String> tags = new Vector<String>();

    private Hashtable<String, String> property = new Hashtable<String, String>();

    private int fileSize;

    private int uploadedDate;

    private Hashtable<String, Vector<String>> splitPartsStoredIps = new Hashtable<String, Vector<String>>();

    private Vector<String> partNames = new Vector<String>();

    private boolean hasParts = false;

    /**
     * @param resourceID
     */
    public MahasenResource(Id resourceID) {

        this.resourceId = resourceID;
    }

    /**
     * @param key
     * @param value
     */
    public void addProperty(String key, String value) {

        property.put(key, value);
    }

    /**
     * @param key
     * @return
     */
    public Object getProperty(String key) {

        return property.get(key);

    }

    /**
     * @return
     */
    public Hashtable<String, String> getProperties() {

        return property;
    }

    /**
     * @param tag
     */
    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }

    }

    /**
     * @return
     */
    public Vector<String> getTags() {

        return tags;

    }

    /**
     * @param propeties this method replaces the values for already existing keys
     */
    public void addAllPropeties(Hashtable<String, String> propeties) {
        this.property.putAll(propeties);
    }

    /**
     * @return
     */
    public Id getId() {

        return resourceId;
    }

    /**
     * @param partName
     * @param ip
     */
    public synchronized void addSplitPartStoredIp(String partName, String ip) {


        if (splitPartsStoredIps.containsKey(partName)) {

            if (!splitPartsStoredIps.get(partName).contains(ip)) {
                splitPartsStoredIps.get(partName).add(ip);
            }

        }
        if (!splitPartsStoredIps.containsKey(partName)) {
            Vector<String> ips = new Vector<String>();
            ips.add(ip);
            splitPartsStoredIps.put(partName, ips);
        }
    }

    /**
     * @return
     */
    public Hashtable<String, Vector<String>> getSplittedPartsIpTable() {
        return splitPartsStoredIps;
    }

    /**
     * @param partNameArray
     */
    public synchronized void addPartNames(String partNameArray[]) {
        for (String partName : partNameArray)
            if (!partNames.contains(partName)) {
                partNames.add(partName);
            }
    }

    /**
     * @return
     */
    public Vector<String> getPartNames() {
        return partNames;
    }

    /**
     * @return
     */
    public int getFileSize() {
        return fileSize;
    }

    /**
     * @param fileSize
     */
    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * @return
     */
    public int getUploadedDate() {
        return uploadedDate;
    }

    /**
     * @param uploadedDate
     */
    public void setUploadedDate(int uploadedDate) {
        this.uploadedDate = uploadedDate;
    }
}
