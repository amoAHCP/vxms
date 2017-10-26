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

package org.jacpfx.vxms.common;

import io.vertx.core.Vertx;
import org.jacpfx.vxms.common.concurrent.LocalData;

/**
 * Created by amo on 23.03.17.
 * Thjis class contains shared structures needed in all modules
 */
public class VxmsShared {

  private final Vertx vertx;

  private final LocalData localData;

  public VxmsShared(Vertx vertx, LocalData localData) {
    this.vertx = vertx;
    this.localData = localData;
  }

  /**
   * Returns the Vert.x instance
   *
   * @return the {@link Vertx} instance
   */
  public Vertx getVertx() {
    return vertx;
  }

  /**
   * Returns the local data instance
   *
   * @return the {@link LocalData} instance
   */
  public LocalData getLocalData() {
    return localData;
  }
}
