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

package org.mahasen.thread;

import org.mahasen.node.MahasenNodeManager;
import org.mahasen.node.MahasenPropertyPastContent;
import org.mahasen.resource.MahasenResource;
import rice.pastry.Id;

import java.util.Enumeration;
import java.util.Hashtable;


public class TreeUpdateWorker implements Runnable {

    private Hashtable<String, String> properties = null;
    private MahasenNodeManager nodeManager = MahasenNodeManager.getInstance();
    private boolean isToDelete = false;
    private MahasenResource resource = null;
    private Id resourceId = null;

    public TreeUpdateWorker(Hashtable<String, String> properties, Id resourceId, MahasenResource resource,
                            boolean isToDelete) {
        this.properties = properties;
        this.resourceId = resourceId;
        this.resource = resource;
        this.isToDelete = isToDelete;

    }

    public void run() {
        Enumeration e = properties.keys();

        while (e.hasMoreElements()) {


            String keyValue = e.nextElement().toString();
            String value = properties.get(keyValue);
            Id propertyTreeId = rice.pastry.Id.build(String.valueOf(keyValue.hashCode()));

            MahasenPropertyPastContent myContent = new MahasenPropertyPastContent(propertyTreeId, value,
                    resourceId, isToDelete, resource);
            nodeManager.insertPastContent(myContent);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                //Log the exception
            }

        }
    }
}
