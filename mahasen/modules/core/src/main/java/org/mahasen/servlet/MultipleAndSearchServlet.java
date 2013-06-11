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

import org.mahasen.MahasenManager;
import org.mahasen.exception.MahasenException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class MultipleAndSearchServlet extends HttpServlet {
    /**
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Vector<String> results = null;

        //Map searchParameters = request.getParameterMap();
        Hashtable<String, Vector<String>> searchParameters = new Hashtable<String, Vector<String>>();

        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String propertyName = String.valueOf(paramNames.nextElement());
            String paramValues[] = request.getParameterValues(propertyName);
            searchParameters.put(propertyName, new Vector<String>(Arrays.asList(paramValues)));
        }

        MahasenManager serviceManager = new MahasenManager();
        try {
            results = serviceManager.multipleAndSearch(searchParameters);
        } catch (MahasenException e) {
            response.sendError(900, e.getMessage());
            return;
        } catch (InterruptedException e) {
            response.sendError(901, e.getMessage());
            return;
        }

        System.out.println("search result" + results.toString());
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        out.println(results);

    }

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
}
