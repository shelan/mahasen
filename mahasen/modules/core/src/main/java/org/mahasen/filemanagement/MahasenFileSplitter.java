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
package org.mahasen.filemanagement;

import org.mahasen.MahasenConstants;

import java.io.*;
import java.util.HashMap;
import java.util.Vector;

public class MahasenFileSplitter {
    private File originalFile;
    private String directory;
    private long fileLength;
    private int noOfParts;
    private long partSize = 20 * 1024 * 1024;
    private boolean success = false;
    private File[] fileParts;
    private HashMap<String, String> partNames;

    /**
     * @param inputFile
     * @return success
     */
    public boolean split(File inputFile) {
        this.originalFile = inputFile;
        this.fileLength = inputFile.length();
        String inputFileName = originalFile.getName();
        String absolutePath = originalFile.getAbsolutePath();
        partNames = new HashMap<String, String>();
        try {

            directory = absolutePath.substring(0, (absolutePath.lastIndexOf("/") + 1));

            if (partSize != 0) {
                if (fileLength % partSize == 0) {
                    this.noOfParts = ((int) (fileLength / partSize));
                } else {
                    this.noOfParts = ((int) (fileLength / partSize));
                    noOfParts++;
                }
            }

            fileParts = new File[noOfParts];

            for (int i = 0; i < noOfParts; i++) {
                String path = directory + inputFileName + ".part" + (i + 1);
                fileParts[i] = new File(path);
                partNames.put("part" + (i + 1), path);
            }
            FileInputStream fileInputStream = new FileInputStream(originalFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            int i = 0;

            FileOutputStream outputStream = new FileOutputStream(fileParts[i]);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

            int buffSize = 8 * 1024;
            byte[] buffer = new byte[buffSize];

            while (true) {
                if (bufferedInputStream.available() < buffSize) {
                    byte[] newBuff = new byte[bufferedInputStream.available()];
                    bufferedInputStream.read(newBuff);
                    bufferedOutputStream.write(newBuff);
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                    success = true;
                    System.out.println("Successfully File Parts created");
                    break;
                }
                int r = bufferedInputStream.read(buffer);

                if (fileParts[i].length() >= partSize) {
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                    i++;
                    outputStream = new FileOutputStream(fileParts[i]);
                    bufferedOutputStream = new BufferedOutputStream(outputStream);
                }
                bufferedOutputStream.write(buffer);
            }


        } catch (Exception e) {
        }

        return success;

    }

    /**
     * @param inputStream
     * @param fileLength
     * @param fileName
     * @param directory
     * @return success
     */
    public boolean split(InputStream inputStream, long fileLength, String fileName, String directory) {
        String inputFileName = fileName;
        partNames = new HashMap<String, String>();

        try {


            if (partSize != 0) {
                this.noOfParts = ((int) (fileLength / partSize));
                noOfParts++;
            }

            File[] fileParts = new File[noOfParts];

            for (int i = 0; i < noOfParts; i++) {
                String path = directory + MahasenConstants.TEMP_FOLDER + inputFileName + ".part" + (i + 1);
                fileParts[i] = new File(path);
                partNames.put("part" + i, path);
            }

            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            int i = 0;

            FileOutputStream outputStream = new FileOutputStream(fileParts[i]);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

            int buffSize = 8 * 1024;
            byte[] buffer = new byte[buffSize];

            while (true) {
                if (bufferedInputStream.available() < buffSize) {
                    byte[] newBuff = new byte[bufferedInputStream.available()];
                    bufferedInputStream.read(newBuff);
                    bufferedOutputStream.write(newBuff);
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                    success = true;
                    System.out.println("Successfully File Parts created");
                    break;
                }
                int r = bufferedInputStream.read(buffer);

                if (fileParts[i].length() >= partSize) {
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                    i++;
                    outputStream = new FileOutputStream(fileParts[i]);
                    bufferedOutputStream = new BufferedOutputStream(outputStream);
                }
                bufferedOutputStream.write(buffer);
            }

        } catch (Exception e) {
        }

        return success;

    }

    /**
     * @return splitParts
     */
    public File[] getSplitParts() {
        File[] splitParts = null;
        if (success) {
            splitParts = fileParts;
        }
        return splitParts;
    }

    /**
     * @return partNames
     */
    public HashMap<String, String> getPartNames() {
        return partNames;
    }

}
