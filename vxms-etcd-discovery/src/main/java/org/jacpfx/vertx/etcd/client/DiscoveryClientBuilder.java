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

package org.jacpfx.vertx.etcd.client;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import or.jacpfx.spi.DiscoveryClientSpi;

import java.net.URI;
import java.util.MissingResourceException;
import java.util.Objects;

/**
 * Created by Andy Moncsek on 23.06.16.
 */
public class DiscoveryClientBuilder implements DiscoveryClientSpi<DiscoveryClientEtcd> {
    private static final String ETCD_BASE_PATH = "/v2/keys/";
    private static final String HTTPS = "https://";
    private static final String HTTP = "http://";

    @Override
    public DiscoveryClientEtcd getClient(AbstractVerticle verticleInstance) {
        if (verticleInstance.getClass().isAnnotationPresent(EtcdClient.class)) {
            final EtcdClient client = verticleInstance.getClass().getAnnotation(EtcdClient.class);
            final CustomConnectionOptions connection = getConnectionOptions(client);
            final HttpClientOptions clientOptions = connection.getClientOptions(verticleInstance.config());
            final int etcdPort = verticleInstance.config().getInteger("etcdport", client.port());
            final String domain = verticleInstance.config().getString("domain", client.domain());
            final String etcdHost = verticleInstance.config().getString("etcdhost", client.host());
            final boolean secure = verticleInstance.config().getBoolean("etcd-secure", clientOptions.isSsl());
            final String prefix = secure ? HTTPS : HTTP;
            final URI fetchAll = URI.create(prefix + etcdHost + ":" + client.port() + ETCD_BASE_PATH + domain + "/?recursive=true");
            return new DiscoveryClientEtcd(Vertx.vertx(), clientOptions, client.domain(), fetchAll, etcdHost, etcdPort);
        } else {
            throw new MissingResourceException("missing @EtcdClient annotation", verticleInstance.getClass().getName(), "");
        }
    }

    private CustomConnectionOptions getConnectionOptions(EtcdClient client) {
        try {
            return client.options().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return new DefaultConnectionOptions();
    }

    @Override
    // TODO check how to handle custom HttpClient options
    public DiscoveryClientEtcd getClient(Vertx vertx, HttpClientOptions clientOptions, JsonObject config) {
        Objects.nonNull(vertx);
        Objects.nonNull(config);
        final String host = config.getString("etcd-host", null);
        final String domain = config.getString("etcd-domain", null);
        final String port = config.getString("etcd-port", null);
        Objects.nonNull(host);
        Objects.nonNull(domain);
        Objects.nonNull(port);
        final boolean secure = config.getBoolean("etcd-secure", clientOptions.isSsl());
        final String prefix = secure ? HTTPS : HTTP;
        final URI fetchAll = URI.create(prefix + host + ":" + port + ETCD_BASE_PATH + domain + "/?recursive=true");
        return new DiscoveryClientEtcd(vertx, clientOptions, domain, fetchAll, host, Integer.valueOf(port));

    }
}
