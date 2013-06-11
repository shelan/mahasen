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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mahasen.MahasenConstants;
import org.mahasen.exception.MahasenException;
import org.mahasen.resource.MahasenResource;
import rice.pastry.Id;


import java.util.*;

public class SearchUtil extends AbstractCommonUtil {

    private static Log log = LogFactory.getLog(SearchUtil.class);

    /**
     * @param propertyName
     * @param propertyValue
     * @return
     * @throws InterruptedException
     * @throws MahasenException
     */
    public Vector<String> searchProperty(String propertyName, String propertyValue)
            throws InterruptedException, MahasenException {

        Vector<Id> idVector = getSearchResultIds(propertyName, propertyValue);
        Vector<String> resultantFileNames = getResultantFileNames(idVector);

        log.debug("===========search results for " + propertyName + " = " + propertyValue + "=============");
        return resultantFileNames;
    }

    /**
     * @param propertyName
     * @param propertyValue
     * @return
     * @throws InterruptedException
     * @throws MahasenException
     */
    private Vector<Id> getSearchResultIds(String propertyName, String propertyValue)
            throws InterruptedException, MahasenException {

        Vector<Id> idVector = new Vector<Id>();

        // tag search for several tags
        if (propertyValue.contains("&")) {
            String values[] = propertyValue.split("\\&");
            for (String value : values) {
                if (idVector.isEmpty()) {
                    Vector<Id> tempResult = mahasenManager.getSearchResults(propertyName, value.trim());
                    if (tempResult != null) {
                        idVector.addAll(tempResult);
                    }
                }
                if (!idVector.isEmpty()) {
                    Stack<Vector<Id>> idVectorSet = new Stack<Vector<Id>>();
                    idVectorSet.push(idVector);
                    idVectorSet.push(mahasenManager.getSearchResults(propertyName, value.trim()));
                    idVector = getCommonIds(idVectorSet);

                }
            }
        } else if (propertyValue.contains("|")) {
            String values[] = propertyValue.split("\\|");
            for (String value : values) {

                Vector<Id> tempResult = mahasenManager.getSearchResults(propertyName, value.trim());
                if (tempResult != null) {
                    for (Id id : tempResult) {
                        if (!idVector.contains(id)) {
                            idVector.add(id);
                        }
                    }
                }

            }
        } else {
            idVector = mahasenManager.getSearchResults(propertyName, propertyValue.trim());
        }

        return idVector;
    }

    /**
     * @param vector1
     * @param vector2
     * @return
     * @throws MahasenException
     */
    private Vector<Id> getCommonIds(Vector<Id> vector1, Vector<Id> vector2) throws MahasenException {
        Vector<Id> commonIds = new Vector<Id>();

        for (Id id : vector1) {
            if (vector2 == null) {
                throw new MahasenException("No results found");
            }
            if (vector2.contains(id)) {
                commonIds.add(id);
            }
        }
        return commonIds;
    }

    /**
     * @param propertyTreeId
     * @param propertyValue
     * @return
     * @throws InterruptedException
     * @throws MahasenException
     */
    private Vector<Id> getResourceIdVector(Id propertyTreeId, String propertyValue)
            throws InterruptedException, MahasenException {

        TreeMap<?, Vector<Id>> propertyTree = mahasenManager.lookupPropertyTreeDHT(propertyTreeId);

        if (propertyTree == null) {
            throw new MahasenException("Property not found");

        } else {

            if (propertyTree.firstKey() instanceof String) {

                return propertyTree.get(propertyValue.toLowerCase());

            } else if (propertyTree.firstKey() instanceof Integer) {

                return propertyTree.get(Integer.valueOf(propertyValue));

            }
        }
        System.out.println("this is the property tree " + propertyTree);

        return null;

    }

