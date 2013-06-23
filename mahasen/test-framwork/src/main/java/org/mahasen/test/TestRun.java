package org.mahasen.test;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.mahasen.authentication.ClientLogin;
import org.mahasen.authentication.ClientLoginData;
import org.mahasen.configuration.ClientConfiguration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: shelan
 * Date: 8/15/11
 * Time: 4:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestRun {

   ClientLoginData clientLoginData ;
   String[] completeTagArray=null;

   HashMap<String, ArrayList<String>> nameValuePair = new HashMap<String, ArrayList<String>>();


    public TestRun(){
        this.initialize();
    }

    public void initialize()

    {
       ClientLogin clientLogin = new ClientLogin();
       ClientConfiguration clientConfiguration = ClientConfiguration.getInstance();

            System.setProperty("javax.net.ssl.trustStore", clientConfiguration.getTrustStorePath());
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
            System.setProperty("javax.net.ssl.trustStoreType", "JKS");

       try {
           clientLoginData = clientLogin.remoteLogin(TestConfig.HOST+":"+TestConfig.PORT, TestConfig.USER_NAME, TestConfig.PASSWORD);
       } catch (Exception e) {
          e.printStackTrace();
       }

            Boolean isLogged = clientLoginData.isLoggedIn();
            System.out.println(" Is Logged : " + isLogged);


        loadTags();
        //loadProperties();

   }

    public void loadTags(){

        String completeTags = "";

        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(TestConfig.TAGS_FILE));

        String str;

            while ((str = in.readLine()) != null) {

                completeTags = completeTags.concat(str);

            }

             completeTagArray = completeTags.split(",");

            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /*public void loadProperties(){

        Hashtable<String, String[]> properties = new Hashtable<String, String[]>();

        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(TestConfig.PROP_FILE));

        String str;

            while ((str = in.readLine()) != null)

            {
                int colon=str.indexOf(":");
                String key=str.substring(0,colon+1);
                System.out.println("Key taken from the file"+key);
                String values=str.substring(colon+1);
                System.out.println("Values taken from the file"+values);
                properties.put(key,values.split(","));

            }

            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }*/


    Random random = new Random();

    public String createTags(int x){

        String tags = "";

        for (int i = 0; i < x; i++) {
            int randomNum = random.nextInt(completeTagArray.length);

            if (i == 0) {
                tags = tags.concat(completeTagArray[randomNum]);
            } else

            {
                tags = tags.concat("," + completeTagArray[randomNum]);
            }

        }
        return tags;
    }


    public List<NameValuePair> getNameValuePairs(){

        ArrayList<NameValuePair> nameValuePairsList = new ArrayList<NameValuePair>();

        // adding random values to hash map
        for(int i =0 ; i <100 ; i++){

            Random random = new Random();
            int randomNo = random.nextInt(10);

         //  NameValuePair nameValuePair = new BasicNameValuePair("name"+i,"value"+randomNo);
           NameValuePair nameNumberPair = new BasicNameValuePair(String.valueOf(i),String.valueOf(randomNo));
          // nameValuePairsList.add(nameValuePair);
            nameValuePairsList.add(nameNumberPair);
        }

          return nameValuePairsList;



    }

}
