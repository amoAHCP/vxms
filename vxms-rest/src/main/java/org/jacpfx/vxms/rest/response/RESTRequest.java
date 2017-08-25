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

package org.jacpfx.vxms.rest.response;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import java.util.Set;

/**
 * Created by Andy Moncsek on 12.01.16.
 * This class allows easy access to Request values like Cookies, parameters and attributes.
 */
public class RESTRequest {

  private final RoutingContext context;

  /**
   * @param context the Vert.x routing context
   */
  public RESTRequest(RoutingContext context) {
    this.context = context;
  }

  /**
   * Returns the parameter value for the given parameter name
   *
   * @param paramName the http parameter name
   * @return the parameter value
   */
  public String param(String paramName) {
    return context.request().getParam(paramName);
  }

  /**
   * Returns the header value for requested name
   *
   * @param headerName the header name
   * @return the requested header value
   */
  public String header(String headerName) {
    return context.request().getHeader(headerName);
  }

  /**
   * Returns the form attribute for requested name
   *
   * @param attributeName the name of the attribute
   * @return the attribute requested
   */
  public String formAttribute(String attributeName) {
    return context.request().getFormAttribute(attributeName);
  }

  /**
   * Returns a set with uploaded files
   *
   * @return the set of files
   */
  public Set<FileUpload> fileUploads() {
    return context.fileUploads();
  }

  /**
   * Returns a set of cookies
   *
   * @return the set of cookies
   */
  public Set<Cookie> cookies() {
    return context.cookies();
  }

  /**
   * Returns a cookie by name
   *
   * @param name the cookie name
   * @return the cookie
   */
  public Cookie cookie(String name) {
    return context.getCookie(name);
  }

  /**
   * Returns the request body
   *
   * @return the request body
   */
  public Buffer body() {
    return context.getBody();
  }


}
