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

package org.jacpfx.vertx.registry;


import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import org.jacpfx.common.AsyncResultHandler;
import org.jacpfx.vertx.etcd.client.DiscoveryClientEtcd;
import org.jacpfx.vertx.registry.nodes.NodeMetadata;
import org.jacpfx.vertx.registry.nodes.Root;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by jmader and Andy Moncsek
 */
public class EtcdRegistration {

    private final static Logger LOG = LoggerFactory.getLogger(EtcdRegistration.class);

    private static final List<Integer> SUCCESS_CODES = Collections.unmodifiableList(Arrays.asList(200, 201, 403));
    private static final String CONTENT_TYPE = HttpHeaders.CONTENT_TYPE.toString();
    private static final String APPLICATION_X_WWW_FORM_URLENCODED = HttpHeaders.APPLICATION_X_WWW_FORM_URLENCODED.toString();
    private static final String CACHE_KEY = "local";
    private static final String MAP_KEY = "cache";
    public static final String ROOT = "/";
    private final Vertx vertx;
    private final int ttl;
    private final String domainname;
    private final String servicename;
    private final String host;
    private final String etcdhost;
    private final int port;
    private final int etcdport;
    private final String nodename;
    private final String contextRoot;
    private final boolean secure;
    private final SharedData data;
    private final URI fetchAll;
    private final HttpClientOptions options;
    private static final String ETCD_BASE_PATH = "/v2/keys/";
    private static final String HTTPS = "https://";
    private static final String HTTP = "http://";


    /**
     * @param vertx         the Vert.x instance
     * @param clientOptions the http client options to connect to etcd
     * @param etcdHost      the etcd host name
     * @param etcdPort      the etcd connection port
     * @param ttl           the default ttl tile for an entry
     * @param domainname    the domain name (all entries in one domain group under the same root enry)
     * @param servicename   the name/key of the service
     * @param host          the service host
     * @param port          the service port
     * @param contextRoot   the service context root
     * @param secure        true if ssl connection is used
     * @param nodename      the node/instance name
     */
    private EtcdRegistration(Vertx vertx, HttpClientOptions clientOptions, String etcdHost, int etcdPort, int ttl, String domainname, String servicename, String host, int port, String contextRoot, boolean secure, String nodename) {
        this.vertx = vertx;
        this.ttl = ttl;
        this.nodename = nodename;
        this.domainname = domainname;
        this.servicename = cleanPath(servicename);
        this.contextRoot = cleanPath(contextRoot);
        this.host = host;
        this.port = port;
        this.secure = secure;
        this.etcdhost = etcdHost;
        this.etcdport = etcdPort;
        this.fetchAll = URI.create(clientOptions.isSsl() ? HTTPS : HTTP + etcdHost + ":" + etcdPort + ETCD_BASE_PATH + domainname + "/?recursive=true");
        this.data = vertx.sharedData();
        this.options = clientOptions;

    }

    private  HttpClientOptions getOptions() {
        return options
                .setDefaultHost(etcdhost)
                .setDefaultPort(etcdport);
    }

    private static String cleanPath(String path) {
        final String path01 = path.startsWith(ROOT) ? path : ROOT + path;
        final int len = path01.length();
        if (path01.charAt(len - 1) == '/') return path01.substring(0, len - 1);
        return path01;
    }


    public void retrieveKeys(Consumer<Root> consumer) {
        vertx.createHttpClient(getOptions()).getAbs(fetchAll.toString(), handler -> handler.
                exceptionHandler(error -> {
                    error.printStackTrace();
                    consumer.accept(new Root());
                }).
                bodyHandler(body -> consumer.accept(decodeRoot(body)))
        ).end();


    }

