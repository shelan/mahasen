package org.mahasen.test;

import org.mahasen.client.AuthenticationExceptionException;
import org.mahasen.client.UpdateMetadata;
import org.mahasen.exception.MahasenClientException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by IntelliJ IDEA.
 * User: hiru
 * Date: Sep 1, 2011
 * Time: 2:46:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateTest extends TestRun {

    private UpdateMetadata update;
    private long totalTime;


    public UpdateTest(){
           super.initialize();
     update = new UpdateMetadata(super.clientLoginData);
    }


    public void UpdateTest(String sourceFolder) throws IOException {

        UpdateTest test = new UpdateTest();
        File folder = new File(sourceFolder);
        File[] files = folder.listFiles();

        for (int i = 0; i < files.length; i++) {

            String tagsToBeApplied=test.createTags(5);
            System.out.println("Tags to be updated  "+tagsToBeApplied );


            //List<NameValuePair> properties = new ArrayList<NameValuePair>();


            final long startTime = System.currentTimeMillis();
            try {
                update.setAddedProperties(getNameValuePairs());
                update.updateMetadata(files[i].getName(),tagsToBeApplied);

            } catch (URISyntaxException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (AuthenticationExceptionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (MahasenClientException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            final long finishTime = System.currentTimeMillis();
            long timeConsumed = finishTime-startTime;
            totalTime = totalTime +timeConsumed ;

            System.out.println("Time to update job no :"+i+" "+timeConsumed);
        }
        System.out.println("\nAverage time taken :" + totalTime/(files.length));
    }

    public static void main(String[] args) {
        UpdateTest test = new UpdateTest();
        try {
            test.UpdateTest(TestConfig.UPLOAD_FOLDER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
