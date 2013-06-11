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

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.mahasen.configuration.MahasenConfiguration;
import org.mahasen.exception.MahasenConfigurationException;
import org.mahasen.resource.MahasenResource;
import org.mahasen.ssl.SSLWrapper;

import org.mahasen.util.PutUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Worker thread that responsible for uploading the file and making replicas
 */
public class MahasenUploadWorker implements Runnable {
    private static Log log = LogFactory.getLog(MahasenUploadWorker.class);
    private String currentPart;
    private File file;
    private MahasenResource mahasenResource;
    private String nodeIp;
    private URI uri;
    private String localIp;
    private Long threadId;
    private String jobId;

    /**
     * @param uri
     * @param currentPartName
     * @param file
     * @param mahasenResource
     * @param nodeIp
     * @throws MahasenConfigurationException
     */
    public MahasenUploadWorker(URI uri, String currentPartName, File file, MahasenResource mahasenResource,
                               String nodeIp) throws MahasenConfigurationException {
        this.uri = uri;
        this.file = file;
        this.mahasenResource = mahasenResource;
        this.nodeIp = nodeIp;
        this.currentPart = currentPartName;
        localIp = MahasenConfiguration.getInstance().getLocalIP().toString().substring(1);
    }

    public void run() {
        HttpClient uploadHttpClient = new DefaultHttpClient();

        uploadHttpClient = SSLWrapper.wrapClient(uploadHttpClient);

        if (file.exists()) {
            if (!nodeIp.equals(localIp)) {
                HttpPost httppost = new HttpPost(uri);

                try {
                    InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(file), -1);

                    reqEntity.setContentType("binary/octet-stream");
                    reqEntity.setChunked(true);
                    httppost.setEntity(reqEntity);
                    System.out.println("Executing Upload request " + httppost.getRequestLine());

                    HttpResponse response = uploadHttpClient.execute(httppost);

                    System.out.println("----------------------------------------");
                    System.out.println(response.getStatusLine());

                    if ((response.getStatusLine().getReasonPhrase().equals("OK")) &&
                            (response.getStatusLine().getStatusCode() == 200)) {
                        mahasenResource.addSplitPartStoredIp(currentPart, nodeIp);
                        PutUtil.storedNoOfParts.get(jobId).getAndIncrement();
                    }

                } catch (IOException e) {
                    log.error("Error occurred in Uploading part : " + currentPart, e);
                }
            } else {
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    File filePart = new File(MahasenConfiguration.getInstance().getRepositoryPath() + file.getName());
                    FileOutputStream outputStream = new FileOutputStream(filePart);
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                    outputStream.close();
                    mahasenResource.addSplitPartStoredIp(currentPart, nodeIp);
                    PutUtil.storedNoOfParts.get(jobId).getAndIncrement();

                } catch (Exception e) {
                    log.error("Error while storing file " + currentPart + " locally", e);
                }
            }
        }
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
