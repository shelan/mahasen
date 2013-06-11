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

import org.mahasen.exception.MahasenException;
import org.mahasen.node.MahasenPropertyPastContent;
import rice.Continuation;
import rice.Executable;
import rice.environment.Environment;
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

public class MahasenMemoryStorage implements Storage {


    // the map used to store the data
    private HashMap storage;

    // the map used to store the metadata
    private ReverseTreeMap metadata;

    // the current list of Ids
    private IdSet idSet;

    // the current total size
    private int currentSize;

    // the factory for manipulating the ids
    private IdFactory factory;

    private Environment environment;

    /**
     * Builds a MemoryStorage object.
     *
     * @param factory The factory to build protocol-specific Ids from.
     */
    public MahasenMemoryStorage(IdFactory factory, Environment env) {
        this.factory = factory;
        idSet = factory.buildIdSet();
        setStorage(new HashMap());
        setMetadata(new ReverseTreeMap());
        currentSize = 0;
        environment = env;
    }

    /**
     * Method which is used to erase all data stored in the Storage.
     * Use this method with care!
     *
     * @param c The command to run once done
     */
    public void flush(Continuation c) {
        setStorage(new HashMap());
        setMetadata(new ReverseTreeMap());
        idSet = factory.buildIdSet();
        currentSize = 0;

        c.receiveResult(Boolean.TRUE);
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
            c.receiveResult(new Boolean(false));
            return;
        }

        idSet.removeId(oldId);
        idSet.addId(newId);

        getStorage().put(newId, getStorage().get(oldId));
        getStorage().remove(oldId);

        getMetadata().put(newId, getMetadata().get(oldId));
        getMetadata().remove(oldId);

