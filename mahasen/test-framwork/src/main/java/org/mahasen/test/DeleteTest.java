package org.mahasen.test;

import org.mahasen.client.Delete;
import org.mahasen.exception.MahasenClientException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by IntelliJ IDEA.
 * User: shelan
 * Date: 8/16/11
 * Time: 11:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class DeleteTest extends TestRun{

  private Delete delete;
    private long totalTime;

    public DeleteTest(){
           super.initialize();
     delete = new Delete(super.clientLoginData);
    }

    public void deleteTest(String sourceFolder) throws IOException, MahasenClientException, URISyntaxException {

        File folder = new File(sourceFolder);
               File[] files = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
                final long startTime = System.currentTimeMillis();
            delete.delete(files[i].getName());
            final long finishTime = System.currentTimeMillis();long timeConsumed = finishTime-startTime;
            totalTime = totalTime +timeConsumed ;

            System.out.println("Time to Delete job no :"+i+" in milliseconds : "+timeConsumed);
            System.out.println("totoal time upto now :" +totalTime);
        }
        System.out.println("\nAverage time taken in milliseconds:" + totalTime/files.length);
    }

    public static void main(String[] args) {
        DeleteTest test = new DeleteTest();
        try {
            test.deleteTest(TestConfig.UPLOAD_FOLDER);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MahasenClientException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
