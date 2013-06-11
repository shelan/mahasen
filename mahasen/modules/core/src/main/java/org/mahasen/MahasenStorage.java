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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mahasen.node.MahasenPastContent;
import org.mahasen.resource.MahasenResource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Tag;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import rice.Continuation;
import rice.environment.Environment;
import rice.environment.processing.WorkRequest;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.IdFactory;
import rice.p2p.commonapi.IdRange;
import rice.p2p.commonapi.IdSet;
import rice.p2p.util.ImmutableSortedMap;
import rice.p2p.util.RedBlackMap;
import rice.p2p.util.ReverseTreeMap;
import rice.p2p.util.XMLObjectOutputStream;
import rice.persistence.Storage;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;


public class MahasenStorage implements Storage {
    /**
     * Builds a PersistentStorage given a root directory in which to
     * persist the data. Uses a default instance name.
     *
     * @param factory The factory to use for creating Ids.
     * @param rootDir The root directory of the persisted disk.
     * @param size    the size of the storage in bytes, or -1 for unlimited
     */

    private static final Log log = LogFactory.getLog(MahasenStorage.class);

    // registry instance used for store objects
    private Registry registry;

    private Environment environment;

    // the map used to store the metadata
    private ReverseTreeMap metadata;

    // the current list of Ids
    private IdSet idSet;

    // the current total size
    private int currentSize;

    // the factory for manipulating the ids
    private IdFactory factory;

    private Object statLock = new Object();

    private long numWrites = 0;
    private long numReads = 0;
    private long numRenames = 0;
    private long numDeletes = 0;
    private long numMetadataWrites = 0;


    public MahasenStorage(IdFactory factory, String rootDir, long size, Environment env, Registry registry) throws IOException {

        this.registry = registry;

        this.environment = env;

        this.metadata = new ReverseTreeMap();

        idSet = factory.buildIdSet();
    }

    /**
     * Makes the object persistent to disk and stored permanantly
     * <p/>
     * If the object is already persistent, this method will
     * simply update the object's serialized image.
     * <p/>
     * This is implemented atomically so that this may succeed
     * and store the new object, or fail and leave the previous
     * object intact.
     * <p/>
     * This method completes by calling recieveResult() of the provided continuation
     * with the success or failure of the operation.
     *
     * @param obj      The object to be made persistent.
     * @param id       The object's id.
     * @param metadata The object's metadata
     * @param c        The command to run once the operation is complete
     * @return <code>true</code> if the action succeeds, else
     *         <code>false</code>.
     */

    public void store(final Id id, final Serializable metadata, final Serializable obj, Continuation c) {

        environment.getProcessor().processBlockingIO(new WorkRequest(c, environment.getSelectorManager()) {


            @Override
            public Object doWork() throws Exception {

                synchronized (statLock) {
                    numWrites++;
                }

                try {

                    MahasenResource tempMahasenResource = ((MahasenPastContent) obj).getMahasenResourse();
                    Resource resource = addResourceProperties(registry.newResource(), tempMahasenResource);
                    String registryKey = MahasenConstants.ROOT_REGISTRY_PATH + id.hashCode();
                    registry.put(registryKey, resource);
                    addTags(registryKey, tempMahasenResource.getTags());

                } catch (RegistryException e) {
                    e.printStackTrace();
                }

                return Boolean.TRUE;
            }
        });

        this.metadata.put(id, metadata);

        idSet.addId(id);

        currentSize += getSize(obj);

    }

    /**
     * Removes the object from the list of stored objects. This method is
     * non-blocking. If the object was not in the stored list in the first place,
     * nothing happens and <code>False</code> is returned.
     * <p/>
     * Returns <code>True</code> if the action succeeds, else
     * <code>False</code>  (through receiveResult on c).
     *
     * @param id The object's persistence id
     * @param c  The command to run once the operation is complete
     */
    public void unstore(final Id id, Continuation c) {

        //PersistentStorage
        environment.getProcessor().processBlockingIO(new WorkRequest(c, environment.getSelectorManager()) {


            @Override
            public Object doWork() throws Exception {
                synchronized (statLock) {
                    numDeletes++;
                }

                try {
                    if (registry.resourceExists(MahasenConstants.ROOT_REGISTRY_PATH + id.hashCode()))
                        registry.delete(MahasenConstants.ROOT_REGISTRY_PATH + id.hashCode());

                } catch (RegistryException e) {
                    e.printStackTrace();
                }

                return Boolean.TRUE;
            }
        });


    }

