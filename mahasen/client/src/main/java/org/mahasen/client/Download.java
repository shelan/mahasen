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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.mahasen.authentication.ClientLogin;
import org.mahasen.authentication.ClientLoginData;
import org.mahasen.configuration.ClientConfiguration;
import org.mahasen.exception.MahasenClientException;
import org.mahasen.ssl.WebClientSSLWrapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


public class Download {

    ClientLogin clientLogin;
    HttpClient httpclient;
    ClientLoginData clientLoginData;

    /**
     *
     */
    public Download() {
    }

    /**
     * @param clientLoginData
     */
    public Download(ClientLoginData clientLoginData) {
        this.clientLoginData = clientLoginData;

    }

    /**
     * @param fileName
     * @throws IOException
     */
    public void download(String fileName) throws IOException, MahasenClientException, URISyntaxException {
        httpclient = new DefaultHttpClient();
        OutputStream outputStream = null;

        ClientConfiguration clientConfiguration = ClientConfiguration.getInstance();


        try {
            String userName = clientLoginData.getUserName();
            String passWord = clientLoginData.getPassWord();
            String hostAndPort = clientLoginData.getHostNameAndPort();
            String userId = clientLoginData.getUserId(userName, passWord);
            Boolean isLogged = clientLoginData.isLoggedIn();
            System.out.println(" Is Logged : " + isLogged);

            if (isLogged == true) {
                httpclient = WebClientSSLWrapper.wrapClient(httpclient);

                List<NameValuePair> qparams = new ArrayList<NameValuePair>();
                qparams.add(new BasicNameValuePair("fileName", fileName));

                URI uri = URIUtils.createURI("https", hostAndPort, -1, "/mahasen/download_ajaxprocessor.jsp",
                        URLEncodedUtils.format(qparams, "UTF-8"), null);

                HttpPost httppost = new HttpPost(uri);

                System.out.println("executing request " + httppost.getRequestLine());
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();

                if (httpEntity.getContentLength() > 0) {
                    outputStream = new FileOutputStream(clientConfiguration.getDownloadRepo() + "/" + fileName);
                    httpEntity.writeTo(outputStream);
                } else {
                    System.out.println("no content available");
                }

                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                if (httpEntity != null) {
                    System.out.println("Response content length: " + httpEntity.getContentLength());
                    System.out.println("Chunked?: " + httpEntity.isChunked());
                }
                EntityUtils.consume(httpEntity);

                if (response.getStatusLine().getStatusCode() == 900) {
                    throw new MahasenClientException(String.valueOf(response.getStatusLine()));
                }
            } else {
                System.out.println("User has to be logged in to perform this function");
            }
        } finally {
            httpclient.getConnectionManager().shutdown();
        }

    }

    public void download(String fileName, String downloadRepo) throws IOException, MahasenClientException, URISyntaxException {
        httpclient = new DefaultHttpClient();
        OutputStream outputStream = null;


        try {
            String userName = clientLoginData.getUserName();
            String passWord = clientLoginData.getPassWord();
            String hostAndPort = clientLoginData.getHostNameAndPort();
            String userId = clientLoginData.getUserId(userName, passWord);
            Boolean isLogged = clientLoginData.isLoggedIn();
            System.out.println(" Is Logged : " + isLogged);

            if (isLogged == true) {
                httpclient = WebClientSSLWrapper.wrapClient(httpclient);

                List<NameValuePair> qparams = new ArrayList<NameValuePair>();
                qparams.add(new BasicNameValuePair("fileName", fileName));

                URI uri = URIUtils.createURI("https", hostAndPort, -1, "/mahasen/download_ajaxprocessor.jsp",
                        URLEncodedUtils.format(qparams, "UTF-8"), null);

                HttpPost httppost = new HttpPost(uri);

                System.out.println("executing request " + httppost.getRequestLine());
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity httpEntity = response.getEntity();

                if (httpEntity.getContentLength() > 0) {
                    outputStream = new FileOutputStream(downloadRepo + "/" + fileName);
                    httpEntity.writeTo(outputStream);
                } else {
                    System.out.println("no content available");
                }

                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                if (httpEntity != null) {
                    System.out.println("Response content length: " + httpEntity.getContentLength());
                    System.out.println("Chunked?: " + httpEntity.isChunked());
                }
                EntityUtils.consume(httpEntity);

                if (response.getStatusLine().getStatusCode() == 900) {
                    throw new MahasenClientException(String.valueOf(response.getStatusLine()));
                }
            } else {
                System.out.println("User has to be logged in to perform this function");
            }
        } finally {
            httpclient.getConnectionManager().shutdown();
        }

    }
}
