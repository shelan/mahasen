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

package org.mahasen.authentication;


public class ClientLoginData {

    private String userName;
    private String passWord;
    private String hostNameAndPort;
    private boolean loggedIn = false;
    private String cookie;

    /**
     * @return
     */
    public String getCookie() {
        return cookie;
    }

    /**
     * @param cookie
     */
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    /**
     * @return
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * @param loggedIn
     */
    public void setIsLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    /**
     * @return
     */
    public String getHostNameAndPort() {
        return hostNameAndPort;
    }

    /**
     * @param hostNameAndPort
     */
    public void setHostNameAndPort(String hostNameAndPort) {
        this.hostNameAndPort = hostNameAndPort;
    }

    /**
     * @return
     */
    public String getPassWord() {
        return passWord;
    }

    /**
     * @param passWord
     */
    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    /**
     * @return
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @param userName
     * @param password
     * @return
     */
    public String getUserId(String userName, String password) {

        return userName.concat(password);

    }
}
