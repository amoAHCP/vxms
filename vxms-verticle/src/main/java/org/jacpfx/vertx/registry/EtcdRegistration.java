package org.jacpfx.vertx.registry;


import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by jmader & Andy Moncsek
 */
public class EtcdRegistration {

    private final static Logger LOG = LoggerFactory.getLogger(EtcdRegistration.class);

    private static final List<Integer> SUCCESS_CODES = Collections.unmodifiableList(Arrays.asList(200, 201, 403));
    public static final String CONTENT_TYPE = HttpHeaders.CONTENT_TYPE.toString();
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = HttpHeaders.APPLICATION_X_WWW_FORM_URLENCODED.toString();

    private Vertx vertx;
    private int ttl;
    private String servicename;
    private String hostAndPort;
    private HttpClient httpClient;
    private String nodename;


    public static final String ETCD_BASE_PATH = "/v2/keys/";

    private EtcdRegistration(Vertx vertx, String etcdHost, int etcdPort, int ttl, String servicename, String nodename, String hostAndPort) {
        this.vertx = vertx;
        this.ttl = ttl;
        this.nodename = nodename;
        this.servicename = servicename;
        this.hostAndPort = hostAndPort;
        try {
            httpClient = vertx.createHttpClient(new HttpClientOptions()
                    .setDefaultHost(etcdHost)
                    .setDefaultPort(etcdPort)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void findService(Consumer<Node> consumer, String serviceName) {
        retrieveKeys(root -> consumer.accept(findNode(root.getNode(), serviceName)));
    }

    public void retrieveKeys(Consumer<Root> consumer) {
        httpClient.getNow(ETCD_BASE_PATH + "?recursive=true", handler -> {
            handler.bodyHandler(body -> {
                try {
                    System.out.println(new String(body.getBytes()) + " \n\n\n");
                    Root root = Json.decodeValue(new String(body.getBytes()), Root.class);
                    consumer.accept(root);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        });

    }

    private Node findNode(Node node, String value) {
        if (node.getKey() != null && node.getKey().equals(value)) return node;
        if (node.isDir()) return node.getNodes().stream().filter(n1 -> {
            final Node n2 = n1.isDir() ? findNode(n1, value) : n1;
            return n2.getKey().equals(value);
        }).findFirst().orElse(new Node(false, "", "", "", 0, 0, 0, Collections.emptyList()));
        return null;
    }


    public void connect(AsyncResultHandler<Void> asyncResultHandler) {
        connectToEtcd(vertx, httpClient, servicename, nodename, ttl, hostAndPort, asyncResultHandler);
    }

    public void disconnect(Handler<HttpClientResponse> responseHandler) {
        deleteInstanceNode(httpClient, servicename, nodename, responseHandler);
    }

    private static void connectToEtcd(Vertx vertx, HttpClient httpClient, String servicename, String nodename, int ttl, String hostAndPort, AsyncResultHandler<Void> asyncResultHandler) {

        createServiceNode(httpClient, servicename,
                pathCreated -> {
                    //403 means the directory already existed
                    if (SUCCESS_CODES.contains(pathCreated.statusCode()))
                        createInstanceNode(httpClient, servicename, nodename, "value=" + hostAndPort + "&ttl=" + ttl,
                                nodeCreated -> {
                                    if (SUCCESS_CODES.contains(nodeCreated.statusCode()) || 403 == nodeCreated.statusCode()) {
                                        startNodeRefresh(vertx, httpClient, servicename, nodename, ttl, hostAndPort, asyncResultHandler);
                                    } else {
                                        LOG.error("Unable to create node (" + nodeCreated.statusCode() + ") " + nodeCreated.statusMessage());
                                        asyncResultHandler.handle(Future.factory.completedFuture("Unable to create node node (" + nodeCreated.statusCode() + ") " + nodeCreated.statusMessage(), true));
                                    }

                                });
                    else {
                        LOG.error("Unable to create service node (" + pathCreated.statusCode() + ") " + pathCreated.statusMessage());
                        asyncResultHandler.handle(Future.factory.completedFuture("Unable to create service node (" + pathCreated.statusCode() + ") " + pathCreated.statusMessage(), true));
                    }
                });
    }

    private static void startNodeRefresh(Vertx vertx, HttpClient httpClient, String servicename, String nodename, int ttl, String hostAndPort, AsyncResultHandler<Void> asyncResultHandler) {
        LOG.info("Succeeded registering");
        // ttl in s setPeriodic in ms
        vertx.setPeriodic((ttl * 1000) - 900,
                refresh -> createInstanceNode(httpClient, servicename, nodename, "value=" + hostAndPort + "&ttl=" + ttl, refreshed -> {
                    if (refreshed.statusCode() != 200) {
                        LOG.error("Unable to refresh node (" + refreshed.statusCode() + ") " + refreshed.statusMessage());
                    }
                }));
        asyncResultHandler.handle(Future.factory.completedFuture(null));
    }


    private static void createServiceNode(HttpClient client, String serviceName, Handler<HttpClientResponse> responseHandler) {
        try {
            client
                    .put(ETCD_BASE_PATH + serviceName)
                    .putHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED)
                    .handler(responseHandler)
                    .exceptionHandler(error -> {
                        error.printStackTrace();
                        responseHandler.handle(new HttpClientResponseError(500));
                    })
                    .end("dir=true");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void createInstanceNode(HttpClient client, String serviceName, String name, String data, Handler<HttpClientResponse> responseHandler) {
        client
                .put(ETCD_BASE_PATH + serviceName + "/" + name)
                .putHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED)
                .handler(responseHandler)
                .end(data);
    }

    private static void deleteInstanceNode(HttpClient client, String serviceName, String name, Handler<HttpClientResponse> responseHandler) {
        client
                .delete(ETCD_BASE_PATH + serviceName + "/" + name)
                .handler(responseHandler)
                .end();
    }


    public interface VertxBuilder {
        EtcdHostBuilder vertx(Vertx vertx);
    }

    public interface EtcdHostBuilder {
        EtcdPortBuilder etcdHost(String etcdHost);
    }

    public interface EtcdPortBuilder {
        TtlBuilder etcdPort(int etcdPort);
    }

    public interface TtlBuilder {
        ServiceNameBuilder ttl(int ttl);
    }

    public interface ServiceNameBuilder {
        ServiceHostBuilder serviceName(String serviceName);
    }

    public interface ServiceHostBuilder {
        ServicePortBuilder serviceHost(String serviceHost);
    }

    public interface ServicePortBuilder {
        NodeNameBuilder servicePort(int servicePort);
    }

    public interface NodeNameBuilder {
        EtcdRegistration nodeName(String nodeName);
    }

    public static VertxBuilder buildRegistration() {
        return vertx -> etcdHost -> etcdPort -> ttl -> servicename -> serviceHost -> servicePort -> nodename -> new EtcdRegistration(vertx, etcdHost, etcdPort, ttl, servicename, nodename, serviceHost + ":" + servicePort);
    }
}