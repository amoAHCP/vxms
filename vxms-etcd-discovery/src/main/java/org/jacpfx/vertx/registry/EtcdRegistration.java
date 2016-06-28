package org.jacpfx.vertx.registry;


import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import org.jacpfx.vertx.etcd.client.DiscoveryClientEtcd;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by jmader & Andy Moncsek
 */
public class EtcdRegistration {

    private final static Logger LOG = LoggerFactory.getLogger(EtcdRegistration.class);

    private static final List<Integer> SUCCESS_CODES = Collections.unmodifiableList(Arrays.asList(200, 201, 403));
    private static final String CONTENT_TYPE = HttpHeaders.CONTENT_TYPE.toString();
    private static final String APPLICATION_X_WWW_FORM_URLENCODED = HttpHeaders.APPLICATION_X_WWW_FORM_URLENCODED.toString();
    private static final String CACHE_KEY = "local";
    private static final String MAP_KEY = "cache";
    private final Vertx vertx;
    private final int ttl;
    private final String domainname;
    private final String servicename;
    private final String host;
    private final int port;
    private HttpClient httpClient;
    private final String nodename;
    private final SharedData data;
    private final URI fetchAll;


    public static final String ETCD_BASE_PATH = "/v2/keys/";

    private EtcdRegistration(Vertx vertx, String etcdHost, int etcdPort, int ttl, String domainname, String servicename, String nodename, String host, int port) {
        this.vertx = vertx;
        this.ttl = ttl;
        this.nodename = nodename;
        this.domainname = domainname;
        this.servicename = servicename.startsWith("/") ? servicename : "/" + servicename;
        this.host = host;
        this.port = port;
        // TODO check http(s)
        this.fetchAll = URI.create("http://" + etcdHost + ":" + etcdPort + ETCD_BASE_PATH + domainname + "/?recursive=true");
        data = vertx.sharedData();
        try {
            httpClient = vertx.createHttpClient(new HttpClientOptions()
                    .setDefaultHost(etcdHost)
                    .setDefaultPort(etcdPort)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void retrieveKeys(Consumer<Root> consumer) {
        httpClient.getAbs(fetchAll.toString(), handler -> handler.
                exceptionHandler(error -> {error.printStackTrace();consumer.accept(new Root());}).
                bodyHandler(body -> consumer.accept(decodeRoot(body)))
        ).end();


    }

    protected Root decodeRoot(Buffer body) {
        try {
            return Json.decodeValue(new String(body.getBytes()), Root.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Root();
    }


    public void connect(AsyncResultHandler<DiscoveryClient> asyncResultHandler) {
        connectToEtcd(vertx, httpClient, domainname, servicename, nodename, ttl, host, port, asyncResultHandler);
    }

    public void disconnect(Handler<HttpClientResponse> responseHandler) {
        deleteInstanceNode(httpClient, domainname, servicename, nodename, responseHandler, null);
    }

    private void connectToEtcd(Vertx vertx, HttpClient httpClient, String domainname, String servicename, String nodename, int ttl, String host, int port, AsyncResultHandler<DiscoveryClient> asyncResultHandler) {

        createServiceNode(httpClient, servicename,
                pathCreated -> {
                    //403 means the directory already existed
                    if (SUCCESS_CODES.contains(pathCreated.statusCode()))
                        createInstanceNode(httpClient, domainname, servicename, nodename, "value=" + Json.encode(new NodeMetadata(servicename, host, port, "http", false)) + "&ttl=" + ttl,
                                nodeCreated -> {
                                    if (SUCCESS_CODES.contains(nodeCreated.statusCode()) || 403 == nodeCreated.statusCode()) {
                                        retrieveKeys(root -> {
                                            putRootToCache(root);
                                            startNodeRefresh(vertx, httpClient, domainname, servicename, nodename, ttl, host, port);
                                            asyncResultHandler.handle(Future.factory.succeededFuture(new DiscoveryClientEtcd(httpClient, vertx, domainname, fetchAll)));
                                        });


                                    } else {
                                        LOG.error("Unable to create node (" + nodeCreated.statusCode() + ") " + nodeCreated.statusMessage());
                                        asyncResultHandler.handle(Future.factory.failureFuture(("Unable to create node node (" + nodeCreated.statusCode() + ") " + nodeCreated.statusMessage())));
                                    }

                                }, asyncResultHandler);
                    else {
                        LOG.error("Unable to create service node (" + pathCreated.statusCode() + ") " + pathCreated.statusMessage());
                        asyncResultHandler.handle(Future.factory.failureFuture("Unable to create service node (" + pathCreated.statusCode() + ") " + pathCreated.statusMessage()));
                    }
                }, asyncResultHandler);
    }

    private void putRootToCache(Root root) {
        final LocalMap<String, String> cache = data.getLocalMap(MAP_KEY);
        cache.put(CACHE_KEY, Json.encode(root));
    }

    private void startNodeRefresh(Vertx vertx, HttpClient httpClient, String domainname, String servicename, String nodename, int ttl, String host, int port) {
        LOG.info("Succeeded registering");
        // ttl in s setPeriodic in ms
        vertx.setPeriodic((ttl * 1000) - 900,
                // TODO check if http(s)
                refresh -> createInstanceNode(httpClient, domainname, servicename, nodename, "value=" + Json.encode(new NodeMetadata(servicename, host, port, "http", false)) + "&ttl=" + ttl, refreshed -> {
                    if (refreshed.statusCode() != 200) {
                        LOG.error("Unable to refresh node (" + refreshed.statusCode() + ") " + refreshed.statusMessage());
                    } else {
                        retrieveKeys(this::putRootToCache);
                    }
                }, null));
    }


    private void createServiceNode(HttpClient client, String serviceName, Handler<HttpClientResponse> responseHandler, AsyncResultHandler<DiscoveryClient> asyncResultHandler) {
        client
                .put(ETCD_BASE_PATH + domainname + serviceName)
                .putHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED)
                .handler(responseHandler)
                .exceptionHandler(error -> {
                    error.printStackTrace();
                    if (asyncResultHandler != null) asyncResultHandler.handle(Future.factory.failedFuture(error));
                    responseHandler.handle(new HttpClientResponseError(500));
                })
                .end("dir=true");
    }

    private static void createInstanceNode(HttpClient client, String domainname, String serviceName, String name, String data, Handler<HttpClientResponse> responseHandler, AsyncResultHandler<DiscoveryClient> asyncResultHandler) {
        client
                .put(ETCD_BASE_PATH + domainname + serviceName + "/" + name)
                .putHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED)
                .handler(responseHandler)
                .exceptionHandler(error -> {
                    error.printStackTrace();
                    if (asyncResultHandler != null) asyncResultHandler.handle(Future.factory.failedFuture(error));
                    responseHandler.handle(new HttpClientResponseError(500));
                })
                .end(data);
    }

    private void deleteInstanceNode(HttpClient client, String domainname, String serviceName, String name, Handler<HttpClientResponse> responseHandler, AsyncResultHandler<DiscoveryClient> asyncResultHandler) {
        client
                .delete(ETCD_BASE_PATH + domainname + serviceName + "/" + name)
                .handler(handler ->
                        retrieveKeys(root ->
                                Optional.ofNullable(root.getNode()).ifPresent(node -> {
                                    // TODO check why this is called twice, while second one is crap
                                    // idea: i register here a handler, at the same time an other handler is registered and this one will be executed twice?
                                    putRootToCache(root);
                                    responseHandler.handle(handler);
                                })))
                .exceptionHandler(error -> {
                    error.printStackTrace();
                    if (asyncResultHandler != null) asyncResultHandler.handle(Future.factory.failedFuture(error));
                    responseHandler.handle(new HttpClientResponseError(500));
                })
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
        DomainNameBuilder ttl(int ttl);
    }

    public interface DomainNameBuilder {
        ServiceNameBuilder domainName(String domainName);
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
        return vertx -> etcdHost -> etcdPort -> ttl -> domainname -> servicename -> serviceHost -> servicePort -> nodename -> new EtcdRegistration(vertx, etcdHost, etcdPort, ttl, domainname, servicename, nodename, serviceHost, servicePort);
    }
}