    /**
     * @param propertyName
     * @param initialValue
     * @param lastValue
     * @return
     * @throws InterruptedException
     * @throws MahasenException
     */
    public Vector<String> rangeSearch(String propertyName, String initialValue, String lastValue)
            throws InterruptedException, MahasenException {

        Vector<Id> idVector = getRangeSearchResultIds(propertyName, initialValue, lastValue);
        log.debug("===========Range search results for " + propertyName + " = " + initialValue + " to " +
                lastValue + "=============");
        Vector<String> resultantFileNames = getResultantFileNames(idVector);
        return resultantFileNames;

    }

    /**
     * @param propertyName
     * @param initialValue
     * @param lastValue
     * @return
     * @throws InterruptedException
     * @throws MahasenException
     */
    private Vector<Id> getRangeSearchResultIds(String propertyName, String initialValue, String lastValue)
            throws InterruptedException, MahasenException {

        return mahasenManager.getRangeSearchResults(propertyName, initialValue, lastValue);
    }

    /**
     * @param propertyTreeId
     * @param initialValue
     * @param lastValue
     * @return
     * @throws InterruptedException
     * @throws MahasenException
     */
    private Vector<Id> getResourceIdVector(Id propertyTreeId, String initialValue, String lastValue)
            throws InterruptedException, MahasenException {

        Vector<Id> resultantIds = new Vector<Id>();
        TreeMap propertyTree = mahasenManager.lookupPropertyTreeDHT(propertyTreeId);

        if (propertyTree == null) {
            throw new MahasenException("Property not found");
        } else {

            if (propertyTree.firstKey() instanceof String) {

                System.out.println("this is the property tree " + propertyTree);
                NavigableMap<String, Vector<Id>> resultMap = propertyTree.subMap(initialValue.toLowerCase(),
                        true, lastValue.toLowerCase(), true);

                Iterator keys = resultMap.keySet().iterator();

                while (keys.hasNext()) {
                    resultantIds.addAll(resultMap.get(keys.next()));
                }

            } else if (propertyTree.firstKey() instanceof Integer) {

                System.out.println("this is the property tree " + propertyTree);
                NavigableMap<Integer, Vector<Id>> resultMap = propertyTree.subMap(Integer.valueOf(initialValue),
                        true, Integer.valueOf(lastValue), true);

                Iterator keys = resultMap.keySet().iterator();

                while (keys.hasNext()) {
                    resultantIds.addAll(resultMap.get(keys.next()));
                }
            }
        }

        return resultantIds;
    }

    /**
     * @param idVector
     * @return
     * @throws InterruptedException
     * @throws MahasenException
     */
    private Vector<String> getResultantFileNames(Vector<Id> idVector)
            throws InterruptedException, MahasenException {
        Vector<String> resultantFileNames = new Vector<String>();

        if (idVector == null) {
            throw new MahasenException("No result found");
        }
        for (Id resourceId : idVector) {
            MahasenResource resource = mahasenManager.lookupDHT(resourceId);
            if (resource == null) {
                throw new MahasenException("Resource not found");
            }
            log.debug("file name  " + resource.getProperty(MahasenConstants.FILE_NAME));
            resultantFileNames.add(resource.getProperty(MahasenConstants.FILE_NAME).toString());
        }
        Collections.sort(resultantFileNames);
        Collections.sort(resultantFileNames);
        return resultantFileNames;
    }

    /**
     * This method is to search files having all given property constraints
     *
     * @param searchParameters
     * @throws MahasenException
     * @throws InterruptedException
     */
    public Vector<String> multipleAndSearch(Hashtable<String, Vector<String>> searchParameters)
            throws MahasenException, InterruptedException {
        ArrayList<SearchBean> searchValues = createSearchBeans(searchParameters);
        Vector<String> fileNames = new Vector<String>();

        Stack<Vector<Id>> resultantIDs = new Stack<Vector<Id>>();

        for (SearchBean searchBean : searchValues) {
            if (searchBean.isRangeBased()) {
                Vector<Id> resultIds = getRangeSearchResultIds(searchBean.getPropertyName(),
                        searchBean.getInitialValue(), searchBean.getLastValue());
                resultantIDs.add(resultIds);
            } else {
                Vector<Id> resultIds = getSearchResultIds(searchBean.getPropertyName(),
                        searchBean.getInitialValue());
                resultantIDs.add(resultIds);
            }
        }

        Vector<Id> finalResultIds = getCommonIds(resultantIDs);
        fileNames = getResultantFileNames(finalResultIds);
        return fileNames;
    }

