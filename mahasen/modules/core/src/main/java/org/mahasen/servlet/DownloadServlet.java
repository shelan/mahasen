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
package org.mahasen.servlet;

import org.mahasen.MahasenConstants;
import org.mahasen.MahasenManager;
import org.mahasen.exception.MahasenConfigurationException;
import org.mahasen.exception.MahasenException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class DownloadServlet extends HttpServlet {
    /**
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        doPost(request, response);

    }

    /**
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletOutputStream outputStream = null;
        ServletContext context = null;
        DataInputStream inputStream = null;


        try {
            String fileName = request.getParameter(MahasenConstants.FILE_NAME);
            MahasenManager serviceManager = new MahasenManager();
            int length = 0;
            outputStream = response.getOutputStream();
            context = getServletConfig().getServletContext();

            File fileToDownload = serviceManager.get(MahasenConstants.ROOT_REGISTRY_PATH + fileName);

            //  Stream to the requester.
            String mimetype = context.getMimeType(fileToDownload.getAbsolutePath());
            log("Absolute Path : " + fileToDownload.getAbsolutePath());

            //  Set the response and go!
            response.setContentType((mimetype != null) ? mimetype : "application/octet-stream");
            response.setContentLength((int) fileToDownload.length());
            response.setHeader("Content-Disposition", "attachment; filename=/" +
                    fileToDownload.getAbsolutePath() + "/");

            //  Stream to the requester.
            if (fileToDownload.exists()) {
                byte[] buffer = new byte[1024];
                inputStream = new DataInputStream(new FileInputStream(fileToDownload));

                while ((inputStream != null) && ((length = inputStream.read(buffer)) != -1)) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.flush();
                String folderToDelete = fileToDownload.getPath() ;
                String folder = folderToDelete.substring(0,folderToDelete.lastIndexOf("/"));
                File fileTodelete = new File(folder);

                File[] tempFiles = fileTodelete.listFiles();
                for (File tempFile : tempFiles) {


                    boolean deleted = tempFile.delete();

                    if (!deleted) {

                        System.out.println("Failed to delete the file" + tempFile);
                    }
                }


                fileTodelete.delete();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MahasenException e) {
            response.sendError(900, e.getMessage());
            return;
        } catch (MahasenConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }

    }
}


