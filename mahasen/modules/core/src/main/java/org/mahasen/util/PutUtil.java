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
package org.mahasen.util;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.mahasen.MahasenConstants;
import org.mahasen.MahasenPastryApp;
import org.mahasen.configuration.MahasenConfiguration;
import org.mahasen.exception.MahasenConfigurationException;
import org.mahasen.exception.MahasenException;
import org.mahasen.filemanagement.MahasenFileSplitter;
import org.mahasen.messaging.MahasenMsg;
import org.mahasen.node.MahasenNodeManager;
import org.mahasen.resource.MahasenResource;
import org.mahasen.thread.MahasenReplicateWorker;
import org.mahasen.thread.MahasenUploadWorker;
import org.mahasen.thread.ReplicateRequestStarter;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.PastException;
import rice.pastry.Id;
import rice.pastry.leafset.LeafSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class PutUtil extends AbstractCommonUtil {

    private static Log log = LogFactory.getLog(PutUtil.class);
    MahasenPastryApp mahasenPastryApp = mahasenManager.getMahasenApp();
    MahasenResource mahasenResource;
    MahasenConfiguration mahasenConfiguration = MahasenConfiguration.getInstance();

    public static ConcurrentHashMap<String, AtomicInteger> storedNoOfParts = new ConcurrentHashMap<String, AtomicInteger>();

    private static ConcurrentHashMap<String, Integer> replicaReference = new ConcurrentHashMap<String, Integer>();

    Thread uploadThread = null;

    String jobId = UUIDGenerator.getUUID();

    public void put(InputStream inputStream, String fileName, String tags, Hashtable<String,
            String> userDefinedProperties)
            throws InterruptedException, RegistryException, PastException,
            IOException, MahasenConfigurationException, MahasenException {

        if (inputStream != null) {


            String repository = super.getRepositoryPath();
            File tempDirectory = new File(repository + MahasenConstants.TEMP_FOLDER);

            // storing in the local file system first
            if (!tempDirectory.exists()) {

                tempDirectory = new File(repository + MahasenConstants.TEMP_FOLDER);
                tempDirectory.mkdir();

            }
            File uploadFileTempDir = new File(mahasenConfiguration.getTempUploadFolderPath() + fileName);
            uploadFileTempDir.mkdir();
            File filetoUpload = new File(uploadFileTempDir.getAbsolutePath() + "/" + fileName);
            FileOutputStream outputStream = new FileOutputStream(filetoUpload);
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
            outputStream.close();

            Id resourceId = Id.build(String.valueOf((MahasenConstants.ROOT_REGISTRY_PATH
                    + fileName).hashCode()));

            if (mahasenManager.lookupDHT(resourceId) != null) {


                File[] tempFiles = uploadFileTempDir.listFiles();

                for (File tempFile : tempFiles) {


                    boolean deleted = tempFile.delete();

                    if (!deleted) {

                        System.out.println("Failed to delete the file" + tempFile);
                    }
                }

                uploadFileTempDir.delete();

                throw new MahasenException("File name already exists in the system");

            }

            mahasenResource = createMahasenResource(resourceId);
            mahasenResource = addSystemMetadata(mahasenResource, resourceId, filetoUpload);
            mahasenResource = addUserDefinedMetadata(mahasenResource, userDefinedProperties);
            mahasenResource = addTags(mahasenResource, tags);
            

            secureUpload(filetoUpload, resourceId);

            log.info("Added resource Metadata to registry at :" + MahasenConstants.ROOT_REGISTRY_PATH
                    + resourceId.hashCode());


            File[] tempFiles = uploadFileTempDir.listFiles();

            for (File tempFile : tempFiles) {


                boolean deleted = tempFile.delete();

                if (!deleted) {

                    System.out.println("Failed to delete the file" + tempFile);
                }
            }

            uploadFileTempDir.delete();
        }
    }


    /**
     * @param mahasenResource
     * @param tagString
     * @return
     */
    private MahasenResource addTags(MahasenResource mahasenResource, String tagString) {
        String tags[] = tagString.split(",");

        log.info("tags taken from user " + tags.toString());
        if (tags != null) {
            for (String tag : tags) {
                mahasenResource.addTag(tag.trim());
            }
        } else {
            log.debug("tags not found");
        }
        return mahasenResource;
    }

    /**
     * @param file
     * @throws InterruptedException
     * @throws RegistryException
     * @throws PastException
     * @throws IOException
     */
    public void secureUpload(File file, Id resourceId)
            throws InterruptedException, RegistryException, PastException,
            IOException, MahasenConfigurationException, MahasenException {

        // get the IP addresses pool to upload files.
        Vector<String> nodeIpsToPut = getNodeIpsToPut();

        MahasenFileSplitter mahasenFileSplitter = new MahasenFileSplitter();
        mahasenFileSplitter.split(file);
        HashMap<String, String> fileParts = mahasenFileSplitter.getPartNames();

        mahasenResource.addPartNames(fileParts.keySet().toArray(new String[fileParts.size()]));
        Random random = new Random();

        for (String currentPartName : fileParts.keySet()) {
            File splittedFilePart = new File(fileParts.get(currentPartName));
            int randomNumber = random.nextInt(nodeIpsToPut.size());
            String nodeIp = nodeIpsToPut.get(randomNumber);

            try {
                setTrustStore();
                URI uri = null;

                ArrayList<NameValuePair> qparams = new ArrayList<NameValuePair>();
                qparams.add(new BasicNameValuePair("splittedfilename", splittedFilePart.getName()));
                uri = URIUtils.createURI("https", nodeIp + ":" +
                        MahasenConstants.SERVER_PORT, -1, "/mahasen/upload_request_ajaxprocessor.jsp",
                        URLEncodedUtils.format(qparams, "UTF-8"), null);

                MahasenUploadWorker uploadWorker = new MahasenUploadWorker(uri, currentPartName,
                        splittedFilePart, mahasenResource, nodeIp);
                uploadThread = new Thread(uploadWorker);
                uploadWorker.setJobId(jobId);

                //keep track of uploading parts
                AtomicInteger noOfParts = new AtomicInteger(0);
                storedNoOfParts.put(jobId, noOfParts);

                uploadThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final BlockFlag blockFlag = new BlockFlag(true, 6000);
        while (true) {

            AtomicInteger noOfParts = storedNoOfParts.get(jobId);
            if (noOfParts.get() == fileParts.size()) {
                storedNoOfParts.remove(uploadThread.getId());
                System.out.println("uploaded no of parts " + noOfParts + "out of " + fileParts.size() + "going out " +
                        "#####Thread id:" + uploadThread.getId());
                blockFlag.unblock();
                break;
            }

            if (blockFlag.isBlocked()) {
                mahasenManager.getNode().getEnvironment().getTimeSource().sleep(10);
            } else {
                throw new MahasenException("Time out in uploading " + file.getName());
            }
        }

        mahasenManager.insertIntoDHT(resourceId, mahasenResource, false);
        mahasenManager.insertTreeMapIntoDHT(resourceId, mahasenResource, false);

        ReplicateRequestStarter replicateStarter = new ReplicateRequestStarter(mahasenResource);
        Thread replicateThread = new Thread(replicateStarter);
        replicateThread.start();
    }


    /**
     * @param mahasenResource
     * @param resourceId
     * @param sourceFile
     * @return
     */
    private MahasenResource addSystemMetadata(MahasenResource mahasenResource, Id resourceId, File sourceFile) {

        mahasenResource.addProperty(MahasenConstants.FILE_NAME, sourceFile.getName());
        //mahasenResource.addProperty(MahasenConstants.FILE_SIZE, String.valueOf(sourceFile.length()/(1024*1024)));
        mahasenResource.setFileSize((int)sourceFile.length()/(1024*1024));

        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        String month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH) + 1);
        if (String.valueOf(month).length() != 2) {
            month = "0".concat(month);
        }

        String date = String.valueOf(Calendar.getInstance().get(Calendar.DATE));
        if (String.valueOf(date).length() != 2) {
            date = "0".concat(date);
        }

        mahasenResource.setUploadedDate(Integer.valueOf(year.concat(month).concat(date)));

        // Add the file path to retrive later
        mahasenResource.addProperty(MahasenConstants.FILE_PATH,
                String.valueOf(MahasenConstants.ROOT_REGISTRY_PATH + resourceId.toString()));

        return mahasenResource;
    }

    /**
     * @param mahasenResource
     * @param folderStructure
     * @return
     */
    private MahasenResource addFolderStructure(MahasenResource mahasenResource, String folderStructure) {

        mahasenResource.addProperty(MahasenConstants.FOLDER_STRUCTURE, folderStructure);
        return mahasenResource;
    }

    /**
     * @param mahasenResource
     * @param userDefinedProperties
     * @return
     */
    private MahasenResource addUserDefinedMetadata(MahasenResource mahasenResource,
                                                   Hashtable<String, String> userDefinedProperties) {

        Hashtable<String, String> userProperties = removeSystemMetadataFromUserMetadata(userDefinedProperties);
        mahasenResource.addAllPropeties(userProperties);
        return mahasenResource;
    }

    /**
     * @return
     */
    private Vector<NodeHandle> getNodeHandlesToPut() {
        LeafSet leafSet = mahasenManager.getNode().getLeafSet();
        Vector<NodeHandle> nodeHandles = new Vector<NodeHandle>();

        for (int i = -leafSet.ccwSize(); i <= leafSet.cwSize(); i++) {
            if (!nodeHandles.contains(leafSet.get(i))) {
                nodeHandles.add(leafSet.get(i));
            }
        }
        return nodeHandles;
    }

    /**
     * @return
     */
    private Vector<String> getNodeIpsToPut() throws InterruptedException {

        Vector<NodeHandle> nodeHandles = getNodeHandlesToPut();
        Vector<String> nodeIps = new Vector<String>();
        MahasenMsg msg = new MahasenMsg();


        msg.setSendersNodeHandle(mahasenManager.getNode().getLocalHandle());

        for (NodeHandle nodeHandle : nodeHandles) {
            BlockFlag blockFlag = new BlockFlag(true, 1000);

            mahasenPastryApp.sendRequestForIp(nodeHandle, msg);
            while (!mahasenPastryApp.isResultAvailable()) {
                if (blockFlag.isBlocked()) {
                    mahasenManager.getNode().getEnvironment().getTimeSource().sleep(10);
                } else {
                    continue;
                }
            }

            String ip = mahasenPastryApp.getResultIpVector().remove(0).toString();

            if (!nodeIps.contains(ip))
                nodeIps.add(ip);

        }

        return nodeIps;
    }


    /**
     * @param part
     * @param parentFileName
     * @param partName
     * @throws MahasenConfigurationException
     * @throws PastException
     * @throws InterruptedException
     */
    public void replicateFilePart(File part, String parentFileName, String partName)
            throws MahasenConfigurationException, PastException, InterruptedException, MahasenException {
        Vector<String> nodeIpsToPut = getNodeIpsToPut();


        String resourcePath = MahasenConstants.ROOT_REGISTRY_PATH + parentFileName;
        Id parentFileId = Id.build(String.valueOf(resourcePath.hashCode()));

        while (mahasenManager.lookupDHT(parentFileId) == null) {
            Thread.sleep(1000);
        }
        MahasenResource mahasenResourceToUpdate = mahasenManager.lookupDHT(parentFileId);

        getReplicaReference().put(partName, 0);

        Random random = new Random();

        Hashtable<String, Vector<String>> spittedPartsStoredIps = mahasenResourceToUpdate.getSplittedPartsIpTable();
        Vector<String> currentPartStoredIps = spittedPartsStoredIps.get(partName);
        List<String> replicateIds = new ArrayList<String>();
        final BlockFlag blockFlag = new BlockFlag(true, 3000);

        while (true) {

            if (nodeIpsToPut.size() >= MahasenConstants.NUMBER_OF_REPLICAS + 1) {
                if (getReplicaReference().get(partName) == MahasenConstants.NUMBER_OF_REPLICAS) {
                    log.info("Success in replicating :" + getReplicaReference().get(partName) + " parts");
                    break;
                }

                String nodeIp = nodeIpsToPut.get(random.nextInt(nodeIpsToPut.size()));

                if (!currentPartStoredIps.contains(nodeIp) && !replicateIds.contains(nodeIp)) {
                    replicateIds.add(nodeIp);
                    try {
                        sendReplicateRequest(nodeIp, part, partName, mahasenResourceToUpdate, parentFileId);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            } else {

                for (String ip : nodeIpsToPut) {
                    if (!currentPartStoredIps.contains(ip)) {
                        try {
                            sendReplicateRequest(ip, part, partName, mahasenResourceToUpdate, parentFileId);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }
                blockFlag.unblock();
                break;

            }

            if (blockFlag.isBlocked()) {

                mahasenManager.getNode().getEnvironment().getTimeSource().sleep(10);
            } else {
                throw new MahasenException("Time out in storing " + part.getName());
            }
            Thread.sleep(100);
        }
    }

    /**
     * @param nodeIp
     * @param part
     * @param partName
     * @param mahasenResourceToUpdate
     * @param parentFileId
     * @throws MahasenConfigurationException
     * @throws URISyntaxException
     */
    private void sendReplicateRequest(String nodeIp, File part, String partName,
                                      MahasenResource mahasenResourceToUpdate, Id parentFileId)
            throws MahasenConfigurationException, URISyntaxException {

        setTrustStore();

        URI uri = null;

        ArrayList<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("splittedfilename", part.getName()));
        uri = URIUtils.createURI("https", nodeIp + ":" +
                MahasenConstants.SERVER_PORT, -1, "/mahasen/upload_request_ajaxprocessor.jsp",
                URLEncodedUtils.format(qparams, "UTF-8"), null);

        System.out.println("Target Address for replicating : " + uri);
        PutUtil.incrementNoOfReplicas(partName);
        MahasenReplicateWorker replicateWorker = new MahasenReplicateWorker(uri, partName,
                part, mahasenResourceToUpdate, nodeIp, MahasenNodeManager.getInstance(), parentFileId);
        Thread replicateThread = new Thread(replicateWorker);

        replicateThread.start();

    }

    /**
     * @return
     */
    public static ConcurrentHashMap<String, Integer> getReplicaReference() {
        return replicaReference;
    }

    /**
     * @param replicaReference
     */
    public static void setReplicaReference(ConcurrentHashMap<String, Integer> replicaReference) {
        PutUtil.replicaReference = replicaReference;
    }

    /**
     * @param partName
     */
    public synchronized static void incrementNoOfReplicas(String partName) {
        int noOfReplicas = replicaReference.get(partName);
        replicaReference.put(partName, ++noOfReplicas);
    }

    /**
     * @param array
     * @return
     */
    public static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).toUpperCase().substring(1, 3));
        }
        return sb.toString();
    }


}



