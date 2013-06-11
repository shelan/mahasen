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
import org.mahasen.servlet.DeleteServlet;
import org.mahasen.ssl.SSLWrapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.mahasen.util.DeleteUtil;

import java.net.URI;

public class MahasenDeleteWorker implements Runnable {
    private static Log log = LogFactory.getLog(MahasenUploadWorker.class);
    private URI uri;

    /**
     * @param uri
     */
    public MahasenDeleteWorker(URI uri) {
        this.uri = uri;

    }


    public void run() {
        HttpClient deleteHttpClient = new DefaultHttpClient();
        try {
            deleteHttpClient = SSLWrapper.wrapClient(deleteHttpClient);
            HttpPost httppost = new HttpPost(uri);

            System.out.println("Executing Delete request " + httppost.getRequestLine());
            HttpResponse response = deleteHttpClient.execute(httppost);

            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());

            if ((response.getStatusLine().getReasonPhrase().equals("OK")) &&
                    (response.getStatusLine().getStatusCode() == 200)) {
                DeleteUtil.decrementStoredNoOfParts();
            }

        } catch (Exception e) {
            log.error("Error occurred while deleting parts", e);
        }
    }
}
