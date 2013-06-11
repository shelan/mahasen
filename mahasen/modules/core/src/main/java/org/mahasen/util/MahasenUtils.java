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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;


public class MahasenUtils {

    /**
     * @param ip
     * @return
     * @throws UnknownHostException
     */
    public static InetAddress convertToInetAddress(String ip) throws UnknownHostException {

        if (ip != null) {

            return InetAddress.getByName(ip);

        } else {
            throw new UnknownHostException("Host is not valid");
        }
    }

    /**
     * @param url
     * @return
     * @throws MalformedURLException
     */
    public static URL convertToURL(String url) throws MalformedURLException {

        if (url != null) {

            return new URL(url);

        } else {
            throw new MalformedURLException("URL is not valid");
        }
    }

    /**
     * @param input
     * @return
     */
    public static String stripBrackets(String input) {

        if (input != null) {

            return input.substring(1, input.length() - 1);

        } else {
            return null;
        }
    }
}