    /**
     * @param resource
     * @param mahasenResource
     * @return
     * @throws RegistryException
     */

    private Resource addResourceProperties(Resource resource, MahasenResource mahasenResource) throws RegistryException {

        for (Map.Entry<String, String> entry : mahasenResource.getProperties().entrySet()) {

            resource.addProperty(entry.getKey(), entry.getValue());

            log.info("property " + entry.getKey() + " =" + entry.getValue() + " adding to resource");
            log.info(resource.getProperty(entry.getKey()) + "was added to Resource");

        }

        resource.addProperty(MahasenConstants.FILE_SIZE, String.valueOf(mahasenResource.getFileSize()));

        log.debug("date added to registry resource " + String.valueOf(mahasenResource.getUploadedDate()));
        resource.addProperty(MahasenConstants.UPLOADED_DATE, String.valueOf(mahasenResource.getUploadedDate()));
        String partNames = mahasenResource.getPartNames().toString();

        if (mahasenResource.getPartNames().size() != 0) {
            resource.addProperty(MahasenConstants.PART_NAMES, partNames.substring(1, partNames.length() - 1));
        }

        Hashtable<String, Vector<String>> partsIpTable = mahasenResource.getSplittedPartsIpTable();

        if (partsIpTable.entrySet() != null) {
            for (Map.Entry<String, Vector<String>> entry : partsIpTable.entrySet()) {
                String entryValue = entry.getValue().toString();
                resource.addProperty(entry.getKey(), entryValue.substring(1, entryValue.length() - 1));
                System.out.println("parts Ip table entry " + entry.getKey() + " = " + entry.getValue().toString());
            }
        }


        return resource;
    }

    /**
     * @param resource
     * @param mahasenResource
     * @return
     */
    private MahasenResource extractResourceProperties(Resource resource, MahasenResource mahasenResource) {

        ArrayList<String> partNameList = new ArrayList<String>();
        String partNames = resource.getProperty(MahasenConstants.PART_NAMES);
        if (partNames != null) {
            String partNameArray[] = partNames.replace(" ", "").split(",");
            mahasenResource.addPartNames(partNameArray);
            for (String partName : partNameArray) {
                partNameList.add(partName);
            }

            for (String partName : partNameList) {
                String ipString = resource.getProperty(partName);
                String ipArray[] = ipString.replace(" ", "").split(",");
                for (String ip : ipArray) {
                    mahasenResource.addSplitPartStoredIp(partName, ip);
                }

            }
        }
        for (Map.Entry<Object, Object> entry : resource.getProperties().entrySet()) {

            String propertyVal = String.valueOf(entry.getValue());
            if (entry.getKey().toString().equals(MahasenConstants.PART_NAMES) ||
                    partNameList.contains(entry.getKey())) {
                continue;
            } else if (entry.getKey().toString().equals(MahasenConstants.FILE_SIZE)) {
                mahasenResource.setFileSize(Integer.valueOf(propertyVal.substring(1, propertyVal.length() - 1)));
            } else if (entry.getKey().toString().equals(MahasenConstants.UPLOADED_DATE)) {
                System.out.println("Uploaded date : " + propertyVal);
                mahasenResource.setUploadedDate(Integer.valueOf(propertyVal.substring(1, propertyVal.length() - 1)));
            } else {
                mahasenResource.addProperty(String.valueOf(entry.getKey()),
                        propertyVal.substring(1, propertyVal.length() - 1));

                log.info("property " + String.valueOf(entry.getKey()) + " =" + String.valueOf(entry.getValue())
                        + " was added to Registry resource");

            }


        }

        return mahasenResource;
    }

