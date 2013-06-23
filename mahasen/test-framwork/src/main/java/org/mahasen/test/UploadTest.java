package org.mahasen.test;

import org.mahasen.client.Upload;
import org.mahasen.exception.MahasenClientException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by IntelliJ IDEA.
 * User: shelan
 * Date: 8/15/11
 * Time: 4:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class UploadTest extends TestRun {

    Upload upload ;

    public UploadTest(){
           super.initialize();
     upload = new Upload(super.clientLoginData);
    }

    public void uploadTest(String uploadingFilesFolder ,String folderStructure)
            throws IOException, MahasenClientException, URISyntaxException {

        UploadTest test = new UploadTest();

        File folder = new File(uploadingFilesFolder);
        File[] files = folder.listFiles();

        long totalTime = 0;


        for (int i = 0; i < files.length; i++) {

            String tagsToBeApplied=test.createTags(50);

            System.out.println("Tags to be applied  "+tagsToBeApplied );

            final long startTime = System.nanoTime();

            System.out.println("Uploaling file :" + files[i].getName());

            upload.upload(files[i],tagsToBeApplied,folderStructure,getNameValuePairs());

            final long finishTime = System.nanoTime();

            long timeConsumed = finishTime-startTime;
            totalTime = totalTime +timeConsumed ;

            System.out.println("Time to upload job no :"+i+" in seconds : "+timeConsumed/1000000000.0);

            System.out.println("totoal time upto now :" +totalTime/1000000000.0);
    }
        System.out.println("\nAverage time taken in seconds:" + totalTime/(files.length*1000000000.0));
    }



    public static void main(String[] args) {

        UploadTest test = new UploadTest();

        try {

            test.uploadTest(TestConfig.UPLOAD_FOLDER ,"/shelan");

        } catch (IOException e)

        {
            e.printStackTrace();
        } catch (MahasenClientException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
