package org.jacpfx.vertx.registry;

import io.vertx.core.http.HttpClient;
import io.vertx.core.json.Json;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

import static org.jacpfx.vertx.registry.EtcdRegistration.ETCD_BASE_PATH;

/**
 * Created by Andy Moncsek on 29.05.16.
 */
public class DiscoveryClient {
    private static final String CACHE_KEY = "local";
    private static final String MAP_KEY = "cache";
    private final HttpClient httpClient;
    private final SharedData data;
    private final String domainname;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    public DiscoveryClient(HttpClient httpClient, SharedData data, String domainname) {
        this.httpClient = httpClient;
        this.data = data;
        this.domainname = domainname;
    }

    public void findNode(String serviceName, Consumer<NodeResponse> consumer) {
        retrieveKeysFromCache(root -> {
            final Node serviceNode = findNode(root.getNode(), "/" + domainname + serviceName);
            if (serviceNode.getKey().equals("/" + domainname + serviceName)) {
                final Optional<Node> first = serviceNode.getNodes().stream().findAny();
                if (!first.isPresent()) {
                    consumer.accept(new NodeResponse(serviceNode, Collections.emptyList(), domainname, false, new NodeNotFoundException("no active node found")));
                }
                first.ifPresent(node -> handleServiceNode(serviceName, consumer, node, serviceNode));
            } else {
                findNodeFromEtcd(serviceName, consumer);
            }
        });
    }

    public void findService(String serviceName, Consumer<NodeResponse> consumer) {
        retrieveKeys(root -> {
            Node node = findNode(root.getNode(), "/" + domainname + serviceName);
            if (node == null || node.getKey().isEmpty()) {
                consumer.accept(new NodeResponse(node, Collections.emptyList(), domainname, false, new NodeNotFoundException("no node found")));
            } else {
                consumer.accept(new NodeResponse(node, Collections.emptyList(), domainname, true, null));
            }

        });
    }


    /**
     * @param serviceName the service name to find
     * @param consumer    the consumer to execute
     * @param node        the first node found
     * @param serviceNode the parent node
     */
    private void handleServiceNode(String serviceName, Consumer<NodeResponse> consumer, Node node, Node serviceNode) {
        LocalDateTime dateTime = LocalDateTime.parse(node.getExpiration(), formatter);
        ZonedDateTime nowUTC = ZonedDateTime.now(ZoneOffset.UTC);
        // check if expire date is still valid
        if (dateTime.compareTo(nowUTC.toLocalDateTime()) >= 0) {
            consumer.accept(new NodeResponse(node, serviceNode.getNodes(), domainname, true, null));
        } else {
            findNodeFromEtcd(serviceName, consumer);
        }
    }

    private void findNodeFromEtcd(String serviceName, Consumer<NodeResponse> consumer) {
        retrieveKeys(root -> {
            putRootToCache(root);
            final Node serviceNode = findNode(root.getNode(), "/" + domainname + serviceName);
            if (serviceNode.getKey().equals("/" + domainname + serviceName)) {
                final Optional<Node> first = serviceNode.getNodes().stream().findFirst();
                if (!first.isPresent()) {
                    consumer.accept(new NodeResponse(serviceNode, Collections.emptyList(), domainname, false, new NodeNotFoundException("no active node found")));
                }

                first.ifPresent(node -> consumer.accept(new NodeResponse(node, serviceNode.getNodes(), domainname, true, null)));
            } else {
                consumer.accept(new NodeResponse(serviceNode, Collections.emptyList(), domainname, false, new NodeNotFoundException("service not found")));
            }
        });
    }



    public void retrieveKeys(Consumer<Root> consumer) {
        httpClient.getNow(ETCD_BASE_PATH + domainname + "/?recursive=true", handler -> {
            handler.bodyHandler(body -> {
                try {
                    consumer.accept(Json.decodeValue(new String(body.getBytes()), Root.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        });

    }

    public void retrieveKeysFromCache(Consumer<Root> consumer) {
        final LocalMap<String, String> cache = data.getLocalMap(MAP_KEY);
        final String local = cache.get(CACHE_KEY);
        if (local != null) {
            consumer.accept(Json.decodeValue(local, Root.class));
        }
    }

    private Node findNode(Node node, String key) {
        if (node.getKey() != null && node.getKey().equals(key)) return node;
        if (node.isDir() && node.getNodes() != null) return node.getNodes().stream().filter(n1 -> {
            final Node n2 = n1.isDir() ? findNode(n1, key) : n1;
            return n2.getKey().equals(key);
        }).findFirst().orElse(new Node(false, "", "", "", 0, 0, 0, Collections.emptyList()));
        return new Node(false, "", "", "", 0, 0, 0, Collections.emptyList());
    }

    private void putRootToCache(Root root) {
        final LocalMap<String, String> cache = data.getLocalMap(MAP_KEY);
        cache.put(CACHE_KEY, Json.encode(root));
    }
}
