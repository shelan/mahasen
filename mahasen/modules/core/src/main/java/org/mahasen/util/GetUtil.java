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
import org.mahasen.exception.MahasenConfigurationException;
import org.mahasen.exception.MahasenException;
import org.mahasen.filemanagement.MahasenFileJoiner;
import org.mahasen.resource.MahasenResource;
import org.mahasen.thread.MahasenDownloadWorker;
import rice.pastry.Id;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;


public class GetUtil extends AbstractCommonUtil {

    private static Log log = LogFactory.getLog(PutUtil.class);
    private static AtomicInteger storedNoOfParts = new AtomicInteger(0);

    /**
     * @param fileToDowload
     */
    public File get(String fileToDowload)
            throws MahasenConfigurationException, MahasenException, InterruptedException {

        Id resourceId = Id.build(String.valueOf(fileToDowload.hashCode()));
        File firstPart = null;
        String fileName = null;
        String currentFileName = null;

        String downloadRepository = super.getDownloadRepositoryPath();
        File tempDirectory = new File(downloadRepository + MahasenConstants.TEMP_FOLDER);

        // storing in the local file system first
        if (!tempDirectory.exists()) {
            tempDirectory = new File(downloadRepository + MahasenConstants.TEMP_FOLDER);
            tempDirectory.mkdir();
        }

        MahasenResource mahasenResource = mahasenManager.lookupDHT(resourceId);

        if (mahasenResource == null) {
            throw new MahasenException("File Not found");
        }
        fileName = mahasenResource.getProperty(MahasenConstants.FILE_NAME).toString();

        File downloadTempDir = new File(tempDirectory.getAbsolutePath() + "/" + fileName);
        downloadTempDir.mkdir();
        setStoredNoOfParts(mahasenResource.getPartNames().size());

        for (String partName : mahasenResource.getPartNames()) {
            currentFileName = fileName + "." + partName;
            MahasenDownloadWorker mahasenDownloadWorker = new MahasenDownloadWorker(partName, mahasenResource);

            Thread downloadThread = new Thread(mahasenDownloadWorker);
            downloadThread.start();

        }

        final BlockFlag blockFlag = new BlockFlag(true, 6000);
        while (true) {
            if (storedNoOfParts.intValue() == 0) {
                storedNoOfParts.set(0);
                break;
            }

            if (blockFlag.isBlocked()) {

                mahasenManager.getNode().getEnvironment().getTimeSource().sleep(10);
            } else {
                throw new MahasenException("Time out in downloading file " + fileName);
            }
        }


        MahasenFileJoiner mahasenFileJoiner = new MahasenFileJoiner();
        firstPart = new File(downloadTempDir.getAbsolutePath() + "/" + fileName + ".part1");
        File fileToReturn = mahasenFileJoiner.join(firstPart);

        return fileToReturn;
    }

    /**
     * @param numberOfParts
     */
    public void setStoredNoOfParts(int numberOfParts) {
        storedNoOfParts.set(numberOfParts);
    }

    /**
     *
     */
    public static void decrementStoredNoOfParts() {
        storedNoOfParts.decrementAndGet();
    }


}
