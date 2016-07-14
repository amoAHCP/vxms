package org.jacpfx.vxms.petservice.services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.jacpfx.vxms.petservice.util.InitMongoDB;

/**
 * This is a plain Vetr.x service for Pet entities
 * Created by Andy Moncsek on 08.07.16.
 */
public class Petservice extends AbstractVerticle {
    private MongoClient mongoClient;
    public void start(Future<Void> startFuture) throws Exception {
        // deploy a new instance of PetServiceGateway,
        // each Petservice instance creates a Gateway instance (N.. in case you deploy many instances with - instances N)
        // alternatively you can move the Gateway to a separate project without changing the code of the gateway or the service...
        // than you need to remove the deployment here and start both Verticles in one cluster

        getVertx().deployVerticle(new PetserviceGateway());


        vertx.eventBus().consumer("/pet/:id", getPetById());
        vertx.eventBus().consumer("/pets/:category", getPetsByCategory());
        vertx.eventBus().consumer("/categories", getCategories());
        vertx.eventBus().consumer("/pets", getAllPets());
        //vertx.eventBus().consumer("/createPet", getAllPets());  // TODO put Pet
        //vertx.eventBus().consumer("/createCategory", getAllPets());  // TODO create category
        mongoClient = InitMongoDB.initMongoData(vertx, config());
        startFuture.complete();
    }

    protected Handler<Message<Object>> getAllPets() {
        return handler -> mongoClient.find("pets", new JsonObject(), lookup -> {
            // error handling
            if (lookup.failed()) {
                handler.fail(500, "lookup failed");
                return;
            } else if(lookup.result().isEmpty()) {
                handler.fail(404, "no pets found");
                return;
            }
            handler.reply(new JsonArray(lookup.result()).encode());
        });
    }

    protected Handler<Message<Object>> getCategories() {
        return handler -> mongoClient.find("category", new JsonObject(), lookup -> {
            // error handling
            if (lookup.failed()) {
                handler.fail(500, "lookup failed");
                return;
            } else if(lookup.result().isEmpty()) {
                handler.fail(404, "no category found");
                return;
            }
            handler.reply(new JsonArray(lookup.result()).encode());
        });
    }

    protected Handler<Message<Object>> getPetsByCategory() {
        return handler -> {
            final Object body = handler.body();
            final String category = body.toString();
        };
    }

    protected Handler<Message<Object>> getPetById() {
        return handler -> {
            final Object body = handler.body();
            final String id = body.toString();

        };
    }

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(Petservice.class.getName());
    }
}
