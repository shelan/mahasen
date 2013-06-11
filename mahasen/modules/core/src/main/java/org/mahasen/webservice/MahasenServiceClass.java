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

package org.mahasen.webservice;

import org.mahasen.MahasenManager;
import org.mahasen.exception.MahasenConfigurationException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import rice.p2p.past.PastException;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Hashtable;

public class MahasenServiceClass {

    private static MahasenManager manager = new MahasenManager();

    /**
     * @param handler
     * @param fileName
     * @param tags
     * @param properties
     * @throws IOException
     * @throws RegistryException
     * @throws PastException
     * @throws MahasenConfigurationException
     * @throws InterruptedException
     */
    public void upload(DataHandler handler, String fileName, String tags, Hashtable<String, String> properties)
            throws IOException, RegistryException, PastException, MahasenConfigurationException, InterruptedException {

    }

    /**
     * @param fileToDownload
     * @return
     * @throws Exception
     */
    public DataHandler download(String fileToDownload) throws Exception {
        File file = manager.get(fileToDownload);
        if (file != null) {
            FileDataSource fileDataSource = new FileDataSource(file);
            DataHandler dataHandler = new DataHandler(fileDataSource);

            return dataHandler;
        }
        return null;
    }

    /**
     * @param fileName
     * @throws MalformedURLException
     * @throws RegistryException
     * @throws MahasenConfigurationException
     * @throws InterruptedException
     */
    public void delete(String fileName) throws
            MalformedURLException, RegistryException, MahasenConfigurationException, InterruptedException {

    }


}

