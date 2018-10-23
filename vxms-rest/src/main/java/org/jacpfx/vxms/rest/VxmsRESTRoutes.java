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

package org.jacpfx.vxms.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.jacpfx.vxms.rest.response.RestHandler;
import org.jacpfx.vxms.spi.VxmsRoutes;

public class VxmsRESTRoutes implements VxmsRoutes {

  protected final Map<String, RestHandlerConsumer> getMapping;
  protected final Map<String, RestHandlerConsumer> postMapping;
  protected final Map<String, RestHandlerConsumer> optionalMapping;
  protected final Map<String, RestHandlerConsumer> putMapping;
  protected final Map<String, RestHandlerConsumer> deleteMapping;

  public VxmsRESTRoutes(
      Map<String, RestHandlerConsumer> getMapping,
      Map<String, RestHandlerConsumer> postMapping,
      Map<String, RestHandlerConsumer> optionalMapping,
      Map<String, RestHandlerConsumer> putMapping,
      Map<String, RestHandlerConsumer> deleteMapping) {
    this.getMapping = getMapping;
    this.postMapping = postMapping;
    this.optionalMapping = optionalMapping;
    this.putMapping = putMapping;
    this.deleteMapping = deleteMapping;
  }

  public static VxmsRESTRoutes init() {
    return new VxmsRESTRoutes( Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
  }

  public VxmsRESTRoutes get(String path, RestHandlerConsumer methodReference) {
    Map<String, RestHandlerConsumer> tmp = new HashMap<>(getMapping);
    tmp.put(path, methodReference);
    return new VxmsRESTRoutes( tmp, postMapping, optionalMapping,
        putMapping, deleteMapping);
  }

  public VxmsRESTRoutes post(String path, RestHandlerConsumer methodReference) {
    Map<String, RestHandlerConsumer> tmp = new HashMap<>(postMapping);
    tmp.put(path, methodReference);
    return new VxmsRESTRoutes( getMapping, tmp, optionalMapping,
        putMapping, deleteMapping);
  }

  public VxmsRESTRoutes optional(String path, RestHandlerConsumer methodReference) {
    Map<String, RestHandlerConsumer> tmp = new HashMap<>(optionalMapping);
    tmp.put(path, methodReference);
    return new VxmsRESTRoutes( getMapping, postMapping, tmp,
        putMapping, deleteMapping);
  }

  public VxmsRESTRoutes put(String path, RestHandlerConsumer methodReference) {
    Map<String, RestHandlerConsumer> tmp = new HashMap<>(putMapping);
    tmp.put(path, methodReference);
    return new VxmsRESTRoutes( getMapping, postMapping, optionalMapping,
        tmp, deleteMapping);
  }

  public VxmsRESTRoutes delete(String path, RestHandlerConsumer methodReference) {
    Map<String, RestHandlerConsumer> tmp = new HashMap<>(deleteMapping);
    tmp.put(path, methodReference);
    return new VxmsRESTRoutes( getMapping, postMapping, optionalMapping,
        putMapping, tmp);
  }

  public Map<String, RestHandlerConsumer> getGetMapping() {
    return Collections.unmodifiableMap(getMapping);
  }

  public Map<String, RestHandlerConsumer> getPostMapping() {
    return Collections.unmodifiableMap(postMapping);
  }

  public Map<String, RestHandlerConsumer> getOptionalMapping() {
    return Collections.unmodifiableMap(optionalMapping);
  }

  public Map<String, RestHandlerConsumer> getPutMapping() {
    return Collections.unmodifiableMap(putMapping);
  }

  public Map<String, RestHandlerConsumer> getDeleteMapping() {
    return Collections.unmodifiableMap(deleteMapping);
  }

  public interface RestHandlerConsumer extends Consumer<RestHandler> {

  }

}
