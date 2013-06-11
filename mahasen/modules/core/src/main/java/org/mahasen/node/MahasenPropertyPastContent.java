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
import org.mahasen.MahasenConstants;
import org.mahasen.resource.MahasenResource;
import rice.p2p.past.ContentHashPastContent;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastException;
import rice.pastry.Id;

import java.util.*;

public class MahasenPropertyPastContent extends ContentHashPastContent {

    private static final Log log = LogFactory.getLog(MahasenPropertyPastContent.class);
    private TreeMap<String, Vector<Id>> stringPropertyTree;
    private TreeMap<Integer, Vector<Id>> intPropertyTree;
    private String treeType = "";
    private MahasenResource newMahasenResource;
    private boolean isToDelete = false;

    /**
     * @param myId
     * @param propertyValue
     * @param resourceId
     * @param isToDelete
     * @param newMahasenResource
     */
    public MahasenPropertyPastContent(Id myId, Object propertyValue, Id resourceId, boolean isToDelete,
                                      MahasenResource newMahasenResource) {
        super(myId);
        this.isToDelete = isToDelete;
        this.newMahasenResource = newMahasenResource;
        System.out.println("-------------------------------------- is to delete " + this.isToDelete);
        Vector<Id> id = new Vector<Id>();
        id.add(resourceId);

        if (propertyValue instanceof String) {
            stringPropertyTree = new TreeMap<String, Vector<Id>>();
            stringPropertyTree.put(String.valueOf(((String) propertyValue).toLowerCase()), id);
            treeType = MahasenConstants.STRING_PROPERTY_TREE;
        } else if (propertyValue instanceof Integer) {
            intPropertyTree = new TreeMap<Integer, Vector<Id>>();
            intPropertyTree.put((Integer) propertyValue, id);
            treeType = MahasenConstants.INTEGER_PROPERTY_TREE;
        }

    }

    /**
     * @param myId
     * @param propertyVector
     * @param resourceId
     * @param isToDelete
     * @param newMahasenResource
     */
    public MahasenPropertyPastContent(Id myId, Vector<String> propertyVector, Id resourceId, boolean isToDelete,
                                      MahasenResource newMahasenResource) {
        super(myId);
        this.isToDelete = isToDelete;
        this.newMahasenResource = newMahasenResource;
        System.out.println("-------------------------------------- is to delete " + this.isToDelete);
        stringPropertyTree = new TreeMap<String, Vector<Id>>();
        createStringTreeMap(propertyVector, resourceId);
        treeType = MahasenConstants.STRING_PROPERTY_TREE;
    }

    /**
     * @param propertyVector
     * @param resourceId
     */
    private void createStringTreeMap(Vector<String> propertyVector, Id resourceId) {

        for (String key : propertyVector) {

            Vector<Id> id = new Vector<Id>();
            id.add(resourceId);

            stringPropertyTree.put(key.toLowerCase(), id);
        }

    }

