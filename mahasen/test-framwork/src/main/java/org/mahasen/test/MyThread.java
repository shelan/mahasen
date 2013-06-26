package org.mahasen.test;
/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class MyThread implements Runnable {

    String uploadFolder;
    String param2;
    String action;
    public MyThread(String uploadFolder, String param2, String action){
        this.uploadFolder = uploadFolder;
        this.param2 = param2;
        this.action = action;
    }
    public void run() {

        if(this.action.equals("upload")) {
            UploadTest test = new UploadTest();

            try {
                test.uploadTest(this.uploadFolder, this.param2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(this.action.equals("download")) {
            DownloadTest test = new DownloadTest();

            try {
                test.DownloadTest(this.uploadFolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }if(this.action.equals("update")) {
            UpdateTest test = new UpdateTest();

            try {
                test.UpdateTest(this.uploadFolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } if(this.action.equals("search")) {
            SearchTest test = new SearchTest();

            try {
                ///// there are many types of search options
                test.rangeSearch(this.uploadFolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }  if(this.action.equals("delete")) {
            DeleteTest test = new DeleteTest();

            try {
                test.deleteTest(this.uploadFolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
