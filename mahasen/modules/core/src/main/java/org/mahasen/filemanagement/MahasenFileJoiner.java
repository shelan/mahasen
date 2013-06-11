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


import java.io.*;

public class MahasenFileJoiner {
    private File firstPart;
    private int noOfParts;
    private String absolutePath;
    private String fileName;
    private boolean isJoinSuccess;


    /**
     * @param firstPart
     * @return joinedFile
     */
    public File join(File firstPart) {

        this.firstPart = firstPart;
        noOfParts = getNoOfPartsToJoin(this.firstPart);
        File[] fileParts = new File[noOfParts];
        File joinedFile = new File(absolutePath + "/" + fileName);

        int i = 0;
        int bufferSize = 1024 * 8;
        byte[] buffer = new byte[1024 * 8];
        try {
            for (i = 0; i < noOfParts; i++) {
                fileParts[i] = new File(absolutePath + "/" + fileName + ".part" + (i + 1));
            }
            i = 0;
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(joinedFile));
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileParts[i]));
            while (true) {
                if (bufferedInputStream.available() > bufferSize) {
                    bufferedInputStream.read(buffer);
                    bufferedOutputStream.write(buffer);
                } else {
                    byte[] bufferTemp = new byte[bufferedInputStream.available()];
                    bufferedInputStream.read(bufferTemp);
                    bufferedOutputStream.write(bufferTemp);
                    bufferedOutputStream.flush();
                    i++;
                    if (i == noOfParts) {
                        System.out.println("Files joined successfully");
                        break;

                    } else {
                        bufferedInputStream = new BufferedInputStream(new FileInputStream(fileParts[i]));
                    }
                }
            }
        } catch (Exception e) {
        }
        return joinedFile;
    }

    /**
     * @param firstFileInp
     * @return numberOfParts
     */
    private int getNoOfPartsToJoin(File firstFileInp) {
        int numberOfParts = 1;
        try {
            this.absolutePath = firstFileInp.getAbsolutePath().substring(0, firstFileInp.getAbsolutePath().lastIndexOf("/"));

            fileName = (firstFileInp.getName()).substring(0, (firstFileInp.getName().lastIndexOf(".")));

            int i = 2;
            while (true) {
                File currentPart = new File(absolutePath + "/" + fileName + ".part" + i);

                if (currentPart.exists()) {
                    numberOfParts++;
                    i++;
                } else {
                    currentPart = null;
                    break;
                }
            }

        } catch (Exception e) {

        } finally {

            return numberOfParts;

        }
    }

    /**
     * @return isJoinSuccess
     */
    public boolean isJoinSuccess() {
        return isJoinSuccess;
    }

    /**
     * @param joinSuccess
     */
    public void setJoinSuccess(boolean joinSuccess) {
        isJoinSuccess = joinSuccess;
    }

}
