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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;


public class Search {

    ClientLogin clientLogin;
    HttpClient httpclient;
    ClientLoginData clientLoginData;

    /**
     *
     */
    public Search() {
    }

    /**
     * @param clientLoginData
     */
    public Search(ClientLoginData clientLoginData) {
        this.clientLoginData = clientLoginData;

    }

    /**
     * @param propertyName
     * @param propertyValue
     * @param hostIP
     * @return
     * @throws URISyntaxException
     * @throws AuthenticationExceptionException
     *
     * @throws MahasenClientException
     * @throws IOException
     */
    public String search(String propertyName, String propertyValue, String hostIP)
            throws URISyntaxException, AuthenticationExceptionException, MahasenClientException, IOException {

        HttpResponse response = sendRequest(propertyName, false, propertyValue, null, hostIP);

        return getEntityContent(response);

    }

    /**
     * @param propertyName
     * @param initialValue
     * @param lastValue
     * @param hostIP
     * @return
     * @throws URISyntaxException
     * @throws AuthenticationExceptionException
     *
     * @throws MahasenClientException
     * @throws IOException
     */
    public String rangeSearch(String propertyName, String initialValue, String lastValue, String hostIP)
            throws URISyntaxException, AuthenticationExceptionException, MahasenClientException, IOException {

        HttpResponse response = sendRequest(propertyName, true, initialValue, lastValue, hostIP);

        return getEntityContent(response);
    }

    /**
     * @param tag
     * @param hostIp
     * @return
     * @throws URISyntaxException
     * @throws AuthenticationExceptionException
     *
     * @throws MahasenClientException
     * @throws IOException
     */
    public String tagSearch(String tag, String hostIp)
            throws URISyntaxException, AuthenticationExceptionException, MahasenClientException, IOException {

        HttpResponse response = sendRequest("tags", false, tag, null, hostIp);

        return getEntityContent(response);

    }

    /**
     * @param propertyName
     * @param propertyValue
     * @return
     * @throws URISyntaxException
     * @throws AuthenticationExceptionException
     *
     * @throws MahasenClientException
     * @throws IOException
     */
    public String search(String propertyName, String propertyValue)
            throws URISyntaxException, AuthenticationExceptionException, MahasenClientException, IOException {

        HttpResponse response = sendRequest(propertyName, false, propertyValue, null);

        return getEntityContent(response);

    }

    /**
     * @param propertyName
     * @param initialValue
     * @param lastValue
     * @return
     * @throws URISyntaxException
     * @throws AuthenticationExceptionException
     *
     * @throws MahasenClientException
     * @throws IOException
     */
    public String rangeSearch(String propertyName, String initialValue, String lastValue)
            throws URISyntaxException, AuthenticationExceptionException, MahasenClientException, IOException {

        HttpResponse response = sendRequest(propertyName, true, initialValue, lastValue);

        return getEntityContent(response);
    }

    /**
     * @param tag
     * @return
     * @throws URISyntaxException
     * @throws AuthenticationExceptionException
     *
     * @throws MahasenClientException
     * @throws IOException
     */
    public String tagSearch(String tag)
            throws URISyntaxException, AuthenticationExceptionException, MahasenClientException, IOException {

        HttpResponse response = sendRequest("tags", false, tag, null);

        return getEntityContent(response);

    }

    /**
     * @param searchRequests
     * @return
     * @throws MahasenClientException
     * @throws IOException
     * @throws URISyntaxException
     * @throws AuthenticationExceptionException
     *
     */
    public String multipleAndSearch(Hashtable<String, Vector<String>> searchRequests)
            throws MahasenClientException, IOException, URISyntaxException, AuthenticationExceptionException {
        HttpResponse response = sendRequest(searchRequests, true);

        return getEntityContent(response);
    }

    /**
     * @param searchRequests
     * @return
     * @throws MahasenClientException
     * @throws IOException
     * @throws URISyntaxException
     * @throws AuthenticationExceptionException
     *
     */
    public String multipleOrSearch(Hashtable<String, Vector<String>> searchRequests)
            throws MahasenClientException, IOException, URISyntaxException, AuthenticationExceptionException {
        HttpResponse response = sendRequest(searchRequests, false);

        return getEntityContent(response);
    }

    /**
     * @param propertyName
     * @param isRangeBase
     * @param value1
     * @param value2
     * @param hostIP
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws AuthenticationExceptionException
     *
     * @throws MahasenClientException
     */
    private HttpResponse sendRequest(String propertyName, boolean isRangeBase,
                                     String value1, String value2, String hostIP)
            throws IOException, URISyntaxException, AuthenticationExceptionException, MahasenClientException {

        httpclient = new DefaultHttpClient();
        clientLogin = new ClientLogin();
        HttpResponse response = null;

        String userName = clientLoginData.getUserName();
        String passWord = clientLoginData.getPassWord();
        String hostAndPort = clientLoginData.getHostNameAndPort();
        String userId = clientLoginData.getUserId(userName, passWord);
        Boolean isLogged = clientLoginData.isLoggedIn();
        System.out.println(" Is Logged : " + isLogged);

        if (isLogged == true) {

            httpclient = WebClientSSLWrapper.wrapClient(httpclient);

            ArrayList<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("propertyName", propertyName));
            qparams.add(new BasicNameValuePair("isRangeBase", String.valueOf(isRangeBase)));


            if (isRangeBase == true) {
                qparams.add(new BasicNameValuePair("initialValue", value1));
                qparams.add(new BasicNameValuePair("lastValue", value2));
            } else {
                qparams.add(new BasicNameValuePair("propertyValue", value1));
            }

            URI uri = URIUtils.createURI("https", hostAndPort, -1, "/mahasen/search_ajaxprocessor.jsp",
                    URLEncodedUtils.format(qparams, "UTF-8"), null);

            HttpPost httppost = new HttpPost(uri);


            System.out.println("executing request " + httppost.getRequestLine());
            response = httpclient.execute(httppost);

        } else {
            System.out.println("User has to be logged in to perform this function");
        }

