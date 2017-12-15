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

package org.jacpfx.vxms.k8s.util;


import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by amo on 06.04.17.
 */
@Deprecated
public class KubeClientBuilder {

  private static final String IO_SERVICEACCOUNT_TOKEN = "/var/run/secrets/kubernetes.io/serviceaccount/token";
  public static final String DEFAULT_MASTER_URL = "https://kubernetes.default.svc";
  public static final String DEFAULT_NAMESPACE = "default";
  public static final String KUBERNETES_NAMESPACE_ENV = "KUBERNETES_NAMESPACE";
  private static Logger log = Logger.getLogger(KubeClientBuilder.class.getName());

  public static KubernetesClient buildKubernetesClient(
      final String user,
      final String pwd,
      final String apiToken,
      final String kubernetesMaster,
      final String namespace) {

    final String masterUrl = getMasterURL(kubernetesMaster);
    final String defaultNamespace = getNamespace(namespace);

    String oauthToken = apiToken;
    if (StringUtil.isNullOrEmpty(user)) {
      if (StringUtil.isNullOrEmpty(oauthToken)) {
        log.info("find API token in POD");
        oauthToken = getAccountToken();
      }
      if (StringUtil.isNullOrEmpty(oauthToken)) {
        log.log(Level.WARNING, "no token found and no user provided, use default client");
        return new DefaultKubernetesClient();
      }
    }

    log.info("found API token: " + oauthToken);
    log.info("found user: " + user);
    log.info("found namespace: " + defaultNamespace);
    final Config config = createConfiguration(user, pwd, masterUrl, oauthToken, defaultNamespace);
    return createClient(config);
  }

  private static String getMasterURL(String kubernetesMaster) {
    return StringUtil.isNullOrEmpty(kubernetesMaster) ? DEFAULT_MASTER_URL : kubernetesMaster;
  }

  private static String getNamespace(String namespace) {
    if (StringUtil.isNullOrEmpty(namespace)) {
      namespace = System.getenv(KUBERNETES_NAMESPACE_ENV);
    }
    return StringUtil.isNullOrEmpty(namespace) ? DEFAULT_NAMESPACE : namespace;
  }

  private static Config createConfiguration(String user, String pwd, String kubernetesMaster,
      String oauthToken, String namespace) {
    ConfigBuilder configBuilder = new ConfigBuilder().
        withMasterUrl(kubernetesMaster).
        withNamespace(namespace);
    if (StringUtil.isNullOrEmpty(user)) {
      configBuilder = updateOauthToken(oauthToken, configBuilder);
    }
    configBuilder = updateUser(user, configBuilder);
    configBuilder = updatePwd(pwd, configBuilder);
    return configBuilder.build();
  }

  private static KubernetesClient createClient(Config config) {
    final DefaultKubernetesClient client = new DefaultKubernetesClient(config);
    /**if (client.isAdaptable(OpenShiftClient.class)) { // TODO check why this is throwing an unauthorized access exception
      OpenShiftClient oClient = client.adapt(OpenShiftClient.class);
      log.info("use OPENSHIFT");
      return oClient;
    }**/
    return client;
  }

  private static ConfigBuilder updatePwd(String pwd, ConfigBuilder configBuilder) {
    if (!StringUtil.isNullOrEmpty(pwd)) {
      configBuilder = configBuilder.withPassword(pwd);
      log.info("init password");
    }
    return configBuilder;
  }

  private static ConfigBuilder updateUser(String user, ConfigBuilder configBuilder) {
    if (!StringUtil.isNullOrEmpty(user)) {
      configBuilder = configBuilder.withUsername(user);
      log.info("init user");
    }
    return configBuilder;
  }

  private static ConfigBuilder updateOauthToken(String oauthToken, ConfigBuilder configBuilder) {
    if (!StringUtil.isNullOrEmpty(oauthToken)) {
      configBuilder = configBuilder.withOauthToken(oauthToken);
      log.info("init token");
    }
    return configBuilder;
  }

  private static String getAccountToken() {
    try {
      final Path path = Paths.get(IO_SERVICEACCOUNT_TOKEN);
      if (!path.toFile().exists()) {
        log.log(Level.WARNING, "no token found");
        return null;
      }
      return new String(Files.readAllBytes(path));

    } catch (IOException e) {
      throw new RuntimeException("Could not get token file", e);
    }
  }

}
