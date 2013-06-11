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
import org.mahasen.util.GetUtil;

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

public class DownloadRequestServlet extends HttpServlet {

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

        String fileName = request.getParameter(MahasenConstants.FILE_NAME);

        System.out.println("File to Download :" + fileName);

        GetUtil getUtil = new GetUtil();
        ServletOutputStream op = null;
        ServletContext context = null;

        try {
            String repository = getUtil.getRepositoryPath();

            String filePath = repository + fileName;
            System.out.println("File Path :" + filePath);

            File file = new File(filePath);
            int length = 0;
            op = response.getOutputStream();
            context = getServletConfig().getServletContext();

            String mimetype = context.getMimeType(filePath);

            //  Set the response and go!
            response.setContentType((mimetype != null) ? mimetype : "application/octet-stream");
            response.setContentLength((int) file.length());
            response.setHeader("Content-Disposition", "attachment; filename=/" + filePath + "/");

            //  Stream to the requester.
            byte[] buffer = new byte[1024];
            DataInputStream in = new DataInputStream(new FileInputStream(file));

            while ((in != null) && ((length = in.read(buffer)) != -1)) {
                op.write(buffer, 0, length);
            }

            in.close();
            op.flush();
            op.close();
        } catch (Exception e) {
            System.out.println("Cannot download file");
        }


    }
}