    /**
     * @param registryKey
     * @param tags
     * @throws RegistryException
     */
    private void addTags(String registryKey, Vector<String> tags) throws RegistryException {

        log.info("tags to apply " + tags);

        for (String tag : tags) {

            registry.applyTag(registryKey, tag);
        }

        log.info("tags for the registry resource " + registryKey + ": ");
        Tag[] tagArray = registry.getTags(registryKey);
        for (Tag tag : tagArray) {
            log.info(tag.getTagName());
        }

    }

    /**
     * @param registryKey
     * @param mahasenResource
     * @return
     * @throws RegistryException
     */
    private MahasenResource extractTags(String registryKey, MahasenResource mahasenResource) throws RegistryException {

        Tag tags[] = registry.getTags(registryKey);

        for (Tag tag : tags) {
            mahasenResource.addTag(tag.getTagName());
        }
        return mahasenResource;

    }


    /**
     * Returns whether or not an object is present in the location <code>id</code>.
     *
     * @param id The id of the object in question.
     * @return Whether or not an object is present at id.
     */
    public boolean exists(Id id) {

        boolean exists = false;

        try {
            exists = registry.resourceExists(MahasenConstants.ROOT_REGISTRY_PATH + id.hashCode());
        } catch (RegistryException e) {
            e.printStackTrace();
        }

        return exists;
    }

    /**
     * Returns the object identified by the given id.
     *
     * @param id The id of the object in question.
     * @param c  The command to run once the operation is complete
     * @return The object, or <code>null</code> if there is no corresponding
     *         object (through receiveResult on c).
     */

    public void getObject(final Id id, final Continuation c) {

        if (!exists(id)) {
            c.receiveResult(null);
            return;
        }
        MahasenPastContent mahasenPastObj = null;

        Resource resource = null;

        try {

            if (registry.resourceExists(MahasenConstants.ROOT_REGISTRY_PATH + id.hashCode())) {

                resource = registry.get(MahasenConstants.ROOT_REGISTRY_PATH + id.hashCode());
                MahasenResource transferResourceOb = new MahasenResource(id);
                transferResourceOb = extractResourceProperties(resource, transferResourceOb);
                String registryKey = MahasenConstants.ROOT_REGISTRY_PATH + id.hashCode();
                transferResourceOb = extractTags(registryKey, transferResourceOb);
                mahasenPastObj = new MahasenPastContent(id, transferResourceOb);
            }
        } catch (RegistryException e) {

            c.receiveException(e);
        }
        if (resource != null) {

            c.receiveResult(mahasenPastObj);

        } else {
            c.receiveResult(null);
        }


    }

    /**
     * Returns the metadata associated with the provided object, or null if
     * no metadata exists.  The metadata must be stored in memory, so this
     * operation is guaranteed to be fast and non-blocking.
     * <p/>
     * The metadata returned from this method must *NOT* be mutated in any way,
     * as the actual reference to the internal object is returned.  Mutating
     * this metadata may make the internal indices incorrect, resulting
     * in undefined behavior.  Changing the metadata should be done by creating
     * a new metadata object and calling setMetadata().
     *
     * @param id The id for which the metadata is needed
     * @return The metadata, or null if none exists
     */

    public Serializable getMetadata(Id id) {

        return (Serializable) metadata.get(id);
    }

    /**
     * Updates the metadata stored under the given key to be the provided
     * value.  As this may require a disk access, the requestor must
     * also provide a continuation to return the result to.
     *
     * @param id       The id for the metadata
     * @param metadata The metadata to store
     * @param command  The command to run once the operation is complete
     */
    public void setMetadata(Id id, Serializable metadata, Continuation command) {

        if (exists(id))
            this.metadata.put(id, metadata);

        command.receiveResult(Boolean.valueOf(exists(id)));

    }

