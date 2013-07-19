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

package org.mahasen.node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mahasen.configuration.MahasenConfiguration;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import rice.environment.Environment;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * OSGi Activator class which will get activated in WSO2 registry at the
 * node start up
 */

/**
 * @scr.component name="org.mahasen.node.Activator" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */

public class Activator implements BundleActivator {
    private static Log log = LogFactory.getLog(Activator.class);
    MahasenNodeManager nodeManager = MahasenNodeManager.getInstance();
    private static RegistryService registryService;

    /**
     * @param bundleContext
     * @throws Exception
     */
    public void start(BundleContext bundleContext) throws Exception {
        log.info("starting Mahasen Broker");

        MahasenConfiguration mahasenConfiguration = MahasenConfiguration.getInstance();
        InetAddress bootaddr = mahasenConfiguration.getBootIP();
        InetAddress localaddr = mahasenConfiguration.getLocalIP();
        int bootport = mahasenConfiguration.getBootPort();
        int localport = mahasenConfiguration.getLocalPort();
        String registryUrl = mahasenConfiguration.getRegistryUrl();
        InetSocketAddress bootaddress = new InetSocketAddress(bootaddr, bootport);
        InetSocketAddress localaddress = new InetSocketAddress(localaddr, localport);

        nodeManager.bootNode(localport, localaddress, bootport, bootaddress, registryUrl, new Environment());
    }

    /**
     * @param bundleContext
     * @throws Exception
     */
    public void stop(BundleContext bundleContext) throws Exception {
        nodeManager.cleanUp();
        log.info("cleaned up Mahasen Broker Application");
    }

    protected void setRegistryService(RegistryService registryService) {
        Activator.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        if (registryService != null) {
            registryService = null;
        }
    }

    public static RegistryService getRegistryService() throws Exception {
        if (registryService == null) {
          //  throw new Exception("Registry service not available");
        }
        return registryService;
    }
}

