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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.mahasen.client.AuthenticationAdminStub;
import org.mahasen.client.AuthenticationExceptionException;

import java.net.URL;
import java.rmi.RemoteException;


public class ClientLogin {

    ClientLoginData clientLoginData;
    ClientLogin clientLogin;

    /**
     * @param data
     * @return
     * @throws AxisFault
     * @throws RemoteException
     * @throws AuthenticationExceptionException
     *
     */
    public ClientLoginData logIn(ClientLoginData data) throws AxisFault,
            RemoteException, AuthenticationExceptionException {
        ConfigurationContext ctx =
                ConfigurationContextFactory
                        .createConfigurationContextFromFileSystem(null, null);
        String serviceEPR = "https://" + data.getHostNameAndPort() +
                "/services/AuthenticationAdmin";
        AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(ctx,
                serviceEPR);
        ServiceClient client = authAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        boolean isLogged = false ;
                 try{
       isLogged = authAdminStub.login(data.getUserName(),
                data.getPassWord(),
                new URL(serviceEPR).getHost());
                 }catch (Exception e){

                 }

        data.setIsLoggedIn(isLogged);
        String cookie = (String) authAdminStub
                ._getServiceClient()
                .getServiceContext()
                .getProperty(HTTPConstants.COOKIE_STRING);
        data.setCookie(cookie);
        return data;

    }

    /**
     * @param data
     * @return
     * @throws AxisFault
     * @throws RemoteException
     * @throws AuthenticationExceptionException
     *
     */
    public ClientLoginData logOut(ClientLoginData data)
            throws AxisFault, RemoteException, AuthenticationExceptionException {
        ConfigurationContext ctx =
                ConfigurationContextFactory
                        .createConfigurationContextFromFileSystem(null, null);
        String serviceEPR = "https://" + data.getHostNameAndPort() +
                "/services/AuthenticationAdmin";
        AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(ctx,
                serviceEPR);
        ServiceClient client = authAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                data.getCookie());

        authAdminStub.logout();
        data.setIsLoggedIn(false);
        data.setCookie(null);
        return data;
    }

    /**
     * @param hostAndPort
     * @param uName
     * @param pWord
     * @return
     * @throws AuthenticationExceptionException
     *
     * @throws RemoteException
     */
    public ClientLoginData remoteLogin(String hostAndPort, String uName, String pWord)
            throws AuthenticationExceptionException, RemoteException {
        clientLoginData = new ClientLoginData();
        clientLoginData.setHostNameAndPort(hostAndPort);
        clientLoginData.setUserName(uName);
        clientLoginData.setPassWord(pWord);

        clientLogin = new ClientLogin();
        return clientLogin.logIn(clientLoginData);


    }

    /**
     * @param hostAndPort
     * @return
     * @throws AuthenticationExceptionException
     *
     * @throws RemoteException
     */
    public ClientLoginData remoteLogOut(String hostAndPort)
            throws AuthenticationExceptionException, RemoteException {
        clientLoginData = new ClientLoginData();
        clientLoginData.setHostNameAndPort(hostAndPort);

        clientLogin = new ClientLogin();
        return clientLogin.logOut(clientLoginData);


    }


}
