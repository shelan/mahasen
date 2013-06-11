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

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mahasen.resource.MahasenResource;
import rice.p2p.commonapi.Id;
import rice.p2p.past.ContentHashPastContent;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastException;

public class MahasenPastContent extends ContentHashPastContent {
    private static final Log log = LogFactory.getLog(MahasenPastContent.class);
    private boolean isToUpdate = false;
    protected MahasenResource mahasenResourse;

    /**
     * @param myId
     * @param resource
     */
    public MahasenPastContent(Id myId, MahasenResource resource) {
        super(myId);
        mahasenResourse = resource;
    }

    /**
     * @param id
     * @param existingContent
     * @return
     * @throws PastException
     */
    @Override
    public PastContent checkInsert(Id id, PastContent existingContent) throws PastException {
        if (existingContent != null) {
            if (isToUpdate && (existingContent instanceof MahasenPastContent)) {

                MahasenResource updatedMahasenResource =
                        updateMahasenResource(((MahasenPastContent) existingContent).getMahasenResourse());
                PastContent updatedContent = existingContent;

                ((MahasenPastContent) updatedContent).setMahasenResourse(updatedMahasenResource);

                log.info("Existing MahasenResource was successfully updated");

                return updatedContent;
            } else {
                return this;
            }
        }

        // only allow correct content hash key
        if (!id.equals(getId())) {
            return null;
        }
        return this;
    }

    /**
     * @param resourceToUpdate
     * @return
     */
    private MahasenResource updateMahasenResource(MahasenResource resourceToUpdate) {

        // file size and uploaded date has not been modified here since they cannot change
        resourceToUpdate.addAllPropeties(mahasenResourse.getProperties());

        resourceToUpdate.addPartNames(mahasenResourse.getPartNames().toArray(new String
                [mahasenResourse.getPartNames().size()]));

        for (String partName : mahasenResourse.getPartNames()) {
            for (String ip : mahasenResourse.getSplittedPartsIpTable().get(partName))
                resourceToUpdate.addSplitPartStoredIp(partName, ip);
        }


        for (String tag : this.getMahasenResourse().getTags()) {
            resourceToUpdate.addTag(tag);
        }
        return resourceToUpdate;
    }

    /**
     * @return
     */
    public MahasenResource getMahasenResourse() {
        return mahasenResourse;
    }

    /**
     * @param mahasenResourse
     */
    public void setMahasenResourse(MahasenResource mahasenResourse) {
        this.mahasenResourse = mahasenResourse;
    }

    /**
     * @param isToUpdate
     */
    public void setIsToUpdate(boolean isToUpdate) {
        this.isToUpdate = isToUpdate;
    }

    /**
     * @return
     */
    public boolean getIsToUpdate() {
        return this.isToUpdate;
    }
}


