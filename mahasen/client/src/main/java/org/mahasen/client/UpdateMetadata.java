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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


public class UpdateMetadata {

    ClientLogin clientLogin;
    HttpClient httpclient;
    List<NameValuePair> properties = new ArrayList<NameValuePair>();
    ClientLoginData clientLoginData;

    /**
     * @param clientLoginData
     */
    public UpdateMetadata(ClientLoginData clientLoginData) {
        this.clientLoginData = clientLoginData;
    }



    /**
     * @param fileToUpdate
     * @param tags
     * @throws URISyntaxException
     * @throws AuthenticationExceptionException
     *
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public void updateMetadata(String fileToUpdate, String tags)
            throws URISyntaxException, AuthenticationExceptionException,
            IOException, UnsupportedEncodingException, MahasenClientException {

        httpclient = new DefaultHttpClient();
        String userName = clientLoginData.getUserName();
        String passWord = clientLoginData.getPassWord();
        String hostAndPort = clientLoginData.getHostNameAndPort();
        String userId = clientLoginData.getUserId(userName, passWord);
        Boolean isLogged = clientLoginData.isLoggedIn();
        System.out.println(" Is Logged : " + isLogged);

        if (isLogged == true) {
            httpclient = WebClientSSLWrapper.wrapClient(httpclient);

            // this will add file name and tags to user defined property list
            properties.add(new BasicNameValuePair("fileName", fileToUpdate));
            properties.add(new BasicNameValuePair("tags", tags));

            URI uri = URIUtils.createURI("https", hostAndPort, -1, "/mahasen/update_ajaxprocessor.jsp",
                    URLEncodedUtils.format(properties, "UTF-8"), null);

            HttpPost httpPost = new HttpPost(uri);

            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();

            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());

            EntityUtils.consume(resEntity);
            if (response.getStatusLine().getStatusCode() == 900) {
            throw new MahasenClientException(String.valueOf(response.getStatusLine()));
            }


        }

        else{
            System.out.println("User has to be logged in to perform this function");
        }

        httpclient.getConnectionManager().shutdown();

    }

    /**
     * @param key
     * @param value
     */
    public void setProperty(String key, String value) {
        properties.add(new BasicNameValuePair(key, value));
    }

    /**
     * @param addedProperties
     */
    public void setAddedProperties(List<NameValuePair> addedProperties) {
        this.properties = addedProperties;
    }

}
