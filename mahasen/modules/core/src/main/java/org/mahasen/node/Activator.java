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

import rice.environment.Environment;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * OSGi Activator class which will get activated in WSO2 registry at the
 * node start up
 */

public class Activator implements BundleActivator {
    private static Log log = LogFactory.getLog(Activator.class);
    MahasenNodeManager nodeManager = MahasenNodeManager.getInstance();

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
}

