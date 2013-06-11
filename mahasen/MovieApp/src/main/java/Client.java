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
import org.mahasen.authentication.ClientLogin;
import org.mahasen.authentication.ClientLoginData;
import org.mahasen.configuration.ClientConfiguration;

public class Client {

    private ClientLoginData loginData;

    public Client(){
       this.initialize();
    }

    private void initialize(){
        {
               ClientLogin clientLogin = new ClientLogin();
               ClientConfiguration clientConfiguration = ClientConfiguration.getInstance();


                    System.setProperty("javax.net.ssl.trustStore", clientConfiguration.getTrustStorePath());
                    System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
                    System.setProperty("javax.net.ssl.trustStoreType", "JKS");

               try {
                  setLoginData(clientLogin.remoteLogin(Configuration.HOST+":"+ Configuration.PORT, Configuration.USER_NAME, Configuration.PASSWORD));
               } catch (Exception e) {
                  e.printStackTrace();
               }

                    boolean isLogged = getLoginData().isLoggedIn();
                    System.out.println(" Is Logged : " + isLogged);
        }

}

    public static void main(String[] args) {
        Client client = new Client();

    }

    public ClientLoginData getLoginData() {
        return loginData;
    }

    public void setLoginData(ClientLoginData loginData) {
        this.loginData = loginData;
    }
}