    private Root decodeRoot(Buffer body) {
        try {
            return Json.decodeValue(new String(body.getBytes()), Root.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Root();
    }


    public void connect(AsyncResultHandler<DiscoveryClient> asyncResultHandler) {
        connectToEtcd(vertx, domainname, servicename, nodename, ttl, contextRoot, host, port, secure, asyncResultHandler);
    }

    public void disconnect(Handler<HttpClientResponse> responseHandler) {
        deleteInstanceNode(domainname, servicename, nodename, responseHandler, null);
    }

    private void connectToEtcd(Vertx vertx, String _domainname, String _servicename, String _nodename, int _ttl, String _contextRoot, String _host, int _port, boolean _secure, AsyncResultHandler<DiscoveryClient> _asyncResultHandler) {
        createServiceNode(_servicename,
                pathCreated -> {
                    //403 means the directory already existed
                    if (SUCCESS_CODES.contains(pathCreated.statusCode()))
                        // TODO handle secure connections
                        createInstanceNode(_domainname, _servicename, _nodename, "value=" + Json.encode(new NodeMetadata(_contextRoot, _host, _port, _secure)) + "&ttl=" + ttl,
                                nodeCreated -> {
                                    if (SUCCESS_CODES.contains(nodeCreated.statusCode()) || 403 == nodeCreated.statusCode()) {
                                        retrieveKeys(root -> {
                                            putRootToCache(root);
                                            startNodeRefresh(vertx, _domainname, _servicename, nodename, _ttl, _contextRoot, _host, _port, _secure);
                                            _asyncResultHandler.handle(Future.factory.succeededFuture(new DiscoveryClientEtcd(vertx, options, _domainname, fetchAll)));
                                        });
                                    } else {
                                        LOG.error("Unable to create node (" + nodeCreated.statusCode() + ") " + nodeCreated.statusMessage());
                                        _asyncResultHandler.handle(Future.factory.failureFuture(("Unable to create node node (" + nodeCreated.statusCode() + ") " + nodeCreated.statusMessage())));
                                    }

                                }, _asyncResultHandler);
                    else {
                        LOG.error("Unable to create service node (" + pathCreated.statusCode() + ") " + pathCreated.statusMessage());
                        _asyncResultHandler.handle(Future.factory.failureFuture("Unable to create service node (" + pathCreated.statusCode() + ") " + pathCreated.statusMessage()));
                    }
                }, _asyncResultHandler);
    }

    private void putRootToCache(Root root) {
        final LocalMap<String, Root> cache = data.getLocalMap(MAP_KEY);
        cache.put(CACHE_KEY, root);
    }

    private void startNodeRefresh(Vertx vertx, String _domainname, String _servicename, String _nodename, int ttl, String _contextRoot, String _host, int _port, boolean _secure) {
        LOG.info("Succeeded registering");
        // ttl in s setPeriodic in ms
        vertx.setPeriodic((ttl * 1000) - 900,
                refresh -> createInstanceNode(_domainname, _servicename, _nodename, "value=" + Json.encode(new NodeMetadata(_contextRoot, _host, _port, _secure)) + "&ttl=" + ttl, refreshed -> {
                    if (refreshed.statusCode() != 200) {
                        LOG.error("Unable to refresh node (" + refreshed.statusCode() + ") " + refreshed.statusMessage()+"  HOST: "+getOptions().getDefaultHost()+" PORT: "+getOptions().getDefaultPort());
                    } else {
                        retrieveKeys(this::putRootToCache);
                    }
                }, null));
    }


    private void createServiceNode(String serviceName, Handler<HttpClientResponse> responseHandler, AsyncResultHandler<DiscoveryClient> asyncResultHandler) {
        vertx.createHttpClient(getOptions())
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

    private void createInstanceNode(String domainname, String serviceName, String name, String data, Handler<HttpClientResponse> responseHandler, AsyncResultHandler<DiscoveryClient> asyncResultHandler) {
        LOG.info("create {0}",serviceName);
        vertx.createHttpClient(getOptions())
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

    private void deleteInstanceNode(String domainname, String serviceName, String name, Handler<HttpClientResponse> responseHandler, AsyncResultHandler<DiscoveryClient> asyncResultHandler) {
        // create new Vert.x instance to be sure that connection is still possible, even when the service verticle is currently shuts down and Vert.x instance is closed
        LOG.info("delete {0}",serviceName);
        Vertx.vertx().createHttpClient(getOptions())
                .delete(ETCD_BASE_PATH + domainname + serviceName + "/" + name)
                .handler(responseHandler::handle)
                .exceptionHandler(error -> {
                    error.printStackTrace();
                    if (asyncResultHandler != null) asyncResultHandler.handle(Future.factory.failedFuture(error));
                    responseHandler.handle(new HttpClientResponseError(500));
                })
                .end();
    }


    public interface VertxBuilder {
        ClientOptionsBuilder vertx(Vertx vertx);
    }

    public interface ClientOptionsBuilder {
        EtcdHostBuilder clientOptions(HttpClientOptions client);
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
        ServiceContextRootBuilder servicePort(int servicePort);
    }

    public interface ServiceContextRootBuilder {
        ServiceSecureBuilder serviceContextRoot(String contextRoot);
    }

    public interface ServiceSecureBuilder {
        NodeNameBuilder secure(boolean secure);
    }

    public interface NodeNameBuilder {
        EtcdRegistration nodeName(String nodeName);
    }

    public static VertxBuilder buildRegistration() {
        return vertx -> clientOptions -> etcdHost -> etcdPort -> ttl -> domainname -> servicename -> serviceHost -> servicePort -> contextRoot -> secure -> nodename -> new EtcdRegistration(vertx, clientOptions, etcdHost, etcdPort, ttl, domainname, servicename, serviceHost, servicePort, contextRoot, secure, nodename);
    }
}