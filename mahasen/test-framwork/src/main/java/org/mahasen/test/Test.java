package org.mahasen.test;

/**
 * .
 * User: shelan
 * Date: 6/23/13
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class Test {

    public static void main(String[] args) throws Exception{

        String testToRun = "";

        if("upload".equalsIgnoreCase(testToRun)){
            UploadTest test = new UploadTest();
            test.uploadTest(TestConfig.UPLOAD_FOLDER ,"/shelan");
        }
        if("delete".equalsIgnoreCase(testToRun)){
            DeleteTest test = new DeleteTest();
            test.deleteTest(TestConfig.UPLOAD_FOLDER);
        }
        if("download".equalsIgnoreCase(testToRun)){
            DownloadTest test = new DownloadTest();
            test.DownloadTest(TestConfig.UPLOAD_FOLDER);
        }
        if("search".equalsIgnoreCase(testToRun)){

        }
        if("update".equalsIgnoreCase(testToRun)){

        }

    }

}
