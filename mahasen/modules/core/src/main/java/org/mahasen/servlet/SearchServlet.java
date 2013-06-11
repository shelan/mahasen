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
import java.util.Vector;


public class SearchServlet extends HttpServlet {

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

        Vector results = null;

        if (Boolean.valueOf(request.getParameter("isRangeBase")).equals(true)) {
            try {
                results = rangeBaseSearch(request);
            } catch (MahasenException e) {
                throw new ServletException(e);
            } catch (InterruptedException e) {
                throw new ServletException(e);
            }
            System.out.println("search result" + results.toString());
            PrintWriter out = response.getWriter();
            response.setContentType("text/html");
            out.println(results);

        } else {

            try {
                results = search(request);
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
    }

    /**
     * @param request
     * @return
     * @throws MahasenException
     * @throws InterruptedException
     */
    private Vector<String> search(HttpServletRequest request)
            throws MahasenException, InterruptedException {

        String propertyName = request.getParameter("propertyName");
        String propertyValue = request.getParameter("propertyValue");
        Vector<String> searchResult = null;
        MahasenManager servicemanager = new MahasenManager();

        searchResult = servicemanager.search(propertyName, propertyValue);
        return searchResult;
    }

    /**
     * @param request
     * @return
     * @throws MahasenException
     * @throws InterruptedException
     */
    private Vector<String> rangeBaseSearch(HttpServletRequest request)
            throws MahasenException, InterruptedException {

        String propertyName = request.getParameter("propertyName");
        String initialValue = request.getParameter("initialValue");
        String lastValue = request.getParameter("lastValue");
        Vector<String> searchResult = null;
        MahasenManager servicemanager = new MahasenManager();

        searchResult = servicemanager.rangeSearch(propertyName, initialValue, lastValue);
        return searchResult;
    }
}
