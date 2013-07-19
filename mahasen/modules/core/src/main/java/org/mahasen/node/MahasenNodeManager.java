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

package org.mahasen.node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mahasen.*;
import org.mahasen.messaging.MahasenMsg;
import org.mahasen.resource.MahasenResource;
import org.mahasen.thread.TreeUpdateWorker;
import org.wso2.carbon.registry.core.Registry;
import rice.Continuation;
import rice.environment.Environment;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastException;
import rice.pastry.Id;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.IPNodeIdFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import rice.persistence.LRUCache;
import rice.persistence.MemoryStorage;
import rice.persistence.Storage;
import rice.persistence.StorageManagerImpl;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MahasenNodeManager {

    private String baseDir = "mahasen-pastry";

    private MahasenPastImpl mahasenPastApp;

    private MahasenPastImpl mahasenPastTreeApp;

    private File storageFile = null;

    private Environment env = null;

    private PastryIdFactory idf;

    private PastryNode node;

    private static Registry registry;

    private static final Log log = LogFactory.getLog(MahasenNodeManager.class);

    private static MahasenNodeManager nodeManager = new MahasenNodeManager();

    private MahasenPastryApp mahasenApp;

    private MahasenNodeManager() {
    }

    /**
     *
     * @return MahasenNodeManager
     */
    public static MahasenNodeManager getInstance() {

        if (nodeManager == null) {
            nodeManager = new MahasenNodeManager();
        }
        return nodeManager;
    }

    /**
     * @param localport
     * @param localaddress
     * @param bootport
     * @param bootaddress
     * @param registryUrl
     * @param env
     * @throws Exception
     */
    public void bootNode(int localport, InetSocketAddress localaddress, int bootport,
                         InetSocketAddress bootaddress, String registryUrl, Environment env) throws Exception {

        this.env = env;

        env.getParameters().setString("nat_search_policy", "never");

        System.out.println("starting pastry node configuration");
        // Generate the NodeIds Randomly
        NodeIdFactory nidFactory = new IPNodeIdFactory(bootaddress.getAddress(), localport, env);

        // construct the PastryNodeFactory, this is how we use rice.pastry.socket
        PastryNodeFactory factory;

        // this will resolve the localhost problem

        if (bootaddress.getAddress().isLoopbackAddress()) {
            factory = new SocketPastryNodeFactory(nidFactory,
                    bootaddress.getAddress(), localaddress.getPort(), env);
        } else {
            factory = new SocketPastryNodeFactory(nidFactory,
                    localaddress.getPort(), env);
        }

        // construct a node, but this does not cause it to boot
        node = factory.newNode();

        // used for generating PastContent object Ids.
        // this implements the "hash function" for our DHT
        idf = new rice.pastry.commonapi.PastryIdFactory(env);

        // create a different storage root for each node
        String storageDirectory = "./" + baseDir + "/storage" + getNode().getId().hashCode();

        storageFile = new File(storageDirectory);

       // registry = new RemoteRegistry(registryUrl, "admin", "admin");

        //registry = Activator.getRegistryService().getRegistry();

        // create the persistent part
        Storage store = new MahasenStorage(new PastryIdFactory(env), storageDirectory, 4 * 1024 * 1024, getNode()
                .getEnvironment());

        mahasenPastApp = new MahasenPastImpl(getNode(), new StorageManagerImpl(idf, store, new LRUCache(
                new MemoryStorage(idf), 512 * 1024, getNode().getEnvironment())), 3, "PastApp");

        mahasenApp = new MahasenPastryApp(node);

        //Create the memory storage part
        MahasenMemoryStorage memoryStorage = new MahasenMemoryStorage(new PastryIdFactory(env), env);

        mahasenPastTreeApp = new MahasenPastImpl(getNode(), new StorageManagerImpl(idf, memoryStorage, new LRUCache(
                new MemoryStorage(idf), 512 * 1024, getNode().getEnvironment())), 1, "PastTreeApp");

        getNode().boot(bootaddress);

        // the node may require sending several messages to fully boot into the ring
        synchronized (getNode()) {
            while (!getNode().isReady() && !getNode().joinFailed()) {
                // delay so we don't busy-wait
                node.wait(500);

                // abort if can't join
                if (getNode().joinFailed()) {
                    throw new IOException("Could not join the FreePastry ring.  Reason:" + getNode().joinFailedReason());
                }
            }
        }


        System.out.println("Finished creating new node " + getNode());

        env.getTimeSource().sleep(10000);

        MahasenMsg msg = new MahasenMsg();

        msg.setSendersNodeHandle(node.getLocalHandle());

        NodeIdFactory rFactory = new RandomNodeIdFactory(env);


    }

    /**
     *  This will destroy pastry node
     */
    public void cleanUp() {
        if (env != null) {
            env.destroy();
        }
        if (storageFile != null) {
            if (deleteDirectory(new File(baseDir))) {
                log.debug("cleaned up registry file :" + storageFile);
            }
        }
    }

    /**
     * Insert past object into DHT
     * @param resourceId
     * @param resource
     * @throws InterruptedException
     * @throws PastException
     */
    public void insertIntoDHT(final Id resourceId, MahasenResource resource, boolean isToUpdate)
            throws InterruptedException, PastException {
        MahasenPastContent myContent = new MahasenPastContent(resourceId, resource);
        myContent.setIsToUpdate(isToUpdate);
        //  String result = this.lookupDHT(resourceId);
        final Semaphore control = new Semaphore(0);

        System.out.println(" storing key for" + resource.getId() + "  :" + myContent.getId());

        {

            mahasenPastApp.insert(myContent, new Continuation<Boolean[], Exception>() {
                // the result is an Array of Booleans for each insert
                public void receiveResult(Boolean[] results) {

                    int numSuccessfulStores = 0;

                    for (int ctr = 0; ctr < results.length; ctr++) {
                        if (results[ctr].booleanValue())
                            numSuccessfulStores++;
                    }
                    control.release();
                    System.out.println(" successfully stored at " + +
                            numSuccessfulStores + " locations.");
                }

                public void receiveException(Exception result) {

                    System.out.println("Error storing ");
                    result.printStackTrace();
                    control.release();

                }
            });

        }

    }

    /**
     * @param resourceId
     * @param newResource
     * @param isToDelete
     * @throws InterruptedException
     */
    public void insertTreeMapIntoDHT(final Id resourceId, MahasenResource newResource, boolean isToDelete)
            throws InterruptedException {

        MahasenResource existingResource = lookupDHT(resourceId);
        if (isToDelete == false && existingResource != null) {
            removeReplacedPropertiesFromTreeMap(existingResource, newResource, resourceId);
        }
        Hashtable<String, String> properties = newResource.getProperties();

        // Starting Treemap workers for background threads
        TreeUpdateWorker treeUpdateWorker = new TreeUpdateWorker(properties, resourceId, newResource, isToDelete);
        Thread treeUpdateThread = new Thread(treeUpdateWorker);
        treeUpdateThread.start();


        Id sizePropertyTreeId = rice.pastry.Id.build(String.valueOf(MahasenConstants.FILE_SIZE.hashCode()));
        MahasenPropertyPastContent mySizeContent = new MahasenPropertyPastContent(sizePropertyTreeId,
                newResource.getFileSize(), resourceId, isToDelete, newResource);
        insertPastContent(mySizeContent);

        Id datePropertyTreeId = rice.pastry.Id.build(String.valueOf(MahasenConstants.UPLOADED_DATE.hashCode()));
        MahasenPropertyPastContent myDateContent = new MahasenPropertyPastContent(datePropertyTreeId,
                newResource.getUploadedDate(), resourceId, isToDelete, newResource);
        insertPastContent(myDateContent);

        Id propertyTagTreeId = rice.pastry.Id.build(String.valueOf(MahasenConstants.TAGS.hashCode()));
        MahasenPropertyPastContent myTagContent = new MahasenPropertyPastContent(propertyTagTreeId,
                newResource.getTags(), resourceId, isToDelete, newResource);
        insertPastContent(myTagContent);


    }

    /**
     * This method will delete contents from TreeMap using insertTreemapintoDHT method
     * 'cos we do not need to remove complete data but only a one element from tree.
     * We update the content after deleting the element at check insert.
     *
     * @param resourceId
     * @param resourceToDelete
     * @throws InterruptedException
     */
    public void deletePropertyFromTreeMap(final Id resourceId, MahasenResource resourceToDelete)
            throws InterruptedException {
        this.insertTreeMapIntoDHT(resourceId, resourceToDelete, true);

    }


    /**
     * @param existingResource
     * @param newResource
     * @param resourceId
     */
    private void removeReplacedPropertiesFromTreeMap(MahasenResource existingResource, MahasenResource newResource,
                                                     rice.pastry.Id resourceId) {
        ////TODO do this for all properties in Mahasen resource


        // remove overridden property values from property tree
        for (Map.Entry<String, String> entry : existingResource.getProperties().entrySet()) {
            Set newProperties = newResource.getProperties().keySet();
            if (newProperties.contains(entry.getKey())) {
                rice.pastry.Id propertyTreeId = rice.pastry.Id.build(String.valueOf(entry.getKey().hashCode()));
                String propertyValue = entry.getValue();
                MahasenPropertyPastContent myContent = new MahasenPropertyPastContent(propertyTreeId, propertyValue,
                        resourceId, true, newResource);
                MahasenNodeManager.getInstance().insertPastContent(myContent);
            }
        }
    }

    /**
     * @param myContent
     */
    public void insertPastContent(MahasenPropertyPastContent myContent) {

        //  String result = this.lookupDHT(resourceId);
        final Semaphore control = new Semaphore(0);


        mahasenPastTreeApp.insert(myContent, new Continuation<Boolean[], Exception>() {
            // the result is an Array of Booleans for each insert
            public void receiveResult(Boolean[] results) {

                int numSuccessfulStores = 0;

                for (int ctr = 0; ctr < results.length; ctr++) {
                    if (results[ctr].booleanValue())
                        numSuccessfulStores++;
                }

                control.release();

                System.out.println(" PropertyTree successfully stored at " + +
                        numSuccessfulStores + " locations.");
            }

            public void receiveException(Exception result) {
                System.out.println("Error storing ");
                result.printStackTrace();
                control.release();

            }
        });

        try {
            control.tryAcquire(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Time out while trying to insert Property tree into DHT");
        }
    }


    /**
     * @param id
     * @return
     * @throws InterruptedException
     */
    public MahasenResource lookupDHT(final Id id) throws InterruptedException {

        final PastContent[] resultContent = new PastContent[1];

        final BlockFlag blockFlag = new BlockFlag(true, 1500);

        mahasenPastApp.lookup(id, false, new Continuation<PastContent, Exception>() {

            public void receiveResult(PastContent result) {
               // System.out.println("Successfully looked up Successfully looked up" + result + " for key " + id + ".");

                resultContent[0] = result;
                blockFlag.unblock();
            }

            public void receiveException(Exception result) {
                System.out.println("Error looking up " + id);
                result.printStackTrace();

                blockFlag.unblock();

            }
        });

        while (blockFlag.isBlocked()) {

            env.getTimeSource().sleep(10);
        }
        if (resultContent != null && resultContent[0] != null) {
            return ((MahasenPastContent) resultContent[0]).getMahasenResourse();
        }
        return null;
    }

    /**
     * @param id
     * @return
     * @throws InterruptedException
     */
    public TreeMap lookupPropertyTreeDHT(final Id id) throws InterruptedException {

        final PastContent[] resultContent = new PastContent[1];

        final BlockFlag blockFlag = new BlockFlag(true, 1500);

        mahasenPastTreeApp.lookup(id, false, new Continuation<PastContent, Exception>() {

            public void receiveResult(PastContent result) {
                System.out.println("Successfully looked up " + result + " for key " + id + ".");

                resultContent[0] = result;

                blockFlag.unblock();
            }

            public void receiveException(Exception result) {
                System.out.println("Error looking up " + id);
                result.printStackTrace();

                blockFlag.unblock();

            }
        });

        while (blockFlag.isBlocked()) {

            env.getTimeSource().sleep(10);
        }
        if (resultContent != null && resultContent[0] != null) {
            return ((MahasenPropertyPastContent) resultContent[0]).getPropertyTree();
        }
        return null;
    }

    /**
     * @param id
     */
    public void deleteContent(final Id id) {

        final Semaphore control = new Semaphore(0);

        Continuation continuation = new Continuation() {

            public void receiveResult(Object result) {
                if (result instanceof MahasenPastContent) {
                    System.out.println(" recieved result for " + ((MahasenPastContent) result).getId());

                }
            }

            public void receiveException(Exception result) {
                result.printStackTrace();
            }
        };

        mahasenPastApp.delete(id, continuation);
        control.release();
    }

    /**
     * @return
     */
    public PastryNode getNode() {
        return node;
    }

    /**
     * @return
     */
    public Registry getRegistry() {
        return registry;
    }

    /**
     * @return
     */
    public MahasenPastryApp getMahasenApp() {
        return mahasenApp;
    }

    /**
     *
     */
    private class BlockFlag {
        boolean block;
        int maxBlockCount = 10;
        int count = 0;

        BlockFlag(boolean blockstaus) {
            this.block = blockstaus;
        }

        BlockFlag(boolean blockstaus, int maxBlockCount) {
            this.block = blockstaus;
            this.maxBlockCount = maxBlockCount;
        }

        void block() {
            block = true;
        }

        void unblock() {
            block = false;
        }

        boolean isBlocked() {
            count++;
            if (maxBlockCount == count) {
                return false;
            }
            return block;
        }
    }

    /**
     * @param path
     * @return
     */
    public boolean deleteDirectory(File path) {

        if (path.exists()) {

            File[] files = path.listFiles();

            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    /**
     * @param propertyName
     * @param propertyValue
     * @return
     * @throws InterruptedException
     */
    public Vector<Id> getSearchResults(String propertyName, String propertyValue) throws InterruptedException {
        final Vector<Id> resultIds = new Vector<Id>();

        // block until we get a result or time out.
        final BlockFlag blockFlag = new BlockFlag(true, 1500);
        mahasenPastTreeApp.getSearchResults(propertyName, propertyValue, new Continuation() {

            public void receiveResult(Object result) {
                if (result != null) {

                    if (resultIds.isEmpty()) {
                        resultIds.addAll((Vector<Id>) result);
                    } else {
                        Vector<Id> tempIdSet = (Vector<Id>) result;
                        for (Id id : tempIdSet) {
                            if (!resultIds.contains(id)) {
                                resultIds.add(id);
                            }
                        }
                    }
                }


                blockFlag.unblock();
            }

            public void receiveException(Exception exception) {
                System.out.println(" Result not found ");
                blockFlag.unblock();
            }
        });

        while (blockFlag.isBlocked()) {

            env.getTimeSource().sleep(10);
        }
        return resultIds;

    }

    /**
     * @param propertyName
     * @param propertyValue
     * @return
     * @throws InterruptedException
     */
    public int getNumberOfSearchResults(String propertyName, String propertyValue) throws InterruptedException {
        final Integer[] resultSize = new Integer[1];
        // block until we get a result or time out.
        final BlockFlag blockFlag = new BlockFlag(true);

        mahasenPastTreeApp.getSearchResults(propertyName, propertyValue, new Continuation() {

            public void receiveResult(Object result) {
                if (result != null) {
                    resultSize[0] = (Integer) result;
                }
                blockFlag.unblock();
            }

            public void receiveException(Exception exception) {
                System.out.println(" Result not found ");
                blockFlag.unblock();
            }
        });

        while (blockFlag.isBlocked()) {

            env.getTimeSource().sleep(10);
        }
        if (resultSize != null && resultSize[0] != null)
            return resultSize[0];

        return 0;
    }

    /**
     * @param propertyName
     * @param initialValue
     * @param lastValue
     * @return
     * @throws InterruptedException
     */
    public Vector<Id> getRangeSearchResults(String propertyName, String initialValue, String lastValue)
            throws InterruptedException {
        final Vector<Id> resultIds = new Vector<Id>();
        // block until we get a result or time out.
        final BlockFlag blockFlag = new BlockFlag(true, 1500);
        mahasenPastTreeApp.getRangeSearchResults(propertyName, initialValue, lastValue, new Continuation() {

            public void receiveResult(Object result) {

                if (result != null) {

                    if (resultIds.isEmpty()) {
                        resultIds.addAll((Vector<Id>) result);
                    } else {
                        Vector<Id> tempIdSet = (Vector<Id>) result;
                        for (Id id : tempIdSet) {
                            if (!resultIds.contains(id)) {
                                resultIds.add(id);
                            }
                        }
                    }
                }
                blockFlag.unblock();
            }

            public void receiveException(Exception exception) {
                System.out.println(" Result not found ");
                blockFlag.unblock();
            }
        });

        while (blockFlag.isBlocked()) {
            env.getTimeSource().sleep(10);
        }

        return resultIds;
    }

    /**
     * @param propertyName
     * @param initialValue
     * @param lastValue
     * @return
     * @throws InterruptedException
     */
    public int getNumberOfRangeSearchResults(String propertyName, String initialValue, String lastValue)
            throws InterruptedException {
        final Integer[] resultSize = new Integer[1];
        // block until we get a result or time out.
        final BlockFlag blockFlag = new BlockFlag(true, 1500);

        mahasenPastTreeApp.getRangeSearchResults(propertyName, initialValue, lastValue, new Continuation() {

            public void receiveResult(Object result) {
                if (result != null) {
                    resultSize[0] = (Integer) result;
                }
            }

            public void receiveException(Exception exception) {
                System.out.println(" Result not found ");
            }
        });

        while (blockFlag.isBlocked()) {

            env.getTimeSource().sleep(10);
        }
        if (resultSize != null && resultSize[0] != null)
            return resultSize[0];

        return 0;
    }
}

