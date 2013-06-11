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

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mahasen.exception.MahasenConfigurationException;
import org.mahasen.util.MahasenUtils;

//~--- JDK imports ------------------------------------------------------------

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

public class MahasenConfiguration {
    private static MahasenConfiguration instance = null;
    private static MahasenConfigurationBuilder mahasenConfigurationBuilder = null;
    private InetAddress bootIp;
    private int bootPort;
    private String downloadRepositoryPath;
    private InetAddress localIp;
    private int localPort;
    private String registryUrl;
    private String repositoryPath;
    private String trustStorePath;
    private String tempUploadFolderPath;
    private String tempDownloadFolderPath;

    private MahasenConfiguration() {
    }

    /**
     *
     * @return
     */
    public static MahasenConfiguration getInstance() {
        if (instance == null) {
            instance = new MahasenConfiguration();
            mahasenConfigurationBuilder = new MahasenConfigurationBuilder();

            try {
                instance = mahasenConfigurationBuilder.createConfiguration();
            } catch (Exception e) {
                e.printStackTrace();    // To change body of catch statement use File | Settings | File Templates.
            }
        }

        return instance;
    }

    /**
     * @param localIp
     * @throws UnknownHostException
     */
    public void setLocalIp(String localIp) throws UnknownHostException {
        this.localIp = MahasenUtils.convertToInetAddress(localIp);
    }

    /**
     * @param localPort
     */
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    /**
     * @param bootIp
     * @throws UnknownHostException
     */
    public void setBootIp(String bootIp) throws UnknownHostException {
        this.bootIp = MahasenUtils.convertToInetAddress(bootIp);
    }

    /**
     * @param bootPort
     */
    public void setBootPort(int bootPort) {
        this.bootPort = bootPort;
    }

    /**
     * @param repositoryPath
     */
    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    /**
     * @param downloadRepo
     */
    public void setDownloadRepository(String downloadRepo) {
        this.downloadRepositoryPath = downloadRepo;
    }

    /**
     * @param registryUrl
     * @throws MalformedURLException
     */
    public void setRegistryUrl(String registryUrl) throws MalformedURLException {
        this.registryUrl = registryUrl;
    }

    /**
     * @param trustStorePath
     */
    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    /**
     * @param tempFolderPath
     */
    public void setUploadTempFolderPath(String tempFolderPath) {
        this.tempUploadFolderPath = tempFolderPath;
    }

    /**
     * @param tempFolderPath
     */
    public void setDownloadTempFolderPath(String tempFolderPath) {
        this.tempDownloadFolderPath = tempFolderPath;
    }

    /**
     * @return
     * @throws MahasenConfigurationException
     */
    public InetAddress getLocalIP() throws MahasenConfigurationException {
        if (localIp == null) {
            throw new MahasenConfigurationException("Local IP is not set");
        }

        return localIp;
    }

    /**
     * @return
     * @throws MahasenConfigurationException
     */
    public int getLocalPort() throws MahasenConfigurationException {
        if (localPort == 0) {
            throw new MahasenConfigurationException("Local port is not set");
        }

        return localPort;
    }

    /**
     * @return
     * @throws MahasenConfigurationException
     */
    public InetAddress getBootIP() throws MahasenConfigurationException {
        if (bootIp == null) {
            throw new MahasenConfigurationException("Boot IP not set");
        }

        return bootIp;
    }

    /**
     * @return
     * @throws MahasenConfigurationException
     */
    public int getBootPort() throws MahasenConfigurationException {
        if (bootPort == 0) {
            throw new MahasenConfigurationException("Boot port is not set");
        }

        return bootPort;
    }

    /**
     * @return
     * @throws MahasenConfigurationException
     */
    public String getRepositoryPath() throws MahasenConfigurationException {
        if (repositoryPath == null) {
            throw new MahasenConfigurationException("Repsitory path is not set");
        }

        return repositoryPath;
    }

    /**
     * @return
     * @throws MahasenConfigurationException
     */
    public String getDownloadRepositoryPath() throws MahasenConfigurationException {
        if (downloadRepositoryPath == null) {
            throw new MahasenConfigurationException("DownloadServlet Repsitory path is not set");
        }

        return downloadRepositoryPath;
    }

    /**
     * @return
     * @throws MahasenConfigurationException
     */
    public String getRegistryUrl() throws MahasenConfigurationException {
        if (registryUrl == null) {
            throw new MahasenConfigurationException("Registry URL is not set");
        }

        return registryUrl;
    }

    /**
     * @return
     * @throws MahasenConfigurationException
     */
    public String getTrustStorePath() throws MahasenConfigurationException {
        if (trustStorePath == null) {
            throw new MahasenConfigurationException("Truststore Path is not set");
        }

        return trustStorePath;
    }

    /**
     * @return
     */
    public String getTempUploadFolderPath() {
        return tempUploadFolderPath;
    }

    /**
     * @return
     */
    public String getTempDownloadFolderPath() {
        return tempDownloadFolderPath;
    }

}



