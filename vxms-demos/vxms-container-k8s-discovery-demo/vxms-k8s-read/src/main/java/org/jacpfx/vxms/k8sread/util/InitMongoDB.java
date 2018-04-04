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

package org.jacpfx.vxms.k8sread.util;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 * Created by Andy Moncsek on 01.04.16.
 */
public class InitMongoDB {

    public static MongoClient initMongoData(Vertx vertx, JsonObject config) {
        MongoClient mongo;
        // Create a mongo client using all defaults (connect to localhost and default port) using the database name "demo".
        String connectionUrl = connectionURL();
        boolean local = config.getBoolean("local", false);
        if (connectionUrl != null && !local) {
            String dbName = config.getString("dbname", "vxmsdemo");
            mongo = MongoClient.createShared(vertx, new JsonObject().put("connection_string", connectionUrl).put("db_name", dbName));
        } else {
            mongo = MongoClient.createShared(vertx, new JsonObject().put("db_name", "demo"));
        }
        // the load function just populates some data on the storage
        return mongo;
    }


    private static String connectionURL() {
        if (System.getenv("OPENSHIFT_MONGODB_DB_URL") != null) {
            return System.getenv("OPENSHIFT_MONGODB_DB_URL");
        } else if (System.getenv("MONGODB_PORT_27017_TCP_ADDR") != null) {
            String address = System.getenv("MONGODB_PORT_27017_TCP_ADDR");
            String port = System.getenv("MONGODB_PORT_27017_TCP_PORT");
            return "mongodb://" + address + ":" + port;

        }
        return "mongodb://mongo:27017";
    }


}