        if (response.getStatusLine().getStatusCode() == 900) {
            throw new MahasenClientException(String.valueOf(response.getStatusLine()));
        } else if (response.getStatusLine().getStatusCode() == 901) {
            throw new MahasenClientException(String.valueOf(response.getStatusLine()));
        }

        return response;
    }

    /**
     * @param propertyName
     * @param isRangeBase
     * @param value1
     * @param value2
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws AuthenticationExceptionException
     *
     * @throws MahasenClientException
     */
    private HttpResponse sendRequest(String propertyName, boolean isRangeBase, String value1, String value2)
            throws IOException, URISyntaxException, AuthenticationExceptionException, MahasenClientException {

        httpclient = new DefaultHttpClient();
        HttpResponse response = null;

        String userName = clientLoginData.getUserName();
        String passWord = clientLoginData.getPassWord();
        String hostAndPort = clientLoginData.getHostNameAndPort();
        String userId = clientLoginData.getUserId(userName, passWord);
        Boolean isLogged = clientLoginData.isLoggedIn();
        System.out.println(" Is Logged : " + isLogged);

        if (isLogged == true) {

            httpclient = WebClientSSLWrapper.wrapClient(httpclient);

            ArrayList<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("propertyName", propertyName));
            qparams.add(new BasicNameValuePair("isRangeBase", String.valueOf(isRangeBase)));


            if (isRangeBase == true) {
                qparams.add(new BasicNameValuePair("initialValue", value1));
                qparams.add(new BasicNameValuePair("lastValue", value2));
            } else {
                qparams.add(new BasicNameValuePair("propertyValue", value1));
            }

            URI uri = URIUtils.createURI("https", hostAndPort, -1, "/mahasen/search_ajaxprocessor.jsp",
                    URLEncodedUtils.format(qparams, "UTF-8"), null);

            HttpPost httppost = new HttpPost(uri);

            System.out.println("executing request " + httppost.getRequestLine());
            response = httpclient.execute(httppost);

        } else {
            System.out.println("User has to be logged in to perform this function");
        }


        return response;
    }

    /**
     * @param searchRequests
     * @param isAndSearch
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws AuthenticationExceptionException
     *
     * @throws MahasenClientException
     */
    private HttpResponse sendRequest(Hashtable<String, Vector<String>> searchRequests, boolean isAndSearch)
            throws IOException, URISyntaxException, AuthenticationExceptionException, MahasenClientException {

        httpclient = new DefaultHttpClient();
        HttpResponse response = null;

        String userName = clientLoginData.getUserName();
        String passWord = clientLoginData.getPassWord();
        String hostAndPort = clientLoginData.getHostNameAndPort();
        String userId = clientLoginData.getUserId(userName, passWord);
        Boolean isLogged = clientLoginData.isLoggedIn();
        System.out.println(" Is Logged : " + isLogged);

        System.out.println(" Is Logged : " + isLogged);

        if (isLogged == true) {

            httpclient = WebClientSSLWrapper.wrapClient(httpclient);

            ArrayList<NameValuePair> qparams = new ArrayList<NameValuePair>();

            for (Map.Entry<String, Vector<String>> entry : searchRequests.entrySet()) {
                for (String value : entry.getValue()) {
                    qparams.add(new BasicNameValuePair(entry.getKey(), value));
                }

            }

            /*qparams.add(new BasicNameValuePair("test","value1"));
           qparams.add(new BasicNameValuePair("test","value2"));
           qparams.add(new BasicNameValuePair("test","value3"));*/
            URI uri;
            if (isAndSearch == true) {
                uri = URIUtils.createURI("https", hostAndPort, -1,
                        "/mahasen/multiple_and_search_request_ajaxprocessor.jsp",
                        URLEncodedUtils.format(qparams, "UTF-8"), null);
            } else {
                uri = URIUtils.createURI("https", hostAndPort, -1,
                        "/mahasen/multiple_or_search_request_ajaxprocessor.jsp",
                        URLEncodedUtils.format(qparams, "UTF-8"), null);
            }

            HttpPost httppost = new HttpPost(uri);
            System.out.println("executing request " + httppost.getRequestLine());
            response = httpclient.execute(httppost);

        } else {
            System.out.println("User has to be logged in to perform this function");
        }


        return response;
    }

    /**
     * @param response
     * @return
     * @throws IOException
     */
    private String getEntityContent(HttpResponse response) throws IOException {

        String result = null;

        if (response != null) {

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                try {

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(instream));
                    // do something useful with the response

                    result = reader.readLine();
                    System.out.println("in put stream" + result);
                } catch (IOException ex) {

                    // In case of an IOException the connection will be released
                    // back to the connection manager automatically
                    throw ex;

                } finally {

                    // Closing the input stream will trigger connection release
                    instream.close();

                }

                // When HttpClient instance is no longer needed,
                // shut down the connection manager to ensure
                // immediate deallocation of all system resources
                httpclient.getConnectionManager().shutdown();
            }

            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());

            EntityUtils.consume(entity);

        }


        // When HttpClient instance is no longer needed,
        // shut down the connection manager to ensure
        // immediate deallocation of all system resources
        httpclient.getConnectionManager().shutdown();


        result = result.substring(1, result.length() - 1);
        if (result == null) {
            result = "No result found";
        }
        return result;


    }
}
