/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.mahasen;

public class MahasenConstants {

    public final static String DESCRIPTION = "description";
    public final static String MEDIA_TYPE = "mediaType";
    public final static String FILE_SIZE = "fileSize(MB)";
    public final static String FILE_PATH = "filePath";
    public final static String UPLOADED_DATE = "uploadedDate";
    public final static String SERVER_PORT = "9443";
    public final static String FILE_NAME = "fileName";
    public final static String PART_NAMES = "partNames";
    public final static String FOLDER_STRUCTURE = "folderStructure";
    public final static String TAGS = "tags";
    public final static String ACCESSIBILITY = "accessibility";
    public final static String TEMP_FOLDER = ".temp/";

    //Mashen root registry path
    public final static String ROOT_REGISTRY_PATH = "/mahasen/";

    //property tree related constants
    public final static String STRING_PROPERTY_TREE = "string";
    public final static String INTEGER_PROPERTY_TREE = "integer";

    //number of replicas for each file. ( so the file will have replica copies and the originaly uploaded file)
    public final static int NUMBER_OF_REPLICAS = 2;


}