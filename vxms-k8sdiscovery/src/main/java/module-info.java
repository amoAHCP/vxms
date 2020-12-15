/*
 * Copyright [2018] [Andy Moncsek]
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


module vxms.k8sdiscovery {
  requires vxms.core;
  requires io.vertx.core;
  requires io.vertx.web;
  requires io.netty.codec;
  requires io.netty.codec.http;
  requires java.logging;
  requires java.management;
  requires kubernetes.client;
  requires kubernetes.model;
  requires fabric8.annotations;

  exports org.jacpfx.vxms.k8s.annotation;

  provides org.jacpfx.vxms.spi.ServiceDiscoverySPI with org.jacpfx.vxms.k8s.client.VxmsDiscoveryK8SImpl;


}