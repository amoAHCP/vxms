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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import java.util.function.Consumer;
import org.jacpfx.vxms.common.throwable.ThrowableFunction;
import org.jacpfx.vxms.common.throwable.ThrowableFutureBiConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableFutureConsumer;
import org.jacpfx.vxms.common.throwable.ThrowableSupplier;

public class BlockingExecutionStep<V, T> {


  private final ThrowableSupplier<T> chainsupplier;

  private final ThrowableFunction<T, V> step;

  public BlockingExecutionStep(ThrowableSupplier<T> chainsupplier) {
    this.chainsupplier = chainsupplier;
    this.step = null;
  }

  public BlockingExecutionStep(ThrowableFunction<T, V> step) {
    this.step = step;
    this.chainsupplier = null;
  }

  public ThrowableSupplier<T> getChainsupplier() {
    return chainsupplier;
  }

  public ThrowableFunction<T, V> getStep() {
    return step;
  }

  public Handler<AsyncResult<ExecutionResult<T>>> getAsyncResultHandler(
      Consumer<ExecutionResult> resultConsumer, Runnable errorHandling) {
    return value -> {
      if (!value.failed()) {
        resultConsumer.accept(value.result());
      } else {
        errorHandling.run();
      }

    };
  }
}
