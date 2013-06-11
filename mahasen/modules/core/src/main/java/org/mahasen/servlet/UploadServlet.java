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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mahasen.MahasenConstants;
import org.mahasen.MahasenManager;
import org.mahasen.exception.MahasenConfigurationException;
import org.mahasen.exception.MahasenException;
import org.mahasen.util.PutUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import rice.p2p.past.PastException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class UploadServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(PutUtil.class);

    /**
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
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

        Enumeration parameters = request.getParameterNames();
        Hashtable<String, String> properties = new Hashtable<String, String>();

        String fileName = "";
        String tags = "";
        String folderStructure = "";

        while (parameters.hasMoreElements()) {

            String key = String.valueOf(parameters.nextElement());

            // extract required data from request parameters
            if (key.equals(MahasenConstants.TAGS)) {
                tags = request.getParameter(key);
            } else if (key.equals(MahasenConstants.FILE_NAME)) {
                fileName = request.getParameter(key);
            } else {
                //extract user defined properties
                properties.put(key, request.getParameter(key));
            }

        }

        InputStream inputStream = request.getInputStream();

        log.debug("##################PARAMS######################");
        System.out.println(request.getParameterMap());
        System.out.println(request.getHeader("testHeader"));
        log.debug("##################PARAMS######################");
        log.info("tags taken from user " + tags);

        MahasenManager servicemanager = new MahasenManager();

        try {
            servicemanager.put(inputStream, fileName, tags, properties);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (PastException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MahasenConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MahasenException e) {
            response.sendError(900, e.getMessage());
            return;
        }
    }
}

