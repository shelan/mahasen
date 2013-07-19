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
package org.mahasen;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;
import java.net.URL;


public class RegistryConnectionManager {


    private static Log log = LogFactory.getLog(RegistryConnectionManager.class);

    private boolean isConnected = false;

    private RemoteRegistry remoteRegistry = null;

    private static boolean isKeyStoreSet = false;

    private static String CARBON_HOME = System.getProperty("carbon.home");

    private static final String axis2Conf = ServerConfiguration.getInstance().getFirstProperty("Axis2Config.clientAxis2XmlLocation");

    private static ConfigurationContext configContext = null;

    private static final String axis2Repo = CARBON_HOME
            + File.separator +"repository" +
            File.separator + "deployment" + File.separator + "client";


    /**
     * @param path
     * @param password
     * @param type
     */
    public void setCustomKeyStore(String path, String password, String type) {

        System.setProperty("javax.net.ssl.trustStore", path);
        System.setProperty("javax.net.ssl.trustStorePassword", password);
        System.setProperty("javax.net.ssl.trustStoreType", type);

        isKeyStoreSet = true;

    }


    public static void setDefaultKeyStore() {

        System.setProperty("javax.net.ssl.trustStore", CARBON_HOME+"/resources/security/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        isKeyStoreSet = true;
    }

    /**
     * @param url
     * @param username
     * @param password
     * @return
     * @throws RegistryException
     */
    public RemoteRegistry getRemoteRegistry(URL url, String username, String password) throws RegistryException {

        if (isKeyStoreSet = false) {
            setDefaultKeyStore();
        }

        remoteRegistry = new RemoteRegistry(url, username, password);

        isConnected = true;
        log.debug("Connected to Remote registry at " + url.toString());

        return remoteRegistry;
    }

    public static Registry getWsRegistry(String url, String username, String password) throws RegistryException {
        setDefaultKeyStore();
        System.setProperty("carbon.repo.write.mode", "true");

        try {
            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                    axis2Repo, axis2Conf);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }
        Registry registry = new WSRegistryServiceClient(url.toString(), username, password, configContext);
        return registry;
    }

    /**
     * @return
     */
    public boolean isConnected() {
        return isConnected;
    }


}
