package org.jacpfx.vxms.petservice.services;

import io.vertx.core.Future;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by Andy Moncsek on 08.07.16.
 */
public class PetserviceGateway extends VxmsEndpoint {

    @Override
    public void postConstruct(final Future<Void> startFuture) {
        // for demo purposes
        //InitMongoDB.initMongoData(vertx, config());
    }

    @Path("/pet/:category/:id")
    @GET
    public void getPet(RestHandler handler) {

    }


    @Path("/pets/:category")
    @GET
    public void getPets(RestHandler handler) {

    }


    @Path("/categories")
    @GET
    public void getCategories(RestHandler handler) {

    }
}
