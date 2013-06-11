package org.mahasen.client;

import org.mahasen.authentication.ClientLogin;
import org.mahasen.authentication.ClientLoginData;
import org.mahasen.exception.MahasenClientException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class TestClient {


    public static void main(String[] args) throws IOException, AuthenticationExceptionException, MahasenClientException, URISyntaxException {

        System.setProperty("javax.net.ssl.trustStore",
                "/home/kishanthan/FinalYearProject/wso2greg-3.5.0/resources/security/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        ClientLoginData loginData = new ClientLoginData();
        loginData.setHostNameAndPort("127.0.0.1:9443");
        loginData.setUserName("admin");
        loginData.setPassWord("admin");

        ClientLogin login = new ClientLogin();
        login.logIn(loginData);

        Upload upload = new Upload(loginData);
        upload.upload(new File("/home/kishanthan/Softwares/Free-PASTRY.tgz"), "foo", "/foo/bar", null);

//        Download download = new Download(loginData);
//        download.download("Free-PASTRY.tgz", "/home/kishanthan/Desktop");
//
//        Delete delete = new Delete(loginData);
//        delete.delete("Free-PASTRY.tgz");

    }
}


