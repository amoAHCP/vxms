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

package org.jacpfx.vxms.k8sread.service;

import io.vertx.core.Future;
import io.vertx.core.json.Json;
import org.jacpfx.vxms.k8sread.entity.Users;
import org.jacpfx.vxms.k8sread.repository.ReactiveUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class UserService {
  @Autowired
  private ReactiveUserRepository repository;

  public void findAllUsers(Future<String> future) {
    repository
        .findAll()
        .collectList()
        .doOnSuccess(value -> future.complete(Json.encode(value)))
        .doOnError(error -> future.fail(error))
        .subscribe();
  }

  public void findUser(String id, Future<String> future) {
    repository
        .findById(id)
        .doOnSuccess(value -> future.complete(Json.encode(value)))
        .doOnError(error -> future.fail(error))
        .subscribe();
  }

  public void initData(Future<Void> startFuture) {
    Flux<Users> people =
        Flux.just(
            new Users("1", "eoc", "Eric", "Foo", "Zh"),
            new Users("2", "fgdf", "Raymond", "Bar", "B"),
            new Users("3", "bdf", "Paul", "Baz", "x"));
    repository.findAll().collectList().doOnSuccess(result -> {
      if(result.isEmpty())repository.saveAll(people);
      startFuture.complete();
    }).subscribe();
  }
}
