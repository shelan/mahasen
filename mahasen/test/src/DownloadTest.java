import org.mahasen.client.Download;
import org.mahasen.exception.MahasenClientException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by IntelliJ IDEA.
 * User: shelan
 * Date: 8/16/11
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class DownloadTest extends TestRun{

    private Download download;
    private long totalTime;

    public DownloadTest(){
           super.initialize();
     download = new Download(super.clientLoginData);
    }

    public void DownloadTest(String sourceFolder) throws IOException, MahasenClientException, URISyntaxException {

        File folder = new File(sourceFolder);
               File[] files = folder.listFiles();

        for (int i = 0; i < files.length; i++) {
                final long startTime = System.nanoTime();
            download.download(files[i].getName());
              final long finishTime = System.nanoTime();
             long timeConsumed = finishTime-startTime;
            totalTime = totalTime +timeConsumed ;

            System.out.println("Time to Download job no :"+i+" in seconds : "+timeConsumed/1000000000.0);
            System.out.println("totoal time upto now :" +totalTime/1000000000.0);
        }
        System.out.println("\nAverage time taken :" + (totalTime/(files.length*1000000000.0)));
    }

    public static void main(String[] args) {
        DownloadTest test = new DownloadTest();
        try {
            test.DownloadTest(TestConfig.UPLOAD_FOLDER);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MahasenClientException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