    /**
     * @param id
     * @param existingContent
     * @return
     * @throws PastException
     */
    @Override
    public PastContent checkInsert(rice.p2p.commonapi.Id id, PastContent existingContent) throws PastException {
        if (existingContent != null) {

            if (((MahasenPropertyPastContent) existingContent).getTreeType()
                    .equals(MahasenConstants.STRING_PROPERTY_TREE)
                    && this.treeType.endsWith(MahasenConstants.STRING_PROPERTY_TREE)) {
                TreeMap<String, Vector<Id>> existingTree = ((MahasenPropertyPastContent) existingContent)
                        .getPropertyTree();

                log.debug("EXISTING TREE " + existingTree);
                log.debug("NEW TREE " + stringPropertyTree);

                if (existingTree != null && stringPropertyTree != null) {
                    Iterator keys = stringPropertyTree.keySet().iterator();
                    while (keys.hasNext()) {

                        String propertyValue = keys.next().toString();
                        log.debug("property value " + propertyValue);

                        if (existingTree.containsKey(propertyValue)) {
                            log.debug("existing tree contains the property value " + propertyValue);
                            log.debug("get node for existing property value " + existingTree.get(propertyValue));

                            log.debug("node is to delete " + (this.isToDelete));
                            // this will update the Id vector for the existing property value in the exsisting TreeMap node
                            if (!this.isToDelete) {
                                log.debug("adding resource id to " + propertyValue);
                                if (!existingTree.get(propertyValue).contains(stringPropertyTree.get(propertyValue)
                                        .get(0))) {
                                    existingTree.get(propertyValue).
                                            add(stringPropertyTree.get(propertyValue).get(0));
                                }
                            } else {
                                if (existingTree.get(propertyValue).contains(stringPropertyTree.get(propertyValue)
                                        .get(0))) {
                                    log.debug("removing resource id from " + propertyValue);
                                    boolean removed = existingTree.get(propertyValue).remove(stringPropertyTree
                                            .get(propertyValue).get(0));
                                    log.debug("deleted " + removed);
                                    if (existingTree.get(propertyValue).size() == 0) {
                                        existingTree.remove(propertyValue);
                                    }

                                }

                            }


                        } else {
                            log.debug("existing tree does not contain the property value " + propertyValue);
                            // this will add the new property value and the resource id to the TreeMap
                            existingTree.put(propertyValue, stringPropertyTree.get(propertyValue));
                        }

                        log.debug("Tree after modifications " + existingTree.toString());

                    }
                }

            } else if (((MahasenPropertyPastContent) existingContent).getTreeType()
                    .equals(MahasenConstants.INTEGER_PROPERTY_TREE)
                    && this.treeType.equals(MahasenConstants.INTEGER_PROPERTY_TREE)) {

                TreeMap<Integer, Vector<Id>> existingTree = ((MahasenPropertyPastContent) existingContent)
                        .getPropertyTree();

                log.debug("EXISTING TREE " + existingTree);
                log.debug("NEW TREE " + intPropertyTree);
                //for(String propertyValue:propertyTree.keySet())  {

                if (existingTree != null && intPropertyTree != null) {
                    Iterator keys = intPropertyTree.keySet().iterator();
                    while (keys.hasNext()) {

                        Integer propertyValue = Integer.valueOf(keys.next().toString());
                        log.debug("property value " + propertyValue);

                        if (existingTree.containsKey(propertyValue)) {
                            log.debug("existing tree contains the property value " + propertyValue);
                            log.debug("get node for existing property value " + existingTree.get(propertyValue));
                            log.debug("node is to delete " + (this.isToDelete));

                            // this will update the Id vector for the existing property value in the exsisting TreeMap node
                            if (!this.isToDelete) {
                                if (!existingTree.get(propertyValue).contains(intPropertyTree
                                        .get(propertyValue).get(0))) {
                                    existingTree.get(propertyValue).
                                            add(intPropertyTree.get(propertyValue).get(0));
                                }
                            } else {
                                if (existingTree.get(propertyValue).contains(intPropertyTree.get(propertyValue).get(0))) {
                                    existingTree.get(propertyValue).remove(intPropertyTree.get(propertyValue).get(0));
                                    if (existingTree.get(propertyValue).size() == 0) {
                                        existingTree.remove(propertyValue);
                                    }
                                }

                            }
                        } else {
                            log.debug("existing tree does not contain the property value " + propertyValue);
                            // this will add the new property value and the resource id to the TreeMap
                            existingTree.put(propertyValue, intPropertyTree.get(propertyValue));
                        }

                        log.debug("Existing PropertyTree" + existingTree.toString());

                    }
                }
            }
            return existingContent;
        } else {
            log.debug("===== crate a new property tree====");
            log.debug("Existing PropertyTree" + this.treeType);
            return this;
        }
    }

    /**
     * @return
     */
    public TreeMap getPropertyTree() {
        if (treeType.equals(MahasenConstants.STRING_PROPERTY_TREE)) {
            return stringPropertyTree;
        } else if (treeType.equals(MahasenConstants.INTEGER_PROPERTY_TREE)) {
            return intPropertyTree;
        }
        return null;
    }

    /**
     * @return
     */
    public String getTreeType() {
        return treeType;
    }

    /**
     * @return
     */
    public boolean getIsToDelete() {
        return isToDelete;
    }

    /**
     * @param toDelete
     */
    public void setToDelete(boolean toDelete) {
        isToDelete = toDelete;
    }

}
