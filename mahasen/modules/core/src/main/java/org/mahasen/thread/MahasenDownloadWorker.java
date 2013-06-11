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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.mahasen.MahasenConstants;
import org.mahasen.configuration.MahasenConfiguration;
import org.mahasen.exception.MahasenConfigurationException;
import org.mahasen.resource.MahasenResource;
import org.mahasen.ssl.SSLWrapper;
import org.mahasen.util.GetUtil;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

public class MahasenDownloadWorker implements Runnable {
    private static Log log = LogFactory.getLog(MahasenUploadWorker.class);
    private FileOutputStream fileOutputStream;
    private InputStream inputStream;
    private String partName;
    private String localIp;
    private MahasenResource mahasenResource;

    /**
     * @param partName
     * @param mahasenResource
     * @throws MahasenConfigurationException
     */
    public MahasenDownloadWorker(String partName, MahasenResource mahasenResource)
            throws MahasenConfigurationException {

        this.partName = partName;
        this.mahasenResource = mahasenResource;
        localIp = MahasenConfiguration.getInstance().getLocalIP().toString().substring(1);

    }

    public void run() {

        Hashtable<String, Vector<String>> iptable = mahasenResource.getSplittedPartsIpTable();
        String fileName = mahasenResource.getProperty(MahasenConstants.FILE_NAME).toString();
        String currentFilePartName = fileName + "." + partName;
        if (iptable.get(partName).contains(localIp)) {

            log.debug("Storing : " + fileName + "." + partName + " locally");
            FileInputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                inputStream = new FileInputStream(new File(MahasenConfiguration.getInstance().getRepositoryPath()
                        + fileName + "." + partName));
                outputStream = new FileOutputStream(new File(MahasenConfiguration.getInstance()
                        .getTempDownloadFolderPath()
                        +"/"+ fileName +"/"+ fileName + "." + partName));
                byte[] buffer = new byte[1024];

                int numRead;

                while ((numRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, numRead);
                }
                GetUtil.decrementStoredNoOfParts();

            } catch (Exception e) {
                log.error("Error occurred in storing part : " + partName, e);
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

        } else {
            for (int i = 0; i < iptable.get(partName).size(); i++) {

                String nodeIp = iptable.get(partName).get(i);
                URI uri = null;
                try {
                    ArrayList<NameValuePair> qparams = new ArrayList<NameValuePair>();
                    qparams.add(new BasicNameValuePair(MahasenConstants.FILE_NAME, currentFilePartName));
                    uri = URIUtils.createURI("https", nodeIp + ":" +
                            MahasenConstants.SERVER_PORT, -1, "/mahasen/download_request_ajaxprocessor.jsp",
                            URLEncodedUtils.format(qparams, "UTF-8"), null);
                } catch (URISyntaxException e) {
                    log.info("URI not found");
                    return;
                }

                HttpPost httppost = new HttpPost(uri);
                HttpClient downloadHttpClient = new DefaultHttpClient();
                downloadHttpClient = SSLWrapper.wrapClient(downloadHttpClient);

                try {
                    System.out.println("Executing Download request " + httppost.getRequestLine());

                    HttpResponse response = downloadHttpClient.execute(httppost);
                    HttpEntity httpEntity = response.getEntity();

                    if (httpEntity.getContentLength() > 0) {
                        fileOutputStream = new FileOutputStream(MahasenConfiguration.getInstance()
                                .getTempDownloadFolderPath()
                                +"/"+ fileName +"/"+ fileName + "." + partName);
                        inputStream = httpEntity.getContent();
                        byte[] buffer = new byte[1024];
                        int numRead;
                        while ((numRead = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, numRead);
                        }
                        GetUtil.decrementStoredNoOfParts();
                        System.out.println(response.getStatusLine());
                        if ((response.getStatusLine().getReasonPhrase().equals("OK")) &&
                                (response.getStatusLine().getStatusCode() == 200)) {
                            break;
                        }
                    }
                    if (httpEntity.getContentLength() <= 0) {
                        System.out.println("no content available");
                    }
                } catch (Exception e) {
                    log.error("Error occurred in downloading file : " + partName, e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }

    }


}

