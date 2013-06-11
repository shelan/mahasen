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

public class ClientConfiguration {

    private static ClientConfiguration instance = null;

    private static ClientConfigurationBuilder clientConfigurationBuilder = null;

    String userName;
    String passWord;
    String host;
    String port;
    String downloadRepo;
    String trustStorePath;

    /**
     * @return
     */
    public static ClientConfiguration getInstance() {

        if (instance == null) {

            instance = new ClientConfiguration();
            clientConfigurationBuilder = new ClientConfigurationBuilder();

            try {

                instance = clientConfigurationBuilder.build();

            } catch (Exception e) {
                System.out.println("Error while building configuration for client");
            }
        }
        return instance;
    }

    /**
     * @param uName
     */
    public void setUserName(String uName) {
        this.userName = uName;
    }

    /**
     * @param pWord
     */
    public void setPassWord(String pWord) {
        this.passWord = pWord;
    }

    /**
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @param port
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @param downloadRepo
     */
    public void setDownloadRepo(String downloadRepo) {
        this.downloadRepo = downloadRepo;
    }

    /**
     * @param trustStorePath
     */
    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    /**
     * @return
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return
     */
    public String getPassWord() {
        return passWord;
    }

    /**
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * @return
     */
    public String getPort() {
        return port;
    }

    /**
     * @return
     */
    public String getDownloadRepo() {
        return downloadRepo;
    }

    /**
     * @return
     */
    public String getTrustStorePath() {
        return trustStorePath;
    }
}
