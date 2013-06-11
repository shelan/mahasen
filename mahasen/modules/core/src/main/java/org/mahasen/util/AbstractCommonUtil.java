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

package org.mahasen.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mahasen.MahasenConstants;
import org.mahasen.RegistryConnectionManager;
import org.mahasen.configuration.MahasenConfiguration;
import org.mahasen.exception.MahasenConfigurationException;
import org.mahasen.node.MahasenNodeManager;
import org.mahasen.resource.MahasenResource;
import org.springframework.core.io.AbstractResource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import rice.p2p.commonapi.Id;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;


public abstract class AbstractCommonUtil {

    MahasenNodeManager mahasenManager;

    RegistryConnectionManager registryConnectionManager;

    boolean isInitialized = false;

    MahasenConfiguration mahasenConfiguration = MahasenConfiguration.getInstance();

    private static Log log = LogFactory.getLog(AbstractResource.class);

    String username = "admin";
    String password = "admin";
    String url = ("https://localhost:9443/registry");

    public AbstractCommonUtil() {
        this.init();
        isInitialized = true;

    }


    private void init() {

        this.mahasenManager = MahasenNodeManager.getInstance();
        registryConnectionManager = new RegistryConnectionManager();
        registryConnectionManager.setDefaultKeyStore();

    }

    /**
     * @return
     * @throws MahasenConfigurationException
     */
    public String getRepositoryPath() throws MahasenConfigurationException {

        return mahasenConfiguration.getRepositoryPath();
    }

    /**
     * @return
     * @throws MahasenConfigurationException
     */
    public String getDownloadRepositoryPath() throws MahasenConfigurationException {

        return mahasenConfiguration.getDownloadRepositoryPath();
    }

    /**
     * @return
     * @throws MahasenConfigurationException
     */
    public String getTrustStorePath() throws MahasenConfigurationException {

        return mahasenConfiguration.getTrustStorePath();
    }

    /**
     * Setting required parameters for HTTPS connection
     *
     * @throws MahasenConfigurationException
     */
    public void setTrustStore() throws MahasenConfigurationException {

        System.setProperty("javax.net.ssl.trustStore", this.getTrustStorePath());
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

    }

    /**
     * @param resourceId
     * @return
     * @throws RegistryException
     */
    public MahasenResource createMahasenResource(Id resourceId) throws RegistryException {

        MahasenResource mahasenResource = new MahasenResource(resourceId);
        log.info("created Mahasen resource with ID :" + resourceId);
        return mahasenResource;

    }

    /**
     * @param userDefinedProperties
     * @return
     */
    public Hashtable<String, String> removeSystemMetadataFromUserMetadata(
            Hashtable<String, String> userDefinedProperties) {
        List systemDefinedProperties = new ArrayList();
        systemDefinedProperties.add(MahasenConstants.FILE_SIZE);
        systemDefinedProperties.add(MahasenConstants.UPLOADED_DATE);
        systemDefinedProperties.add(MahasenConstants.FILE_NAME);
        systemDefinedProperties.add(MahasenConstants.FILE_PATH);

        Iterator iterator = userDefinedProperties.keySet().iterator();
        while (iterator.hasNext()) {
            String userKey = (String) iterator.next();
            if (systemDefinedProperties.contains(userKey)) {
                iterator.remove();
            }
        }
        return userDefinedProperties;
    }
}
