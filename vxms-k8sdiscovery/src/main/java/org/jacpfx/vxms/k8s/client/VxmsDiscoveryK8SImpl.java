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

package org.jacpfx.vxms.k8s.client;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.jacpfx.vxms.common.util.ConfigurationUtil;
import org.jacpfx.vxms.k8s.annotation.K8SDiscovery;
import org.jacpfx.vxms.k8s.util.StringUtil;
import org.jacpfx.vxms.k8s.util.TokenUtil;


public class VxmsDiscoveryK8SImpl {

  public void initDiscovery(AbstractVerticle service){
    final JsonObject config = service.config();
    if(!service.getClass().isAnnotationPresent(K8SDiscovery.class)) throw new IllegalArgumentException("no @K8SDiscovery annotation found");
    final K8SDiscovery annotation = service.getClass().getAnnotation(K8SDiscovery.class);
    final String user = ConfigurationUtil.getStringConfiguration(config,"user",annotation.user());
    final String password = ConfigurationUtil.getStringConfiguration(config,"password",annotation.password());
    final String api_token = ConfigurationUtil.getStringConfiguration(config,"api_token",annotation.api_token());
    final String master_url = ConfigurationUtil.getStringConfiguration(config,"master_url",annotation.master_url());
    final String namespace = ConfigurationUtil.getStringConfiguration(config,"namespace",annotation.namespace());
    final Config kubeConfig = new ConfigBuilder()
        .withMasterUrl(master_url)
        .withNamespace(namespace)
        .build();
    if(!StringUtil.isNullOrEmpty(api_token))
      kubeConfig.setOauthToken(api_token);
    if(!StringUtil.isNullOrEmpty(password))
      kubeConfig.setPassword(password);
    if(!StringUtil.isNullOrEmpty(user))
      kubeConfig.setUsername(user);
    // check oauthToken
    if(StringUtil.isNullOrEmpty(kubeConfig.getOauthToken()))kubeConfig.setOauthToken(TokenUtil.getAccountToken());
    // 1.) Check from K8SDiscovery Annotation
    // 1.1) read properties and from Annotation or from configuration
    // 2.) init KubernetesClient
    KubeDiscovery.resolveBeanAnnotations(service,kubeConfig);
  }

  public void initDiscovery(AbstractVerticle service,Config kubeConfig){
    final JsonObject config = service.config();
    if(!service.getClass().isAnnotationPresent(K8SDiscovery.class)) throw new IllegalArgumentException("no @K8SDiscovery annotation found");
    final K8SDiscovery annotation = service.getClass().getAnnotation(K8SDiscovery.class);
    final String user = ConfigurationUtil.getStringConfiguration(config,"user",annotation.user());
    final String password = ConfigurationUtil.getStringConfiguration(config,"password",annotation.password());
    final String api_token = ConfigurationUtil.getStringConfiguration(config,"api_token",annotation.api_token());
    final String master_url = ConfigurationUtil.getStringConfiguration(config,"master_url",annotation.master_url());
    final String namespace = ConfigurationUtil.getStringConfiguration(config,"namespace",annotation.namespace());
    if(StringUtil.isNullOrEmpty(kubeConfig.getUsername()))kubeConfig.setUsername(user);
    if(StringUtil.isNullOrEmpty(kubeConfig.getPassword()))kubeConfig.setPassword(password);
    if(StringUtil.isNullOrEmpty(kubeConfig.getOauthToken()))kubeConfig.setOauthToken(api_token);
    if(StringUtil.isNullOrEmpty(kubeConfig.getMasterUrl()))kubeConfig.setMasterUrl(master_url);
    if(StringUtil.isNullOrEmpty(kubeConfig.getNamespace()))kubeConfig.setNamespace(namespace);
    // check oauthToken
    if(StringUtil.isNullOrEmpty(kubeConfig.getOauthToken()))kubeConfig.setOauthToken(TokenUtil.getAccountToken());
    // 1.) Check from K8SDiscovery Annotation
    // 1.1) read properties and from Annotation or from configuration
    // 2.) init KubernetesClient
    KubeDiscovery.resolveBeanAnnotations(service,kubeConfig);
  }

}
