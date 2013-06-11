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

package org.mahasen.client;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.mahasen.authentication.ClientLogin;
import org.mahasen.authentication.ClientLoginData;
import org.mahasen.configuration.ClientConfiguration;
import org.mahasen.exception.MahasenClientException;
import org.mahasen.ssl.WebClientSSLWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


public class Upload {
    ClientLogin clientLogin;
    HttpClient httpclient;
    ClientLoginData clientLoginData;
    List<NameValuePair> customProperties = new ArrayList<NameValuePair>();

    public Upload() {
    }

    /**
     * @param clientLoginData
     */
    public Upload(ClientLoginData clientLoginData) {
        this.clientLoginData = clientLoginData;
        //To change body of created methods use File | Settings | File Templates.
    }

    /**
     * @param uploadFile
     * @param tags
     * @param folderStructure
     * @param addedProperties
     * @throws IOException
     */
    public void upload(File uploadFile, String tags, String folderStructure, List<NameValuePair> addedProperties)
            throws IOException, MahasenClientException, URISyntaxException {
        httpclient = new DefaultHttpClient();

        if (addedProperties != null) {
            this.customProperties = addedProperties;
        }


        try {

            System.out.println(" Is Logged : " + clientLoginData.isLoggedIn());

            if (clientLoginData.isLoggedIn() == true) {

                httpclient = WebClientSSLWrapper.wrapClient(httpclient);

                File file = uploadFile;

                if (file.exists()) {

                    if(!folderStructure.equals("")){
                    customProperties.add(new BasicNameValuePair("folderStructure", folderStructure));
                    }
                    customProperties.add(new BasicNameValuePair("fileName", file.getName()));
                    customProperties.add(new BasicNameValuePair("tags", tags));

                    URI uri = URIUtils.createURI("https", clientLoginData.getHostNameAndPort(), -1,
                            "/mahasen/upload_ajaxprocessor.jsp",
                            URLEncodedUtils.format(customProperties, "UTF-8"), null);

                    HttpPost httppost = new HttpPost(uri);

                    InputStreamEntity reqEntity = new InputStreamEntity(
                            new FileInputStream(file), -1);
                    reqEntity.setContentType("binary/octet-stream");
                    reqEntity.setChunked(true);

                    httppost.setEntity(reqEntity);

                    httppost.setHeader("testHeader", "testHeadervalue");

                    System.out.println("executing request " + httppost.getRequestLine());
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity resEntity = response.getEntity();

                    System.out.println("----------------------------------------");
                    System.out.println(response.getStatusLine());

                    EntityUtils.consume(resEntity);

                    if (response.getStatusLine().getStatusCode() == 900) {
            throw new MahasenClientException(String.valueOf(response.getStatusLine()));
        }

                }
            } else {
                System.out.println("User has to be logged in to perform this function");
            }
        }  finally
        {
            httpclient.getConnectionManager().shutdown();
        }

    }

    /**
     * @param key
     * @param value
     */
    @Deprecated
    public void setProperty(String key, String value) {
        customProperties.add(new BasicNameValuePair(key, value));
    }

    /**
     * @param addedProperties
     */
    public void setCustomProperties(List<NameValuePair> addedProperties) {
        this.customProperties = addedProperties;
    }
}
