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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mahasen.MahasenConstants;
import org.mahasen.exception.MahasenConfigurationException;

import java.io.File;
import java.io.FileInputStream;

import java.util.Properties;

public class MahasenConfigurationBuilder {
    private static final Log log = LogFactory.getLog(MahasenConfigurationBuilder.class);

    /**
     * @return
     * @throws MahasenConfigurationException
     * @throws Exception
     */
    public MahasenConfiguration createConfiguration() throws MahasenConfigurationException, Exception {
        Properties props = new Properties();
        String path = getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(5);
        int i = path.lastIndexOf(File.separator);

        path = (i > -1)
                ? path.substring(0, i)
                : path;

        String propertyFilePath = path + "/../../conf/mahasen.properties";
        File propertyFile = new File(propertyFilePath);
        MahasenConfiguration mahasenConfiguration = null;

        if (propertyFile.exists()) {
            FileInputStream fis = new FileInputStream(propertyFile);
            props.load(fis);
            fis.close();

            log.info("Creating Mahasen Pastry Configuraion with " + propertyFile.getPath());

            mahasenConfiguration = MahasenConfiguration.getInstance();

            try {
                mahasenConfiguration.setRegistryUrl(props.getProperty("registry-url"));
                mahasenConfiguration.setRepositoryPath(props.getProperty("repository-path"));
                mahasenConfiguration.setDownloadRepository(props.getProperty("download-repository"));
                mahasenConfiguration.setTrustStorePath(props.getProperty("truststore-path"));
                mahasenConfiguration.setBootIp(props.getProperty("boot-ip-address"));
                mahasenConfiguration.setBootPort(Integer.parseInt(props.getProperty("boot-port")));
                mahasenConfiguration.setLocalIp(props.getProperty("local-ip-address"));
                mahasenConfiguration.setLocalPort(Integer.parseInt(props.getProperty("local-port")));
                createTempFolder(props.getProperty("repository-path"));
                mahasenConfiguration.setUploadTempFolderPath(props.getProperty("repository-path") +
                        MahasenConstants.TEMP_FOLDER);
                createTempFolder(props.getProperty("download-repository"));
                mahasenConfiguration.setDownloadTempFolderPath(props.getProperty("download-repository") +
                        MahasenConstants.TEMP_FOLDER);
            } catch (Exception e) {
                log.error(e);
            }
        } else {
            throw new MahasenConfigurationException("Cannot find Property File");
        }

        return mahasenConfiguration;
    }

    /**
     * @param repository
     * @throws Exception
     */
    private void createTempFolder(String repository) throws Exception {
        new File(repository + MahasenConstants.TEMP_FOLDER).mkdir();
    }
}

