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

package org.mahasen.configuration;

import java.io.*;
import java.util.Properties;


public class ClientConfigurationBuilder {
    /**
     * @return
     * @throws IOException
     */
    public ClientConfiguration build() throws IOException {
        Properties props = new Properties();
        ClientConfiguration clientConfiguration = new ClientConfiguration();

        try {
            props.load(getClass().getResourceAsStream("/config/mahasen.client.properties"));
            clientConfiguration.setUserName(props.getProperty("user-name"));
            clientConfiguration.setPassWord(props.getProperty("pass-word"));
            clientConfiguration.setHost(props.getProperty("host"));
            clientConfiguration.setPort(props.getProperty("port"));
            clientConfiguration.setDownloadRepo(props.getProperty("download-repo"));
            clientConfiguration.setTrustStorePath(getResourcePathWithinJar("/security/client-truststore.jks"));
        } catch (Exception e) {
            System.out.println("Error while building configuration for client");

        }
        return clientConfiguration;
    }

    /**
     * @param path
     * @return
     */
    private String getResourcePathWithinJar(String path) {
        String absolutePath = null;
        try {
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            File temporaryFile = new File(tempDir, "templateCopy.jks");
            OutputStream outputStream = new FileOutputStream(temporaryFile);
            InputStream inputStream = getClass().getResourceAsStream(path);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            inputStream.close();
            outputStream.close();
            absolutePath = temporaryFile.getAbsolutePath();
        } catch (Exception e) {
            absolutePath = ""; //Current working directory instead.
        }
        System.out.println(absolutePath);
        return absolutePath;
    }
}