    /**
     * @param searchParameters
     * @return
     * @throws MahasenException
     * @throws InterruptedException
     */
    public Vector<String> multipleOrSearch(Hashtable<String, Vector<String>> searchParameters)
            throws MahasenException, InterruptedException {
        ArrayList<SearchBean> searchValues = createSearchBeans(searchParameters);
        Vector<String> fileNames = new Vector<String>();

        Vector<Id> finalResultIds = new Vector<Id>();
        for (SearchBean searchBean : searchValues) {
            Vector<Id> resultIds;
            if (searchBean.isRangeBased()) {
                resultIds = getRangeSearchResultIds(searchBean.getPropertyName(),
                        searchBean.getInitialValue(), searchBean.getLastValue());

            } else {
                resultIds = getSearchResultIds(searchBean.getPropertyName(), searchBean.getInitialValue());

            }

            if (finalResultIds.isEmpty()) {
                finalResultIds.addAll(resultIds);
            } else {
                for (Id id : resultIds) {
                    if (!finalResultIds.contains(id)) {
                        finalResultIds.add(id);
                    }
                }
            }
        }
        fileNames = getResultantFileNames(finalResultIds);
        return fileNames;
    }

    /**
     * @param searchParameters
     * @return
     */
    private ArrayList<SearchBean> createSearchBeans(Hashtable<String, Vector<String>> searchParameters) {
        ArrayList<SearchBean> searchValues = new ArrayList<SearchBean>();
        Iterator iterator = searchParameters.keySet().iterator();
        while (iterator.hasNext()) {
            String propertyName = (String) iterator.next();
            String initialValue = "";
            String finalValue = "";
            boolean isRangeBased = false;
            for (String propertyValue : searchParameters.get(propertyName)) {
                if (searchParameters.get(propertyName) != null && propertyValue.contains(",")) {
                    String propertyValues[] = new String[2];
                    propertyValues = propertyValue.split(",");
                    initialValue = propertyValues[0];
                    finalValue = propertyValues[1];
                    isRangeBased = true;
                } else {
                    initialValue = propertyValue;
                    finalValue = null;
                    isRangeBased = false;
                }
                SearchBean bean = new SearchBean(propertyName, initialValue, finalValue, isRangeBased);
                searchValues.add(bean);
            }


        }
        return searchValues;
    }

    /**
     * @param idVectorSet
     * @return
     * @throws MahasenException
     */
    private Vector<Id> getCommonIds(Stack<Vector<Id>> idVectorSet) throws MahasenException {
        if (idVectorSet != null) {
            while (idVectorSet.size() >= 2) {

                idVectorSet.push(getCommonIds(idVectorSet.pop(), idVectorSet.pop()));
            }

            return idVectorSet.pop();
        } else {
            return null;
        }

    }

    /**
     * @param propertyName
     * @param propertyValue
     * @return
     * @throws InterruptedException
     */
    private int getNumberOfSearchResults(String propertyName, String propertyValue) throws InterruptedException {
        return mahasenManager.getNumberOfSearchResults(propertyName, propertyValue.trim());

    }

    /**
     * @param propertyName
     * @param initialValue
     * @param lastValue
     * @return
     * @throws InterruptedException
     */
    private int getNumberOfRangeSearchResults(String propertyName, String initialValue, String lastValue)
            throws InterruptedException {
        return mahasenManager.getNumberOfRangeSearchResults(propertyName, initialValue, lastValue);
    }
}
