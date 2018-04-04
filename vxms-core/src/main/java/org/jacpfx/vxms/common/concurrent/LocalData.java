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

package org.jacpfx.vxms.common.concurrent;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.Arguments;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.impl.AsynchronousCounter;
import io.vertx.core.shareddata.impl.AsynchronousLock;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by amo on 23.03.17. Local Data implementation to get lock and counters without involving
 * cluster manager when running in clustered mode
 */
@SuppressWarnings("unchecked")
public class LocalData {

  private final ConcurrentMap<String, Counter> localCounters = new ConcurrentHashMap();
  private final ConcurrentMap<String, AsynchronousLock> localLocks = new ConcurrentHashMap();
  private final Vertx vertx;

  public LocalData(Vertx vertx) {
    this.vertx = vertx;
  }


  /**
   * Get a local counter. The counter will be passed to the handler.
   *
   * @param name the name of the counter.
   * @param resultHandler the handler
   */
  public void getCounter(String name, Handler<AsyncResult<Counter>> resultHandler) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(resultHandler, "resultHandler");
    Counter counter = this.localCounters
        .computeIfAbsent(name, (n) -> new AsynchronousCounter((VertxInternal) this.vertx));
    Context context = this.vertx.getOrCreateContext();
    context.runOnContext((v) -> resultHandler.handle(Future.succeededFuture(counter)));
  }

  /**
   * Get a local lock with the specified name with specifying a timeout. The lock will be passed to
   * the handler when it is available.  If the lock is not obtained within the timeout a failure
   * will be sent to the handler
   *
   * @param name the name of the lock
   * @param timeout the timeout in ms
   * @param resultHandler the handler
   */
  public void getLockWithTimeout(String name, long timeout,
      Handler<AsyncResult<Lock>> resultHandler) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(resultHandler, "resultHandler");
    Arguments.require(timeout >= 0L, "timeout must be >= 0");
    AsynchronousLock lock = this.localLocks
        .computeIfAbsent(name, (n) -> new AsynchronousLock(this.vertx));
    lock.acquire(timeout, resultHandler);

  }

}
