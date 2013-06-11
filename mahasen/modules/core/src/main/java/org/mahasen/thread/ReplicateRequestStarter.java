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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.mahasen.MahasenConstants;
import org.mahasen.resource.MahasenResource;
import org.mahasen.ssl.SSLWrapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ReplicateRequestStarter implements Runnable {

    private MahasenResource resource = null;

    /**
     * @param resource
     */
    public ReplicateRequestStarter(MahasenResource resource) {
        this.resource = resource;

    }

    public void run() {
        for (String partName : resource.getPartNames()) {
            try {

                URI uri = null;
                ArrayList<NameValuePair> qparams = new ArrayList<NameValuePair>();
                qparams.add(new BasicNameValuePair("parentFileName",
                        String.valueOf(resource.getProperty(MahasenConstants.FILE_NAME))));
                qparams.add(new BasicNameValuePair("partName", partName));
                uri = URIUtils.createURI("https", resource.getSplittedPartsIpTable().get(partName).get(0) + ":" +
                        MahasenConstants.SERVER_PORT, -1, "/mahasen/replicate_request_ajaxprocessor.jsp",
                        URLEncodedUtils.format(qparams, "UTF-8"), null);

                System.out.println("Target Address for Replicate Request : " + uri);

                HttpClient replicateHttpClient = new DefaultHttpClient();
                replicateHttpClient = SSLWrapper.wrapClient(replicateHttpClient);
                HttpPost httppost = new HttpPost(uri);

                HttpResponse response = replicateHttpClient.execute(httppost);
                System.out.println("--------- response to ReplicateRequestStarter----------"
                        + response.getStatusLine());
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (URISyntaxException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }

    }
}
