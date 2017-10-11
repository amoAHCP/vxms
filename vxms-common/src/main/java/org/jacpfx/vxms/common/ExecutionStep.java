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

import org.jacpfx.vxms.common.throwable.ThrowableFutureBiConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureConsumer;

public class ExecutionStep<V, T> {


  private final ThrowableFutureConsumer<T> chainconsumer;

  private final ThrowableFutureBiConsumer<T,V> step;

  public ExecutionStep(ThrowableFutureConsumer<T> chainconsumer) {
    this.chainconsumer = chainconsumer;
    this.step=null;
  }

  public ExecutionStep(ThrowableFutureBiConsumer<T,V> step) {
    this.step = step;
    this.chainconsumer = null;
  }

  public ThrowableFutureConsumer<T> getChainconsumer() {
    return chainconsumer;
  }

  public ThrowableFutureBiConsumer<T, V> getStep() {
    return step;
  }
}
