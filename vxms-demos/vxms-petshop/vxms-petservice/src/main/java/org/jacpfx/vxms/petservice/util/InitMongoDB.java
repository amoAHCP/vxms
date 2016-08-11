package org.jacpfx.vxms.petservice.util;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Andy Moncsek on 13.07.16.
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
        loadData(mongo);

        return mongo;
    }

    private static void loadData(MongoClient db) {
        db.find("category", new JsonObject(), lookup -> {
            // error handling
            if (lookup.failed()) {
                dropAndCreate(db);
                return;
            }

            if (lookup.result().isEmpty()) {
                dropAndCreate(db);
            } else {
                System.out.println("categories already exists");
            }

        });

    }

    private static void dropAndCreate(MongoClient db) {


        db.dropCollection("pets", drop -> {
            if (drop.failed()) {
                throw new RuntimeException(drop.cause());
            }


            db.dropCollection("category", dropCat -> {
                if (dropCat.failed()) {
                    throw new RuntimeException(dropCat.cause());
                }


                createReptiles(db);

                createCats(db);

                createDogs(db);

                createHorses(db);


            });


        });


    }

    private static void createHorses(MongoClient db) {
        db.insert("category", new JsonObject().put("name", "horse").put("imageRef", "horse.png"), res -> {
            if (res.succeeded()) {
                db.find("category", new JsonObject().put("name", "horse"), cat -> {
                    if (cat.succeeded() && !cat.result().isEmpty()) {
                        JsonObject reptile = cat.result().get(0);
                        String catId = reptile.getString("_id");

                        List<JsonObject> pets = new LinkedList<>();

                        pets.add(new JsonObject()
                                .put("name", "Arabian")
                                .put("size", "3m")
                                .put("imgeRef", "afgan.png")
                                .put("amount", 5)
                                .put("category", "horse")
                                .put("categoryId", catId));


                        pets.add(new JsonObject()
                                .put("name", "Belgian")
                                .put("size", "2m")
                                .put("imgeRef", "akita.png")
                                .put("amount", 3)
                                .put("category", "horse")
                                .put("categoryId", catId));

                        for (JsonObject pet : pets) {
                            db.insert("pets", pet, resPet -> {
                                System.out.println("inserted " + resPet.succeeded());
                            });
                        }
                    }
                });
            }
        });
    }

    private static void createDogs(MongoClient db) {
        db.insert("category", new JsonObject().put("name", "dog").put("imageRef", "dog.png"), res -> {
            if (res.succeeded()) {
                db.find("category", new JsonObject().put("name", "dog"), cat -> {
                    if (cat.succeeded() && !cat.result().isEmpty()) {
                        JsonObject reptile = cat.result().get(0);
                        String catId = reptile.getString("_id");

                        List<JsonObject> pets = new LinkedList<>();

                        pets.add(new JsonObject()
                                .put("name", "Afgan Hound")
                                .put("size", "40cm")
                                .put("imgeRef", "afgan.png")
                                .put("amount", 32)
                                .put("category", "dog")
                                .put("categoryId", catId));

                        pets.add(new JsonObject()
                                .put("name", "Akita")
                                .put("size", "50cm")
                                .put("imgeRef", "akita.png")
                                .put("amount", 33)
                                .put("category", "dog")
                                .put("categoryId", catId));

                        for (JsonObject pet : pets) {
                            db.insert("pets", pet, resPet -> {
                                System.out.println("inserted " + resPet.succeeded());
                            });
                        }
                    }
                });
            }
        });
    }

    private static void createCats(MongoClient db) {
        db.insert("category", new JsonObject().put("name", "cat").put("imageRef", "cat.png"), res -> {
            if (res.succeeded()) {
                db.find("category", new JsonObject().put("name", "cat"), cat -> {
                    if (cat.succeeded() && !cat.result().isEmpty()) {
                        JsonObject reptile = cat.result().get(0);
                        String catId = reptile.getString("_id");

                        List<JsonObject> pets = new LinkedList<>();

                        pets.add(new JsonObject()
                                .put("name", "siam cat")
                                .put("size", "30cm")
                                .put("imgeRef", "siam.png")
                                .put("amount", 13)
                                .put("category", "cat")
                                .put("categoryId", catId));

                        pets.add(new JsonObject()
                                .put("name", "american curl")
                                .put("size", "20cm")
                                .put("imgeRef", "american.png")
                                .put("amount", 12)
                                .put("category", "cat")
                                .put("categoryId", catId));

                        pets.add(new JsonObject()
                                .put("name", "Burmese")
                                .put("size", "23cm")
                                .put("imgeRef", "burmese.png")
                                .put("amount", 22)
                                .put("category", "cat")
                                .put("categoryId", catId));
                        for (JsonObject pet : pets) {
                            db.insert("pets", pet, resPet -> {
                                System.out.println("inserted " + resPet.succeeded());
                            });
                        }
                    }
                });
            }
        });
    }

    private static void createReptiles(MongoClient db) {
        db.insert("category", new JsonObject().put("name", "reptile").put("imageRef", "reptile.png"), res -> {
            if (res.succeeded()) {
                db.find("category", new JsonObject().put("name", "reptile"), cat -> {
                    if (cat.succeeded() && !cat.result().isEmpty()) {
                        JsonObject reptile = cat.result().get(0);
                        String catId = reptile.getString("_id");

                        List<JsonObject> pets = new LinkedList<>();

                        pets.add(new JsonObject()
                                .put("name", "leguan")
                                .put("size", "1m")
                                .put("imgeRef", "leguan.png")
                                .put("amount", 20)
                                .put("category", "reptile")
                                .put("categoryId", catId));

                        pets.add(new JsonObject()
                                .put("name", "crocodile")
                                .put("size", "5m")
                                .put("imgeRef", "crocodile.png")
                                .put("amount", 10)
                                .put("category", "reptile")
                                .put("categoryId", catId));
                        for (JsonObject pet : pets) {
                            db.insert("pets", pet, resPet -> {
                                System.out.println("inserted " + resPet.succeeded());
                            });
                        }
                    }
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
