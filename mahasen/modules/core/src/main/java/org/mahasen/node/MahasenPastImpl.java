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

import org.apache.axis2.util.LoggingControl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mahasen.MahasenMemoryStorage;
import org.mahasen.messaging.ContentRemoveMessage;
import org.mahasen.messaging.MahasenSearchMessage;
import rice.Continuation;
import rice.environment.logging.Logger;
import rice.p2p.commonapi.*;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastContentHandle;
import rice.p2p.past.PastImpl;
import rice.p2p.past.messaging.InsertMessage;
import rice.p2p.past.messaging.PastMessage;
import rice.p2p.past.rawserialization.SocketStrategy;
import rice.p2p.util.rawserialization.JavaSerializedDeserializer;
import rice.persistence.StorageManager;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class MahasenPastImpl extends PastImpl {
    private static final Log log = LogFactory.getLog(MahasenPastImpl.class);

    ConcurrentHashMap<Integer, Continuation> searchCallback = new ConcurrentHashMap<Integer, Continuation>();

    /**
     * @param node
     * @param manager
     * @param replicas
     * @param instance
     */
    public MahasenPastImpl(Node node, StorageManager manager, int replicas, String instance) {
        super(node, manager, replicas, instance);
        this.endpoint.setDeserializer(new MahasenDeserializer());
    }

    /**
     * Overidden insert methode where we avoid LRU caching to keep the consistency of the system with the take operation
     *
     * @param obj
     * @param command
     */
    public void insert(final PastContent obj, final Continuation command) {
        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
            log.debug("Invoking the overrided insert method in MahasenPast");
        }

        super.doInsert(obj.getId(), new MessageBuilder() {
                    public PastMessage buildMessage() {
                        return new InsertMessage(getUID(), obj, getLocalNodeHandle(), obj.getId());
                    }
                }, new Continuation.StandardContinuation(command) {
                    public void receiveResult(final Object array) {
                        parent.receiveResult(array);
                    }
                }, socketStrategy.sendAlongSocket(SocketStrategy.TYPE_INSERT, obj)
        );
    }

    /**
     * @param id
     * @param command
     */
    public void delete(final Id id, final Continuation command) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting content for id " + id);
        }

        /* 1st do a local search and delete */
        lockManager.lock(id, new Continuation() {
            public void receiveResult(Object result) {
                storage.getObject(id, new Continuation() {
                    public void receiveResult(Object result) {
                        if (result instanceof MahasenPastContent) {

                            storage.unstore(id, new Continuation() {
                                public void receiveResult(Object result) {
                                    Boolean bool = (Boolean) result;

                                    if (bool.booleanValue() == true) {
                                        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                                            log.debug("succcesfully removed past object " + id + " from storage");
                                        }

                                        log.info("succcesfully removed past object " + id + " from storage");
                                    }

                                    lockManager.unlock(id);
                                }

                                public void receiveException(Exception result) {
                                    log.error("Error in unstoring content " + id + " releasing the lock");
                                    lockManager.unlock(id);
                                }
                            });
                        }

                    }

                    public void receiveException(Exception result) {
                        lockManager.unlock(id);
                    }
                });
            }

            public void receiveException(Exception exception) {
                lockManager.unlock(id);
            }
        });

        final NodeHandle localNodeHandle = this.getLocalNodeHandle();

        getHandles(id, replicationFactor + 1, new Continuation.StandardContinuation(command) {
            public void receiveResult(Object result) {
                NodeHandleSet replicas = (NodeHandleSet) result;

                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                    log.debug("Received replicas " + replicas + " for id " + id);
                    log.info("Received replicas " + replicas + " for id " + id);
                }

                for (int i = 0; i < replicas.size(); i++) {
                    NodeHandle nodeHandle = replicas.getHandle(i);

                    endpoint.route(nodeHandle.getId(), new ContentRemoveMessage(id, localNodeHandle), nodeHandle);

                    if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                        log.debug("Routing the content remove message");
                    }

                    log.info("Routing the content remove message to :" + nodeHandle.getId());
                    log.info("Routing the content remove message");
                }
            }
        });
    }

    /**
     * Method will be called when a message gets delivered to this application
     *
     * @param id
     * @param message
     */
    @Override
    public void deliver(Id id, Message message) {

        // delete content if the message recived is to delete a object
        if (message instanceof ContentRemoveMessage) {
            ContentRemoveMessage rMsg = (ContentRemoveMessage) message;

            handleContentRemoving(id, rMsg.getNodeHandle(), message);

            // if a search result request message
        } else if (message instanceof MahasenSearchMessage) {
            MahasenSearchMessage sMsg = (MahasenSearchMessage) message;

            System.out.println("Got message for search " + sMsg);

            // if message is a response to a search we handle it here
            if (sMsg.isResponse()) {
                log.debug("recieved a search response message " + message);
                handleResponse(sMsg);
            } else if (sMsg.isNumOfResults()) {
                if (sMsg.isRangeBased()) {
                    log.debug("Trying to get [no of] range search results for:" + sMsg);
                    handleGetNoOfRangeResults(sMsg.getPropertyName(), sMsg.getIntialVal(), sMsg.getLastVal(),
                            getResponseContinuation(sMsg));
                } else {
                    log.debug("Trying to get [no of] search results for:" + sMsg);
                    handleGetNoSearchResults(sMsg.getPropertyName(), sMsg.getPropertyValue(),
                            getResponseContinuation(sMsg));
                }
            } else {
                if (sMsg.isRangeBased()) {
                    log.debug("Trying to get range search results for:" + sMsg);
                    handleGetRangeResults(sMsg.getPropertyName(), sMsg.getIntialVal(), sMsg.getLastVal(),
                            getResponseContinuation(sMsg));
                } else {

                    log.debug("trying to get search result for property val:" + sMsg.getPropertyValue() +
                            " for: " + sMsg.getPropertyName());

                    handleGetSearchResults(sMsg.getPropertyName(), sMsg.getPropertyValue(),
                            getResponseContinuation(sMsg));
                }
            }

        } else {
            super.deliver(id, message);
        }
    }

    /**
     * @param id
     * @param originator
     * @param message
     */
    private void handleContentRemoving(Id id, final NodeHandle originator, Message message) {
        final long starttime = System.currentTimeMillis();
        final Id contentId = ((ContentRemoveMessage) message).getId();

        lockManager.lock(contentId, new Continuation() {
            public void receiveResult(Object result) {

                // lock acquired....
                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                    log.debug("Lock aquired for content " + contentId);
                }

                if (backup != null) {    // removing from the cache
                    backup.uncache(contentId, new Continuation() {
                        public void receiveResult(Object result) {
                            Boolean bool = (Boolean) result;

                            if (bool.booleanValue() == true) {
                                if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                                    log.debug("succcesfully unstored content " + contentId + " from the cache....");
                                }
                            }
                        }

                        public void receiveException(Exception result) {
                            log.error("Error in unstoring content " + contentId + " releasing the lock");
                            lockManager.unlock(contentId);
                        }
                    });
                }

                storage.getObject(contentId, new Continuation() {
                    public void receiveResult(Object result) {
                        if (result instanceof MahasenPastContent) {
                            MahasenPastContent content = (MahasenPastContent) result;

                            storage.unstore(contentId, new Continuation() {
                                public void receiveResult(Object result) {
                                    Boolean bool = (Boolean) result;

                                    if (bool.booleanValue() == true) {
                                        if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                                            log.debug("succcesfully removed content " + contentId + " from storage");
                                        }

                                        System.out.println("succcesfully removed content " + contentId
                                                + " from storage");

                                    }

                                    lockManager.unlock(contentId);
                                }

                                public void receiveException(Exception result) {
                                    log.error("Error in unstoring content " + contentId + " releasing the lock");
                                    lockManager.unlock(contentId);
                                }
                            });
                            System.out.println("Unstore " + (System.currentTimeMillis() - starttime));
                        }
                    }

                    public void receiveException(Exception result) {
                        lockManager.unlock(contentId);
                    }
                });
                storage.uncache(contentId, new Continuation() {
                    public void receiveResult(Object result) {
                        Boolean bool = (Boolean) result;

                        if (bool.booleanValue() == true) {
                            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                                log.debug("succcesfully unstored content " + contentId + " from the cache....");
                            }

                            System.out.println("succcesfully unstored content " + contentId + " from the cache....");
                        }

                        lockManager.unlock(contentId);
                    }

                    public void receiveException(Exception result) {
                        log.error("Error in unstoring content " + contentId + " releasing the lock");
                        lockManager.unlock(contentId);
                    }
                });
            }

            public void receiveException(Exception result) {
                log.error("Failed in aquiring lock for content: " + contentId);
                lockManager.unlock(contentId);
            }
        });
    }


    protected class MahasenDeserializer extends PastDeserializer {
        @Override
        public Message deserialize(InputBuffer buf, short type, int priority, NodeHandle sender) throws IOException {
            switch (type) {
                case 0: {
                    return new JavaSerializedDeserializer(endpoint).deserialize(buf, type, priority, sender);
                }
                case MahasenSearchMessage.TYPE: {
                    log.debug("Going to build MahasenSearch message in Mahasen Deserializer");
                    return MahasenSearchMessage.build(buf, endpoint, contentDeserializer);
                }
            }

            return super.deserialize(buf, type, priority, sender);
        }
    }

    /**
     * to get search results locally or if not get from replicas
     *
     * @param propertyName
     * @param propertyValue
     * @param c
     */
    public void getSearchResults(final String propertyName, final String propertyValue, final Continuation c) {
        if (storage.getStorage() instanceof MahasenMemoryStorage) {

            final rice.pastry.Id treeId = rice.pastry.Id.build(String.valueOf(propertyName.hashCode()));

            ((MahasenMemoryStorage) storage.getStorage()).getSearchResult(propertyName, propertyValue,
                    new Continuation() {

                        public void receiveResult(Object result) {

                            if (result != null) {
                                c.receiveResult(result);
                                log.debug("Getting results from local copy");
                            } else {
                                lookupHandles(treeId, replicationFactor + 1, new Continuation() {
                                    public void receiveResult(Object o) {
                                        final PastContentHandle[] handles = (PastContentHandle[]) o;

                                        for (int i = 0; i < handles.length; i++) {

                                            if (handles[i] != null) {
                                                NodeHandle nodeHandle = handles[i].getNodeHandle();
                                                MahasenSearchMessage sMsg =
                                                        new MahasenSearchMessage(getUID(), treeId, getLocalNodeHandle(),
                                                                nodeHandle.getId());
                                                sMsg.setPropertyName(propertyName);
                                                sMsg.setPropertyValue(propertyValue);
                                                sMsg.setNumOfResults(false);
                                                sMsg.setRangeBased(false);

                                                log.debug("Sending Search Request message for " + handles[i].
                                                        getNodeHandle() + "messaage " + sMsg);

                                                sendSearchRequest(nodeHandle.getId(), sMsg, nodeHandle,
                                                        new Continuation() {

                                                    public void receiveResult(Object result) {
                                                        log.debug("Recived search results..." + result);
                                                        c.receiveResult(result);
                                                    }

                                                    public void receiveException(Exception exception) {
                                                        log.error("Error while returing a search result");
                                                        c.receiveResult(null);
                                                    }
                                                });
                                            }
                                        }

                                    }

                                    public void receiveException(Exception e) {
                                        c.receiveException(e);
                                    }
                                });
                            }

                        }

                        public void receiveException(Exception exception) {
                            log.error("error while getting search results");
                        }
                    });
        }
    }

    /**
     * to get Number of search results in result set locally or if not get from replicas
     *
     * @param propertyName
     * @param propertyValue
     * @param c
     */
    public void getNumberOfSearchResults(final String propertyName, final String propertyValue, final Continuation c) {
        if (storage.getStorage() instanceof MahasenMemoryStorage) {

            final rice.pastry.Id treeId = rice.pastry.Id.build(String.valueOf(propertyName.hashCode()));

            ((MahasenMemoryStorage) storage.getStorage()).getNumberOfSearchResults(propertyName, propertyValue,
                    new Continuation() {

                        public void receiveResult(Object result) {

                            if (result != null) {
                                c.receiveResult(result);
                                log.debug("Getting results from local copy");
                            } else {
                                lookupHandles(treeId, replicationFactor + 1, new Continuation() {
                                    public void receiveResult(Object o) {
                                        final PastContentHandle[] handles = (PastContentHandle[]) o;

                                        for (int i = 0; i < handles.length; i++) {

                                            if (handles[i] != null) {
                                                NodeHandle nodeHandle = handles[i].getNodeHandle();
                                                MahasenSearchMessage sMsg =
                                                        new MahasenSearchMessage(getUID(), treeId, getLocalNodeHandle(),
                                                                nodeHandle.getId());
                                                sMsg.setPropertyName(propertyName);
                                                sMsg.setPropertyValue(propertyValue);
                                                sMsg.setNumOfResults(true);
                                                sMsg.setRangeBased(false);

                                                log.debug("Sending Search Request message for " + handles[i].
                                                        getNodeHandle() + "messaage " + sMsg);

                                                sendSearchRequest(nodeHandle.getId(), sMsg, nodeHandle,
                                                        new Continuation() {

                                                    public void receiveResult(Object result) {
                                                        log.debug("Recived search results..." + result);
                                                        c.receiveResult(result);
                                                    }

                                                    public void receiveException(Exception exception) {
                                                        log.error("Error while returing a search result");
                                                        c.receiveResult(null);
                                                    }
                                                });
                                            }
                                        }

                                    }

                                    public void receiveException(Exception e) {
                                        c.receiveException(e);
                                    }
                                });
                            }

                        }

                        public void receiveException(Exception exception) {
                            log.error("error while getting search results");
                        }
                    });
        }
    }

    /**
     * to get Range search results locally or if not get from replicas
     *
     * @param propertyName
     * @param initialValue
     * @param lastValue
     * @param c
     */
    public void getRangeSearchResults(final String propertyName, final String initialValue, final String lastValue,
                                      final Continuation c) {
        if (storage.getStorage() instanceof MahasenMemoryStorage) {

            final rice.pastry.Id treeId = rice.pastry.Id.build(String.valueOf(propertyName.hashCode()));

            ((MahasenMemoryStorage) storage.getStorage()).getRangeSearchResult(propertyName, initialValue, lastValue,
                    new Continuation() {

                        public void receiveResult(Object result) {

                            if (result != null) {
                                c.receiveResult(result);
                                log.debug("Getting results from local copy");
                            } else {
                                lookupHandles(treeId, replicationFactor + 1, new Continuation() {
                                    public void receiveResult(Object o) {
                                        final PastContentHandle[] handles = (PastContentHandle[]) o;

                                        for (int i = 0; i < handles.length; i++) {

                                            if (handles[i] != null) {
                                                NodeHandle nodeHandle = handles[i].getNodeHandle();
                                                MahasenSearchMessage sMsg =
                                                        new MahasenSearchMessage(getUID(), treeId, getLocalNodeHandle(),
                                                                nodeHandle.getId());
                                                sMsg.setPropertyName(propertyName);
                                                sMsg.setIntialVal(initialValue);
                                                sMsg.setLastVal(lastValue);
                                                sMsg.setNumOfResults(false);
                                                sMsg.setRangeBased(true);

                                                log.debug("Sending Search Request message for " + handles[i].
                                                        getNodeHandle() + "messaage " + sMsg);

                                                sendSearchRequest(nodeHandle.getId(), sMsg, nodeHandle,
                                                        new Continuation() {

                                                    public void receiveResult(Object result) {
                                                        log.debug("Recived search results..." + result);
                                                        c.receiveResult(result);
                                                    }

                                                    public void receiveException(Exception exception) {
                                                        log.error("Error while returing a search result");
                                                        c.receiveResult(null);
                                                    }
                                                });
                                            }
                                        }

                                    }

                                    public void receiveException(Exception e) {
                                        c.receiveException(e);
                                    }
                                });
                            }

                        }

                        public void receiveException(Exception exception) {
                            log.error("error while getting search results");
                        }
                    });
        }
    }

    /**
     * To get no of RangeS Search Results ,try locally first then go for replicas
     *
     * @param propertyName
     * @param initialValue
     * @param lastValue
     * @param c
     */
    public void getNumberOfRangeSearchResults(final String propertyName, final String initialValue,
                                              final String lastValue,
                                              final Continuation c) {
        if (storage.getStorage() instanceof MahasenMemoryStorage) {

            final rice.pastry.Id treeId = rice.pastry.Id.build(String.valueOf(propertyName.hashCode()));

            ((MahasenMemoryStorage) storage.getStorage()).getNumberOfRangeSearchResults(propertyName, initialValue,
                    lastValue,
                    new Continuation() {

                        public void receiveResult(Object result) {

                            if (result != null) {
                                c.receiveResult(result);
                                log.debug("Getting results from local copy");
                            } else {
                                lookupHandles(treeId, replicationFactor + 1, new Continuation() {
                                    public void receiveResult(Object o) {
                                        final PastContentHandle[] handles = (PastContentHandle[]) o;

                                        for (int i = 0; i < handles.length; i++) {

                                            if (handles[i] != null) {
                                                NodeHandle nodeHandle = handles[i].getNodeHandle();
                                                MahasenSearchMessage sMsg =
                                                        new MahasenSearchMessage(getUID(), treeId, getLocalNodeHandle(),
                                                                nodeHandle.getId());
                                                sMsg.setPropertyName(propertyName);
                                                sMsg.setIntialVal(initialValue);
                                                sMsg.setLastVal(lastValue);
                                                sMsg.setNumOfResults(true);
                                                sMsg.setRangeBased(true);

                                                log.debug("Sending Search Request message for " + handles[i].
                                                        getNodeHandle() + "messaage " + sMsg);

                                                sendSearchRequest(nodeHandle.getId(), sMsg, nodeHandle,
                                                        new Continuation() {

                                                    public void receiveResult(Object result) {
                                                        log.debug("Recived search results..." + result);
                                                        c.receiveResult(result);
                                                    }

                                                    public void receiveException(Exception exception) {
                                                        log.error("Error while returing a search result");
                                                        c.receiveResult(null);
                                                    }
                                                });
                                            }
                                        }

                                    }

                                    public void receiveException(Exception e) {
                                        c.receiveException(e);
                                    }
                                });
                            }

                        }

                        public void receiveException(Exception exception) {
                            log.error("error while getting search results");
                        }
                    });
        }
    }

    /**
     * To get Search result for a given value
     *
     * @param propertyName
     * @param propertyValue
     * @param c
     */
    private void handleGetSearchResults(final String propertyName, final String propertyValue, final Continuation c) {
        if (storage.getStorage() instanceof MahasenMemoryStorage) {
            ((MahasenMemoryStorage) storage.getStorage()).getSearchResult(propertyName, propertyValue,
                    new Continuation.StandardContinuation(c) {

                        public void receiveResult(Object result) {
                            parent.receiveResult(result);
                        }
                    });
        }
    }

    /**
     * To get a range search result eg : 10 to 100
     *
     * @param propertyName
     * @param initialValue
     * @param lastValue
     * @param c
     */
    private void handleGetRangeResults(final String propertyName, final String initialValue, final String lastValue,
                                       final Continuation c) {
        if (storage.getStorage() instanceof MahasenMemoryStorage) {
            ((MahasenMemoryStorage) storage.getStorage()).getRangeSearchResult(propertyName, initialValue, lastValue,
                    new Continuation.StandardContinuation(c) {

                        public void receiveResult(Object result) {
                            parent.receiveResult(result);
                        }
                    });
        }
    }

    /**
     * To See how many no of results are available in the result set .
     *
     * @param propertyName
     * @param propertyValue
     * @param c
     */
    private void handleGetNoSearchResults(final String propertyName, final String propertyValue,
                                          final Continuation c) {
        if (storage.getStorage() instanceof MahasenMemoryStorage) {
            ((MahasenMemoryStorage) storage.getStorage()).getNumberOfSearchResults(propertyName, propertyValue,
                    new Continuation.StandardContinuation(c) {

                        public void receiveResult(Object result) {
                            parent.receiveResult(result);
                        }
                    });
        }
    }

    /**
     * To see how many range results are available  in the result set.
     *
     * @param propertyName
     * @param initialValue
     * @param lastValue
     * @param c
     */
    private void handleGetNoOfRangeResults(final String propertyName, final String initialValue, final String lastValue,
                                           final Continuation c) {
        if (storage.getStorage() instanceof MahasenMemoryStorage) {
            ((MahasenMemoryStorage) storage.getStorage()).getNumberOfRangeSearchResults(propertyName, initialValue,
                    lastValue,
                    new Continuation.StandardContinuation(c) {

                        public void receiveResult(Object result) {
                            parent.receiveResult(result);
                        }
                    });
        }
    }

    /**
     * @param message
     */
    private void handleResponse(PastMessage message) {
        if (logger.level <= Logger.FINE) logger.log("handling reponse message " + message + " from the request");
        Continuation command = removePending(message.getUID());

        if (command != null) {
            message.returnResponse(command, environment, instance);
        }
    }

    /**
     * @param uid
     * @return
     */
    private Continuation removePending(int uid) {
        if (logger.level <= Logger.FINER) logger.log("Removing and returning continuation " + uid + " from search" +
                " callback table");

        return searchCallback.remove(new Integer(uid));
    }

    /**
     * @param id
     * @param message
     * @param hint
     * @param command
     */
    protected void sendSearchRequest(Id id, PastMessage message, NodeHandle hint, Continuation command) {
        if (logger.level <= Logger.FINER)
            logger.log("Sending request message " + message + " {" + message.getUID() + "} to id " + id + " via "
                    + hint);

        searchCallback.put(message.getUID(), command);
        endpoint.route(id, message, hint);
    }

}

