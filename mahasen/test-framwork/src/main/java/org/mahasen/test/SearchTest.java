package org.mahasen.test;

import org.mahasen.client.AuthenticationExceptionException;
import org.mahasen.client.Search;
import org.mahasen.exception.MahasenClientException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: shelan
 * Date: 8/16/11
 * Time: 11:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class SearchTest extends TestRun {

    Search search;
    private long totalTime;

    public SearchTest() {
        super.initialize();
        search = new Search(super.clientLoginData);
    }

   /* public static void main(String args[]){

        SearchTest org.mahasen = new SearchTest();
        SearchBean bean1 = new SearchBean("fileName","22.pdf", null, false);
        SearchBean bean2 = new SearchBean("fileSize","5", "30", true);
        ArrayList<SearchBean> list = new ArrayList<SearchBean>();
        list.add(bean1);
        list.add(bean2);
    }*/

    public static void main(String[] args) {
       SearchTest test = new SearchTest();

       try {

           //org.mahasen.searchTagTest();
           //org.mahasen.searchPropertyTest(TestConfig.UPLOAD_FOLDER);

           test.rangeSearch(TestConfig.UPLOAD_FOLDER);

           //org.mahasen.searchTagTest("McKeesport");

       } catch (Exception e) {


       }
    }

    public void searchTagTest()
            throws IOException, MahasenClientException, URISyntaxException {


        System.out.println("Tag search");

            try {


                for(int i=0; i<1;i++) {

                    String separatedTag = new TestRun().createTags(1);
                final long startTime = System.currentTimeMillis();

                search.tagSearch(separatedTag);

                final long finishTime = System.currentTimeMillis();

                long timeConsumed = finishTime-startTime;

                totalTime = totalTime +timeConsumed ;

                System.out.println("Time to Delete job no  in milliseconds : " + timeConsumed);
                System.out.println("totoal time upto now :" +totalTime);

                 }
            System.out.println("\nAverage time taken in milliseconds:" + totalTime/10);

            } catch (Exception e) {
                System.out.println("exception");
            }
        }


    public void searchPropertyTest(String uploadingFilesFolder) {

        File folder = new File(uploadingFilesFolder);
        File[] files = folder.listFiles();

        long totalTime = 0;

        for (int i = 0; i < files.length; i++) {

                 Random random = new Random();
                 int randomNo = random.nextInt(10);
                final long startTime = System.currentTimeMillis();
            try {

                switch (i%4){
                    case 0 :search.search(TestConfig.FILE_SIZE, String.valueOf(files[i].length()));
                        break;

                    case 1:
                        search.search(String.valueOf(i), String.valueOf(random.nextInt(10)));
                        break;
                    case 2 : search.search(String.valueOf(i), String.valueOf(random.nextInt(10)));
                        break;
                    case 3:search.search(TestConfig.UPLOADED_DATE, getDate());
                        break;

                }


            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            final long finishTime = System.currentTimeMillis();

            long timeConsumed = finishTime - startTime;
            totalTime = totalTime + timeConsumed;

            System.out.println("Time to upload job no :" + i + "in milliseconds " + timeConsumed);
        }
        
        System.out.println("\nAverage time taken in milliseconds:" + totalTime/files.length);

        System.out.println("\nAverage time taken in milliseconds:" + totalTime/files.length);

    }

    public void rangeSearch(String uploadingFilesFolder) {
        File folder = new File(uploadingFilesFolder);
        File[] files = folder.listFiles();
        long totalTime = 0;
        Random random = new Random();

        for (int i = 0; i < files.length; i++) {
            final long startTime;
            final long finishTime;
            if(i%2 ==0 ) {
                try {
                    int randomNum1 = random.nextInt(1);
                    int randomNum2 = random.nextInt(1);

                    String initialValue =  files[randomNum1].getName();
                    String finalValue = files[randomNum2].getName();

                    if(initialValue.compareTo(finalValue)  <= 0){

                        System.out.println("&&&&&&&&&&&& logic correct&&&&&&&&&"+initialValue +" "+finalValue);
                        startTime = System.currentTimeMillis();
                        search.rangeSearch(TestConfig.FILE_NAME, initialValue, finalValue);

                        finishTime = System.currentTimeMillis();

                        long timeConsumed = finishTime - startTime;
                        totalTime = totalTime + timeConsumed;

                        System.out.println("Time to upload job no :" + i + " " + timeConsumed);
                    }  else {
                        startTime = System.currentTimeMillis();
                        search.rangeSearch(TestConfig.FILE_NAME, finalValue, initialValue);

                        finishTime = System.currentTimeMillis();

                        long timeConsumed = finishTime - startTime;
                        totalTime = totalTime + timeConsumed;

                        System.out.println("Time to search job no :" + i + " " + timeConsumed);
                    }


                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            } else {
                try {
                String initialValue = String.valueOf(random.nextInt(1));
                String lastValue = String.valueOf(random.nextInt(1));
                if(initialValue.compareTo(lastValue)  <= 0) {
                    System.out.println("&&&&&&&&&&&& logic correct&&&&&&&&&"+ initialValue +" "+lastValue);
                        startTime = System.currentTimeMillis();
                        search.rangeSearch(String.valueOf(i),initialValue, lastValue);

                    finishTime = System.currentTimeMillis();

                        long timeConsumed = finishTime - startTime;
                        totalTime = totalTime + timeConsumed;

                        System.out.println("Time to search job no :" + i + " " + timeConsumed);

                }   else {
                    startTime = System.currentTimeMillis();
                    search.rangeSearch(String.valueOf(i), lastValue, initialValue);


                    finishTime = System.currentTimeMillis();

                        long timeConsumed = finishTime - startTime;
                        totalTime = totalTime + timeConsumed;

                        System.out.println("Time to upload job no :" + i + " " + timeConsumed);
                }

               } catch (URISyntaxException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (AuthenticationExceptionException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (MahasenClientException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
            }



        }

        System.out.println("\nAverage time taken in seconds:" + totalTime/(files.length));
    }

    public String getDate() {

        String date = null;

        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        System.out.println(year);

        String month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1);
        if (String.valueOf(month).length() != 2) {
            month = "0".concat(month);
        }
        System.out.println(month);

        date = String.valueOf(Calendar.getInstance().get(Calendar.DATE));
        if (String.valueOf(date).length() != 2) {
            date = "0".concat(date);
        }
        System.out.println(date);


        return date;
    }


}