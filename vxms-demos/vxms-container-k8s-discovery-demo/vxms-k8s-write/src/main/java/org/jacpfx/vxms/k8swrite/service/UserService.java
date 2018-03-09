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

package org.jacpfx.vxms.k8swrite.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class UserService {
  private final MongoClient mongo;

  public UserService(MongoClient mongo) {
    this.mongo = mongo;
  }

  public void handleInsert(final JsonObject newUser, Future<String> future) {
    mongo.findOne("users", new JsonObject().put("username", newUser.getString("username")), null,
        lookup -> {
          // error handling
          if (lookup.failed()) {
            future.fail(lookup.cause());
            return;
          }

          JsonObject user = lookup.result();
          if (user != null) {
            // already exists
            future.fail("user already exists");
          } else {
            mongo.insert("users", newUser, insert -> {
              // error handling
              if (insert.failed()) {
                future.fail("lookup failed");
                return;
              }
              // add the generated id to the user object
              newUser.put("_id", insert.result());
              future.complete(newUser.encode());
            });
          }
        });
  }

  public void handleUpdate(final JsonObject user, Future<String> future) {
    final String id = user.getString("id");
    mongo.findOne("users", new JsonObject().put("_id", id), null, lookup -> {
      // error handling
      if (lookup.failed()) {
        future.fail(lookup.cause());
        return;
      }

      JsonObject existingUser = lookup.result();
      if (existingUser == null) {
        // does not exist
        future.fail("user does not exists");
      } else {
        // update the user properties
        existingUser.put("username", user.getString("username"));
        existingUser.put("firstName", user.getString("firstName"));
        existingUser.put("lastName", user.getString("lastName"));
        existingUser.put("address", user.getString("address"));

        mongo.replace("users", new JsonObject().put("_id", id), existingUser, replace -> {
          // error handling
          if (replace.failed()) {
            future.fail(lookup.cause());
            return;
          }
          future.complete(user.encode());
        });
      }
    });
  }


  public void handleDelete(String id, Future<String> future) {
    mongo.findOne("users", new JsonObject().put("_id", id), null, lookup -> {
      // error handling
      if (lookup.failed()) {
        future.fail(lookup.cause());
        return;
      }
      JsonObject user = lookup.result();
      if (user == null) {
        // does not exist
        future.fail("user does not exists");
      } else {
        mongo.remove("users", new JsonObject().put("_id", id), remove -> {
          // error handling
          if (remove.failed()) {
            future.fail("lookup failed");
            return;
          }
          future.complete("end");
        });
      }
    });
  }
}
