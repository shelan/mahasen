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

import org.mahasen.MahasenConstants;
import org.mahasen.exception.MahasenException;
import org.mahasen.resource.MahasenResource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import rice.p2p.past.PastException;
import rice.pastry.Id;

import java.util.Hashtable;

public class UpdateUtil extends AbstractCommonUtil {

    MahasenResource mahasenResource;

    /**
     * @param fileToUpdate
     * @param tags
     * @param propeties
     * @throws RegistryException
     * @throws PastException
     * @throws InterruptedException
     * @throws MahasenException
     */
    public void updateMetadata(String fileToUpdate, String tags, Hashtable<String, String> propeties)
            throws RegistryException, PastException, InterruptedException, MahasenException {

        Id resourceId = Id.build(String.valueOf((MahasenConstants.ROOT_REGISTRY_PATH
                + fileToUpdate).hashCode()));

        if (mahasenManager.lookupDHT(resourceId) == null) {
            throw new MahasenException("File not found");
        }

        mahasenResource = createMahasenResource(resourceId);
        mahasenResource = updateProperties(mahasenResource, propeties);
        mahasenResource = updateTags(mahasenResource, tags);

        mahasenManager.insertTreeMapIntoDHT(resourceId, mahasenResource, false);
        mahasenManager.insertIntoDHT(resourceId, mahasenResource, true);

    }

    /**
     * @param mahasenResource
     * @param userDefinedProperties
     * @return
     */
    private MahasenResource updateProperties(MahasenResource mahasenResource,
                                             Hashtable<String, String> userDefinedProperties) {

        Hashtable<String, String> userProperties = removeSystemMetadataFromUserMetadata(userDefinedProperties);
        mahasenResource.addAllPropeties(userProperties);

        return mahasenResource;

    }

    /**
     * @param mahasenResource
     * @param tagString
     * @return
     */
    private MahasenResource updateTags(MahasenResource mahasenResource, String tagString) {

        String tags[] = tagString.split(",");

        for (String tag : tags) {
            mahasenResource.addTag(tag);
        }

        return mahasenResource;
    }
}
