package org.mahasen.test;

import org.mahasen.exception.MahasenClientException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * .
 * User: shelan
 * Date: 6/23/13
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class Test{

    public static void main(String[] args) throws Exception {

        String concurrencyLevel = System.getProperty("n", String.valueOf(0));
        String testToRun = System.getProperty("operation", "delete");

        System.out.println("printing properties "+concurrencyLevel + "and" + testToRun);

        if ("upload".equalsIgnoreCase(testToRun)) {
            UploadTest test = new UploadTest();
            test.createUploadFolders(Integer.valueOf(concurrencyLevel));
            for (int i = 0; i < Integer.valueOf(concurrencyLevel); i++) {
                final int finalI = i;
                Thread t = new Thread() {
                    public void run() {
                        UploadTest test = new UploadTest();
                        try {
                            test.uploadTest(TestConfig.UPLOAD_FOLDER+ File.separator+ finalI+"folder", "/shelan");
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (MahasenClientException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
            }

        }
        if ("delete".equalsIgnoreCase(testToRun)) {

            for (int i = 0; i < Integer.valueOf(concurrencyLevel); i++) {
                final int finalI = i;
                Thread t = new Thread() {
                    public void run() {
                        DeleteTest test = new DeleteTest();
                        try {
                            test.deleteTest(TestConfig.UPLOAD_FOLDER + File.separator + finalI + "folder");
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (MahasenClientException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
            }

        }
        if ("download".equalsIgnoreCase(testToRun)) {
            for (int i = 0; i < Integer.valueOf(concurrencyLevel); i++) {
                final int finalI = i;
                Thread t = new Thread() {
                    public void run() {
                        DownloadTest test = new DownloadTest();
                        try {
                            test.DownloadTest(TestConfig.UPLOAD_FOLDER + File.separator + finalI + "folder");
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (MahasenClientException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
            }
        }
        if ("search".equalsIgnoreCase(testToRun)) {
            for (int i = 0; i < Integer.valueOf(concurrencyLevel); i++) {
                final int finalI = i;
                Thread t = new Thread() {
                    public void run() {
                        SearchTest test = new SearchTest();
                        try {
                            test.rangeSearch(TestConfig.UPLOAD_FOLDER + File.separator + finalI + "folder");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
            }

        }
        if ("update".equalsIgnoreCase(testToRun)) {
            for (int i = 0; i < Integer.valueOf(concurrencyLevel); i++) {
                final int finalI = i;
                Thread t = new Thread() {
                    public void run() {
                        UpdateTest test = new UpdateTest();
                        try {
                            test.UpdateTest(TestConfig.UPLOAD_FOLDER+ File.separator+ finalI+"folder");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
            }
        }

    }
}
