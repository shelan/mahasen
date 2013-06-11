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
import org.mahasen.exception.MahasenException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import rice.p2p.past.PastException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;


public class UpdateServlet extends HttpServlet {

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

        String fileName = "";
        String tags = "";
        Hashtable<String, String> propeties = new Hashtable<String, String>();

        try {

            Enumeration enumeration = request.getParameterNames();

            while (enumeration.hasMoreElements()) {
                String paramName = String.valueOf(enumeration.nextElement());

                if (paramName.equals(MahasenConstants.FILE_NAME))
                    fileName = request.getParameter(paramName);

                else if (paramName.equals(MahasenConstants.TAGS))
                    tags = request.getParameter(paramName);

                else {
                    propeties.put(paramName, request.getParameter(paramName));
                }
            }

            MahasenManager serviceManager = new MahasenManager();
            serviceManager.update(fileName, tags, propeties);

        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (PastException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MahasenException e) {
            response.sendError(900, e.getMessage());
            return;
        }

    }
}