    /**
     * Renames the given object to the new id.  This method is potentially faster
     * than store/cache and unstore/uncache.
     *
     * @param oldId The id of the object in question.
     * @param newId The new id of the object in question.
     * @param c     The command to run once the operation is complete
     */
    public void rename(Id oldId, Id newId, Continuation c) {

        if (!idSet.isMemberId(oldId)) {

            c.receiveResult(Boolean.valueOf(false));
            return;
        }

        try {

            registry.rename(MahasenConstants.ROOT_REGISTRY_PATH + oldId.hashCode(),
                    MahasenConstants.ROOT_REGISTRY_PATH + newId.hashCode());

        } catch (RegistryException e) {
            c.receiveException(e);
        }

        idSet.removeId(oldId);
        idSet.addId(newId);
        metadata.put(newId, metadata.get(oldId));
        metadata.remove(oldId);
        c.receiveResult(Boolean.valueOf(true));
    }

    /**
     * Return the objects identified by the given range of ids. The IdSet
     * returned contains the Ids of the stored objects. The range is
     * partially inclusive, the lower range is inclusive, and the upper
     * exclusive.
     * <p/>
     * NOTE: This method blocks so if the behavior of this method changes and
     * no longer stored in memory, this method may be deprecated.
     *
     * @param range The range to query
     * @return The idset containing the keys
     */
    public IdSet scan(IdRange range) {

        return idSet.subSet(range);
    }

    /**
     * Return all objects currently stored by this catalog
     * <p/>
     * NOTE: This method blocks so if the behavior of this method changes and
     * no longer stored in memory, this method may be deprecated.
     *
     * @return The idset containing the keys
     */
    public IdSet scan() {

        return idSet;
    }

    /**
     * Returns a map which contains keys mapping ids to the associated
     * metadata.
     *
     * @param range The range to query
     * @return The map containing the keys
     */
    public SortedMap scanMetadata(IdRange range) {

        if (range.isEmpty())

            return new RedBlackMap();

        else if (range.getCCWId().equals(range.getCWId()))

            return scanMetadata();

        else

            return new ImmutableSortedMap(metadata.keySubMap(range.getCCWId(), range.getCWId()));
    }

    /**
     * Returns a map which contains keys mapping ids to the associated
     * metadata.
     *
     * @return The treemap mapping ids to metadata
     */
    public SortedMap scanMetadata() {

        return new ImmutableSortedMap(metadata.keyMap());
    }

    /**
     * Returns the submapping of ids which have metadata less than the provided
     * value.
     *
     * @param value The maximal metadata value
     * @return The submapping
     */
    public SortedMap scanMetadataValuesHead(Object value) {

        return new ImmutableSortedMap(metadata.valueHeadMap(value));
    }

    /**
     * Returns the submapping of ids which have metadata null
     *
     * @return The submapping
     */
    public SortedMap scanMetadataValuesNull() {

        return new ImmutableSortedMap(metadata.valueNullMap());
    }

    /**
     * Returns the number of Ids currently stored in the catalog
     *
     * @return The number of ids in the catalog
     */
    public int getSize() {

        return idSet.numElements();
    }

    /**
     * Returns the total size of the stored data in bytes.
     *
     * @return The total storage size
     */
    public long getTotalSize() {

        throw new UnsupportedOperationException("no total object size for registry supported");
    }

    /**
     * Method which is used to erase all data stored in the Catalog.
     * Use this method with care!
     *
     * @param c The command to run once done
     */
    public void flush(Continuation c) {

        try {

            registry.delete(MahasenConstants.ROOT_REGISTRY_PATH);

        } catch (RegistryException e) {

            c.receiveException(e);
        }

        metadata = new ReverseTreeMap();
        idSet = factory.buildIdSet();
        currentSize = 0;
        c.receiveResult(Boolean.TRUE);

    }

    /**
     * @param obj
     * @return
     */
    private int getSize(Object obj) {

        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new XMLObjectOutputStream(new BufferedOutputStream(new GZIPOutputStream(baos)));
            oos.writeObject(obj);
            oos.close();
            return baos.toByteArray().length;

        } catch (IOException e) {

            throw new RuntimeException("Object " + obj + " was not serialized correctly! " + e.toString(), e);
        }
    }
}