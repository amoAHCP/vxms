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


module vxms.spa.demo {
  requires vxms.core;
  requires vertx.core;
  requires vertx.auth.oauth2;
  requires vertx.auth.common;
  requires spring.context;
  requires vxms.rest;
  requires vertx.web;
  requires java.logging;
  requires java.management;
  requires java.ws.rs;
  requires javax.inject;
  requires java.xml.ws.annotation;
  requires io.netty.codec.http;
  requires jacpfx.vertx.spring;

  uses org.jacpfx.vxms.spi.RESThandlerSPI;

}