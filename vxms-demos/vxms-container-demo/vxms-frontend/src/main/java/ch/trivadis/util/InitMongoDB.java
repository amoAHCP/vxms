package ch.trivadis.util;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Andy Moncsek on 01.04.16.
 */
public class InitMongoDB {

    public static void initMongoData(Vertx vertx,JsonObject config) {
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
        loadData(mongo);
    }

    private static void loadData(MongoClient db) {
        db.find("users", new JsonObject(), lookup -> {
            // error handling
            if (lookup.failed()) {
                dropAndCreate(db);
                return;
            }

            if(lookup.result().isEmpty()){
                dropAndCreate(db);
            }else {
                System.out.println("users already exists");
            }

        });

    }

    private static void dropAndCreate(MongoClient db) {
        db.dropCollection("users", drop -> {
            if (drop.failed()) {
                throw new RuntimeException(drop.cause());
            }

            List<JsonObject> users = new LinkedList<>();

            users.add(new JsonObject()
                    .put("username", "pmlopes")
                    .put("firstName", "Paulo")
                    .put("lastName", "Lopes")
                    .put("address", "The Netherlands"));

            users.add(new JsonObject()
                    .put("username", "timfox")
                    .put("firstName", "Tim")
                    .put("lastName", "Fox")
                    .put("address", "The Moon"));

            for (JsonObject user : users) {
                db.insert("users", user, res -> {
                    System.out.println("inserted " + user.encode());
                });
            }
        });
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
