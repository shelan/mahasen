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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mahasen.node.MahasenNodeManager;
import org.mahasen.resource.MahasenResource;
import org.mahasen.ssl.SSLWrapper;
import org.mahasen.util.PutUtil;
import rice.p2p.past.PastException;
import rice.pastry.Id;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;


public class MahasenReplicateWorker implements Runnable {
    private static Log log = LogFactory.getLog(MahasenUploadWorker.class);
    private String currentPart;
    private File file;
    private MahasenResource mahasenResource;
    private String nodeIp;
    private URI uri;
    private MahasenNodeManager nodeManager;
    private Id parentFileId;

    /**
     * @param uri
     * @param currentPartName
     * @param file
     * @param mahasenResource
     * @param nodeIp
     * @param nodeManager
     * @param parentFileId
     */
    public MahasenReplicateWorker(URI uri, String currentPartName, File file, MahasenResource mahasenResource,
                                  String nodeIp, MahasenNodeManager nodeManager, Id parentFileId) {
        this.uri = uri;
        this.file = file;
        this.mahasenResource = mahasenResource;
        this.nodeIp = nodeIp;
        this.currentPart = currentPartName;
        this.nodeManager = nodeManager;
        this.parentFileId = parentFileId;

    }

    public void run() {
        HttpClient uploadHttpClient = new DefaultHttpClient();

        uploadHttpClient = SSLWrapper.wrapClient(uploadHttpClient);

        if (file.exists()) {
            HttpPost httppost = new HttpPost(uri);

            try {
                InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(file), -1);

                reqEntity.setContentType("binary/octet-stream");
                reqEntity.setChunked(true);
                httppost.setEntity(reqEntity);
                System.out.println("Executing Replicate request " + httppost.getRequestLine());

                HttpResponse response = uploadHttpClient.execute(httppost);

                System.out.println("Replicate worker----------------------------------------");
                System.out.println(response.getStatusLine());

                if ((response.getStatusLine().getReasonPhrase().equals("OK")) &&
                        (response.getStatusLine().getStatusCode() == 200)) {
                    log.debug(currentPart + " was replicated at " + nodeIp);
                    //PutUtil.incrementNoOfReplicas(currentPart);
                    mahasenResource.addSplitPartStoredIp(currentPart, nodeIp);
                    try {
                        nodeManager.insertIntoDHT(parentFileId, mahasenResource, true);
                    } catch (InterruptedException e) {
                        log.error("Interrupted while updating replicated file's metadata");
                    } catch (PastException e) {
                        log.error("Error while updating replicated file ID:" + mahasenResource.getId() + "medatada");
                    }
                }

            } catch (IOException e) {
                log.error("Error occurred in URL connection");
            }
        }
    }
}
