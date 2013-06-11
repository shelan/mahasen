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
package org.mahasen.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.mahasen.MahasenConstants;
import org.mahasen.exception.MahasenConfigurationException;
import org.mahasen.exception.MahasenException;
import org.mahasen.resource.MahasenResource;
import org.mahasen.thread.MahasenDeleteWorker;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import rice.pastry.Id;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class DeleteUtil extends AbstractCommonUtil {
    private static Log log = LogFactory.getLog(PutUtil.class);
    private static AtomicInteger storedNoOfParts = new AtomicInteger(0);

    /**
     * @param fileToDelete
     * @throws MalformedURLException
     * @throws RegistryException
     */
    public void delete(String fileToDelete)
            throws MalformedURLException, RegistryException, MahasenConfigurationException, MahasenException {
        Id resourceId = Id.build(String.valueOf(fileToDelete.hashCode()));
        String currentFileName = null;

        try {
            MahasenResource mahasenResource = mahasenManager.lookupDHT(resourceId);

            if (mahasenManager.lookupDHT(resourceId) == null) {
                throw new MahasenException("File not found");
            }

            String fileName = mahasenResource.getProperty(MahasenConstants.FILE_NAME).toString();
            Hashtable<String, Vector<String>> iptable = mahasenResource.getSplittedPartsIpTable();

            int totalNoOfParts = 0;
            for (String partName : mahasenResource.getPartNames()) {
                totalNoOfParts += iptable.get(partName).size();
            }

            setStoredNoOfParts(totalNoOfParts);

            for (String partName : mahasenResource.getPartNames()) {
                currentFileName = fileName + "." + partName;
                for (int i = 0; i < iptable.get(partName).size(); i++) {
                    String nodeIp = iptable.get(partName).get(i);

                    ArrayList<NameValuePair> qparams = new ArrayList<NameValuePair>();
                    qparams.add(new BasicNameValuePair(MahasenConstants.FILE_NAME, fileName + "." + partName));

                    URI uri = null;
                    try {
                        uri = URIUtils.createURI("https", nodeIp + ":" +
                                MahasenConstants.SERVER_PORT, -1, "/mahasen/delete_request_ajaxprocessor.jsp",
                                URLEncodedUtils.format(qparams, "UTF-8"), null);
                        MahasenDeleteWorker mahasenDeleteWorker = new MahasenDeleteWorker(uri);
                        Thread deleteThread = new Thread(mahasenDeleteWorker);
                        deleteThread.start();

                    } catch (URISyntaxException e) {
                        log.info("URI not found");
                        return;
                    }
                }
            }

            final BlockFlag blockFlag = new BlockFlag(true, 1500);
            while (true) {

                if (storedNoOfParts.intValue() == 0) {
                    MahasenResource resourceToDelete = mahasenManager.lookupDHT(resourceId);
                    mahasenManager.deletePropertyFromTreeMap(resourceId, resourceToDelete);
                    mahasenManager.deleteContent(resourceId);
                    blockFlag.unblock();
                    break;
                }

                if (blockFlag.isBlocked()) {

                    mahasenManager.getNode().getEnvironment().getTimeSource().sleep(10);
                } else {
                    throw new MahasenException("Time out in delete operation for " + fileName);
                }
            }
        } catch (InterruptedException e) {
            log.error("Error deleting file : " + currentFileName);
        }
    }

    /**
     * @param numberOfParts
     */
    public void setStoredNoOfParts(int numberOfParts) {
        storedNoOfParts.set(numberOfParts);
    }

    /**
     *
     */
    public static void decrementStoredNoOfParts() {
        storedNoOfParts.decrementAndGet();
    }
}

