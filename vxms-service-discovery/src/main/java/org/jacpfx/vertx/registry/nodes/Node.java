/*
 * Copyright [2017] [Andy Moncsek]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jacpfx.vertx.registry.nodes;

import io.vertx.core.shareddata.Shareable;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Created by Andy Moncsek on 04.05.16.
 * Represents a service node with all metadata to connect the Service. Is used to register a service at the discovery server
 */
public class Node implements Serializable, Shareable {
    private final boolean dir;
    private final String key;
    private final String value;
    private final String expiration;
    private final int ttl;
    private final int modifiedIndex;
    private final int createdIndex;
    private final List<Node> nodes;

    public Node(boolean dir, String key, String value, String expiration, int ttl, int modifiedIndex, int createdIndex, List<Node> nodes) {
        this.dir = dir;
        this.key = key;
        this.value = value;
        this.expiration = expiration;
        this.ttl = ttl;
        this.modifiedIndex = modifiedIndex;
        this.createdIndex = createdIndex;
        this.nodes = nodes;
    }

    public Node() {
        this(false, "", "", "", 0, 0, 0, Collections.emptyList());
    }


    public static Dir create() {
        return dirVal ->
                keyVal ->
                        valueVal ->
                                expirationVal ->
                                        ttlVal ->
                                                modifiedIndexVal ->
                                                        createdIndexVal ->
                                                                nodesVal ->
                                                                        new Node(dirVal, keyVal, valueVal, expirationVal, ttlVal, modifiedIndexVal, createdIndexVal, nodesVal);
    }


    public static Node emptyNode() {
        return new Node(false, "", "", "", 0, 0, 0, Collections.emptyList());
    }


    public boolean isDir() {
        return dir;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getExpiration() {
        return expiration;
    }

    public int getTtl() {
        return ttl;
    }

    public int getModifiedIndex() {
        return modifiedIndex;
    }

    public int getCreatedIndex() {
        return createdIndex;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "Node{" +
                "dir=" + dir +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", expiration='" + expiration + '\'' +
                ", ttl=" + ttl +
                ", modifiedIndex=" + modifiedIndex +
                ", createdIndex=" + createdIndex +
                ", nodes=" + nodes +
                '}';
    }

    public interface Nodes {
        Node nodes(List<Node> nodes);
    }

    public interface CreateIndex {
        Nodes createIndex(int createdIndex);
    }

    public interface ModifyIndex {
        CreateIndex modifiedIndex(int modifiedIndex);
    }

    public interface TTL {
        ModifyIndex ttl(int ttl);
    }

    public interface Expiration {
        TTL expiration(String expiration);
    }

    public interface Value {
        Expiration value(String value);
    }

    public interface Key {
        Value key(String Key);
    }

    public interface Dir {
        Key dir(boolean dir);
    }

}
