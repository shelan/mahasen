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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mahasen.exception.MahasenConfigurationException;
import org.mahasen.exception.MahasenException;
import org.mahasen.util.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import rice.p2p.past.PastException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Vector;


public class MahasenManager {

    private static Log log = LogFactory.getLog(MahasenManager.class);

    /**
     * @param inputStream
     * @param fileName
     * @param tags
     * @throws InterruptedException
     * @throws RegistryException
     * @throws PastException
     * @throws IOException
     * @throws MahasenConfigurationException
     */
    public void put(InputStream inputStream, String fileName, String tags, Hashtable<String, String> properties
                    )
            throws InterruptedException, RegistryException, PastException,
            IOException, MahasenConfigurationException, MahasenException {

        PutUtil putUtil = new PutUtil();
        putUtil.put(inputStream, fileName, tags, properties);
    }

    /**
     * @param fileToDownload
     * @return
     * @throws Exception
     */
    public File get(String fileToDownload)
            throws MahasenException, MahasenConfigurationException, InterruptedException {

        GetUtil getUtil = new GetUtil();
        return getUtil.get(fileToDownload);
    }

    /**
     * @param fileToDelete
     * @throws MalformedURLException
     * @throws RegistryException
     * @throws InterruptedException
     */
    public void delete(String fileToDelete) throws MalformedURLException, RegistryException, InterruptedException,
            MahasenConfigurationException, MahasenException {

        DeleteUtil deleteUtil = new DeleteUtil();
        deleteUtil.delete(fileToDelete);

    }

    /**
     * @param fileToUpdate
     * @param tags
     * @param properties
     * @throws RegistryException
     * @throws PastException
     * @throws InterruptedException
     * @throws MahasenException
     */
    public void update(String fileToUpdate, String tags, Hashtable<String, String> properties)
            throws RegistryException, PastException, InterruptedException, MahasenException {

        UpdateUtil updateUtil = new UpdateUtil();
        updateUtil.updateMetadata(fileToUpdate, tags, properties);

    }

    /**
     * @return
     */
    public Vector<String> search(String propertyName, String propertyValue) throws InterruptedException,
            MahasenException {
        SearchUtil searchUtil = new SearchUtil();
        return searchUtil.searchProperty(propertyName, propertyValue);

    }

    /**
     * @param propertyName
     * @param initialValue
     * @param lastValue
     * @return
     * @throws InterruptedException
     * @throws MahasenException
     */
    public Vector<String> rangeSearch(String propertyName, String initialValue, String lastValue)
            throws InterruptedException, MahasenException {
        SearchUtil searchUtil = new SearchUtil();
        return searchUtil.rangeSearch(propertyName, initialValue, lastValue);
    }

    /**
     * @param searchParameters
     * @return
     * @throws MahasenException
     * @throws InterruptedException
     */
    public Vector<String> multipleAndSearch(Hashtable<String, Vector<String>> searchParameters)
            throws MahasenException, InterruptedException {
        SearchUtil searchUtil = new SearchUtil();
        return searchUtil.multipleAndSearch(searchParameters);
    }

    /**
     * @param searchParameters
     * @return
     * @throws MahasenException
     * @throws InterruptedException
     */
    public Vector<String> multipleOrSearch(Hashtable<String, Vector<String>> searchParameters)
            throws MahasenException, InterruptedException {
        SearchUtil searchUtil = new SearchUtil();
        return searchUtil.multipleOrSearch(searchParameters);
    }
}