        c.receiveResult(new Boolean(true));
    }

    /**
     * Stores the object under the key <code>id</code>.  If there is already
     * an object under <code>id</code>, that object is replaced.
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
    public void store(final Id id, final Serializable metadata, final Serializable obj, final Continuation c) {


        environment.getProcessor().process(new Executable() {

                    public Object execute() {
                        if (id == null || obj == null) {

                            return false;
                        }

                        currentSize += getSize(obj);

                        getStorage().put(id, obj);
                        getMetadata().put(id, metadata);
                        idSet.addId(id);
                        return Boolean.TRUE;
                    }

                }, new Continuation<Object, Exception>() {

                    public void receiveResult(Object result) {
                        c.receiveResult(result);
                    }

                    public void receiveException(Exception exception) {
                        c.receiveException(exception);
                    }

                }, environment.getSelectorManager(), environment.getTimeSource(),
                environment.getLogManager()
        );

    }

    /**
     * Removes the object from the list of stored objects. If the object was not
     * in the cached list in the first place, nothing happens and <code>false</code>
     * is returned.
     * <p/>
     * This method completes by calling recieveResult() of the provided continuation
     * with the success or failure of the operation.
     *
     * @param id The object's persistence id
     * @param c  The command to run once the operation is complete
     * @return <code>true</code> if the action succeeds, else
     *         <code>false</code>.
     */
    public void unstore(Id id, Continuation c) {
        Object stored = getStorage().remove(id);
        getMetadata().remove(id);
        idSet.removeId(id);

        if (stored != null) {
            currentSize -= getSize(stored);
            c.receiveResult(new Boolean(true));
        } else {
            c.receiveResult(new Boolean(false));
        }
    }

    /**
     * Returns whether or not the provided id exists
     *
     * @param id The id to check
     * @return Whether or not the given id is stored
     */
    public boolean exists(Id id) {
        return getStorage().containsKey(id);
    }

    /**
     * Returns the metadata associated with the provided object, or null if
     * no metadata exists.  The metadata must be stored in memory, so this
     * operation is guaranteed to be fast and non-blocking.
     *
     * @param id The id for which the metadata is needed
     * @return The metadata, or null of non exists
     */
    public Serializable getMetadata(Id id) {
        return (Serializable) getMetadata().get(id);
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
            this.getMetadata().put(id, metadata);

        command.receiveResult(new Boolean(exists(id)));
    }

    /**
     * Returns the object identified by the given id, or <code>null</code> if
     * there is no cooresponding object (through receiveResult on c).
     *
     * @param id The id of the object in question.
     * @param c  The command to run once the operation is complete
     */
    public void getObject(Id id, Continuation c) {
        c.receiveResult(getStorage().get(id));
    }

    /**
     * Return the objects identified by the given range of ids. The IdSet
     * returned contains the Ids of the stored objects. The range is
     * partially inclusive, the lower range is inclusive, and the upper
     * exclusive.
     * <p/>
     * <p/>
     * NOTE: This method blocks so if the behavior of this method changes and
     * the guys don't fit in memory, this method may be deprecated.
     *
     * @param range The range to query
     * @return The idset containg the keys
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
     * @return The idset containg the keys
     */
    public IdSet scan() {
        return idSet;
    }

    /**
     * Returns a map which contains keys mapping ids to the associated
     * metadata.
     *
     * @param range The range to query
     * @return The map containg the keys
     */
    public SortedMap scanMetadata(IdRange range) {
        if (range.isEmpty())
            return new RedBlackMap();
        else if (range.getCCWId().equals(range.getCWId()))
            return scanMetadata();
        else
            return new ImmutableSortedMap(getMetadata().keySubMap(range.getCCWId(), range.getCWId()));
    }

    /**
     * Returns a map which contains keys mapping ids to the associated
     * metadata.
     *
     * @return The treemap mapping ids to metadata
     */
    public SortedMap scanMetadata() {
        return new ImmutableSortedMap(getMetadata().keyMap());
    }

    /**
     * Returns the submapping of ids which have metadata less than the provided
     * value.
     *
     * @param value The maximal metadata value
     * @return The submapping
     */
    public SortedMap scanMetadataValuesHead(Object value) {
        return new ImmutableSortedMap(getMetadata().valueHeadMap(value));
    }

    /**
     * Returns the submapping of ids which have metadata null
     *
     * @return The submapping
     */
    public SortedMap scanMetadataValuesNull() {
        return new ImmutableSortedMap(getMetadata().valueNullMap());
    }

    /**
     * Returns the total size of the stored data in bytes.The result
     * is returned via the receiveResult method on the provided
     * Continuation with an Integer representing the size.
     */
    public long getTotalSize() {
        return currentSize;
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
     * Returns the size of the given object, in bytes.
     *
     * @param obj The object to determine the size of
     * @return The size, in bytes
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

    /**
     * Get Search results for one property
     *
     * @param propertyName
     * @param propertyValue
     * @param c
     */
    public void getSearchResult(final String propertyName, final String propertyValue, final Continuation c) {

        rice.pastry.Id treeId = rice.pastry.Id.build(String.valueOf(propertyName.hashCode()));
        getObject(treeId, new Continuation() {
            Vector<Id> resultIds;

            public void receiveResult(Object result) {
                if (result instanceof MahasenPropertyPastContent) {
                    if (((MahasenPropertyPastContent) result).getTreeType()
                            .equals(MahasenConstants.INTEGER_PROPERTY_TREE)) {
                        resultIds = (Vector<Id>) ((MahasenPropertyPastContent) result)
                                .getPropertyTree().get(Integer.valueOf(propertyValue));
                    } else if (((MahasenPropertyPastContent) result).getTreeType().
                            equals(MahasenConstants.STRING_PROPERTY_TREE)) {
                        resultIds = (Vector<Id>) ((MahasenPropertyPastContent) result)
                                .getPropertyTree().get(propertyValue.toLowerCase());
                    }
                }
                c.receiveResult(resultIds);
            }

            public void receiveException(Exception exception) {
                c.receiveException(new MahasenException("Results cannot be found"));
            }
        });

    }

    /**
     * get No of Search results in a result set
     *
     * @param propertyName
     * @param propertyValue
     * @param c
     */
    public void getNumberOfSearchResults(final String propertyName,
                                         final String propertyValue, final Continuation c) {

        getSearchResult(propertyName, propertyValue, new Continuation() {

            public void receiveResult(Object result) {
                if (result != null) {
                    c.receiveResult(((Vector<Id>) result).size());
                } else {
                    c.receiveResult(null);
                }
            }

            public void receiveException(Exception exception) {
                c.receiveException(new MahasenException("Results cannot be found"));
            }
        });

    }

    /**
     * Get a range search result
     *
     * @param propertyName
     * @param initialValue
     * @param lastValue
     * @param c
     */
    public void getRangeSearchResult(final String propertyName, final String initialValue, final String lastValue,
                                     final Continuation c) {
        rice.pastry.Id treeId = rice.pastry.Id.build(String.valueOf(propertyName.hashCode()));
        getObject(treeId, new Continuation() {
            Vector<Id> resultIds = new Vector<Id>();

            public void receiveResult(Object result) {
                if (result instanceof MahasenPropertyPastContent) {

                    if (((MahasenPropertyPastContent) result).getTreeType().
                            equals(MahasenConstants.INTEGER_PROPERTY_TREE)) {
                        NavigableMap<String, Vector<rice.pastry.Id>> resultMap
                                = ((MahasenPropertyPastContent) result).getPropertyTree().
                                subMap(Integer.valueOf(initialValue), true, Integer.valueOf(lastValue), true);
                        Iterator keys = resultMap.keySet().iterator();

                        while (keys.hasNext()) {
                            if (resultIds.isEmpty()) {
                                resultIds.addAll(resultMap.get(keys.next()));
                            } else {
                                Vector<rice.pastry.Id> tempIdSet = resultMap.get(keys.next());
                                for (Id id : tempIdSet) {
                                    if (!resultIds.contains(id)) {
                                        resultIds.add(id);
                                    }
                                }
                            }

                        }

                    } else if (((MahasenPropertyPastContent) result).getTreeType()
                            .equals(MahasenConstants.STRING_PROPERTY_TREE)) {
                        NavigableMap<String, Vector<rice.pastry.Id>> resultMap
                                = ((MahasenPropertyPastContent) result).getPropertyTree().
                                subMap(initialValue.toLowerCase(), true, lastValue.toLowerCase(), true);
                        Iterator keys = resultMap.keySet().iterator();

                        while (keys.hasNext()) {
                            resultIds.addAll(resultMap.get(keys.next()));
                        }
                    }
                    System.out.println("Results ID for range based in Mahasen Memory storage " + resultIds);
                    c.receiveResult(resultIds);
                } else {
                    // if there is no such treeMap in memoryStorage return null
                    c.receiveResult(null);
                }
            }

            public void receiveException(Exception exception) {
                c.receiveException(new MahasenException("Results cannot be found"));
            }
        });

    }

    /**
     * Get a no of Range results in result set
     *
     * @param propertyName
     * @param initialValue
     * @param lastValue
     * @param c
     */
    public void getNumberOfRangeSearchResults(final String propertyName,
                                              final String initialValue, final String lastValue, final Continuation c) {

        getRangeSearchResult(propertyName, initialValue, lastValue, new Continuation() {

            public void receiveResult(Object result) {
                if (result != null) {
                    c.receiveResult(((Vector<Id>) result).size());
                } else {
                    c.receiveResult(null);
                }
            }

            public void receiveException(Exception exception) {
                c.receiveException(new MahasenException("Results cannot be found"));
            }
        });
    }

    public void getMultipleSearchResults(final Hashtable<String, String> tableToSearch,
                                         final Hashtable<String, Object> resultTable,
                                         final ArrayList<String> propertiesLeftToSearch) {
        final String propertyToSearch = propertiesLeftToSearch.get(0);
        getSearchResult(propertyToSearch, tableToSearch.get(propertyToSearch), new Continuation() {

            public void receiveResult(Object result) {
                if (result != null) {
                    Vector<Id> resultIDs = (Vector<Id>) result;
                    resultTable.put(tableToSearch.get(propertyToSearch), resultIDs);
                    propertiesLeftToSearch.remove(0);
                } else {
                    resultTable.remove(tableToSearch.get(propertyToSearch));
                    propertiesLeftToSearch.remove(0);
                }

            }

            public void receiveException(Exception exception) {

            }
        });

        if (!propertiesLeftToSearch.isEmpty()) {

        }
    }

    /**
     * ****************************************************************************
     * <p/>
     * "FreePastry" Peer-to-Peer Application Development Substrate
     * <p/>
     * Copyright 2002-2007, Rice University. Copyright 2006-2007, Max Planck Institute
     * for Software Systems.  All rights reserved.
     * <p/>
     * Redistribution and use in source and binary forms, with or without
     * modification, are permitted provided that the following conditions are
     * met:
     * <p/>
     * - Redistributions of source code must retain the above copyright
     * notice, this list of conditions and the following disclaimer.
     * <p/>
     * - Redistributions in binary form must reproduce the above copyright
     * notice, this list of conditions and the following disclaimer in the
     * documentation and/or other materials provided with the distribution.
     * <p/>
     * - Neither the name of Rice  University (RICE), Max Planck Institute for Software
     * Systems (MPI-SWS) nor the names of its contributors may be used to endorse or
     * promote products derived from this software without specific prior written
     * permission.
     * <p/>
     * This software is provided by RICE, MPI-SWS and the contributors on an "as is"
     * basis, without any representations or warranties of any kind, express or implied
     * including, but not limited to, representations or warranties of
     * non-infringement, merchantability or fitness for a particular purpose. In no
     * event shall RICE, MPI-SWS or contributors be liable for any direct, indirect,
     * incidental, special, exemplary, or consequential damages (including, but not
     * limited to, procurement of substitute goods or services; loss of use, data, or
     * profits; or business interruption) however caused and on any theory of
     * liability, whether in contract, strict liability, or tort (including negligence
     * or otherwise) arising in any way out of the use of this software, even if
     * advised of the possibility of such damage.
     * <p/>
     * *****************************************************************************
     */

    /**
     * @return
     */
    public HashMap getStorage() {
        return storage;
    }

    /**
     * @param storage
     */
    public void setStorage(HashMap storage) {
        this.storage = storage;
    }

    /**
     * @return
     */
    public ReverseTreeMap getMetadata() {
        return metadata;
    }

    /**
     * @param metadata
     */
    public void setMetadata(ReverseTreeMap metadata) {
        this.metadata = metadata;
    }
}
