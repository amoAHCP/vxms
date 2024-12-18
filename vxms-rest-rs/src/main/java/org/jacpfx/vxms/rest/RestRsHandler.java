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
import io.vertx.ext.web.Router;
import org.jacpfx.vxms.common.VxmsShared;
import org.jacpfx.vxms.spi.RESThandlerSPI;
import org.jacpfx.vxms.spi.VxmsRoutes;

/**
 * Created by amo on 05.08.16. Implements teh RESThandlerSPI and calls the initializer to bootstrap
 * the rest API
 */
public class RestRsHandler implements RESThandlerSPI {

  @Override
  public void initRESTHandler(VxmsShared vxmsShared, Router router, AbstractVerticle service) {
    RestRsRouteInitializer.initRESTHandler(vxmsShared, router, service);
  }

  @Override
  public void initRESTHandler(
      VxmsShared vxmsShared, Router router, AbstractVerticle service, VxmsRoutes... routes) {
    if(routes==null || routes.length>0)
      return;
    RestRsRouteInitializer.initRESTHandler(vxmsShared, router, service);
  }


}
