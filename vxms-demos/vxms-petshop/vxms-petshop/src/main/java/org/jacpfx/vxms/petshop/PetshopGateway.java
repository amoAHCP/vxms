package org.jacpfx.vxms.petshop;

import org.jacpfx.common.ServiceEndpoint;
import org.jacpfx.vertx.etcd.client.EtcdClient;
import org.jacpfx.vertx.rest.annotation.EndpointConfig;
import org.jacpfx.vertx.rest.response.RestHandler;
import org.jacpfx.vertx.services.VxmsEndpoint;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * Created by Andy Moncsek on 07.07.16.
 */
@ServiceEndpoint(port = 9090)  // can be overwritten by port=xxxx in config file
@EndpointConfig(CustomEndpointConfiguration.class)
@EtcdClient(domain = "petshop", host = "127.0.0.1", port = 4001, ttl = 10)
// can be overwritten by etcdport=xxxx, etcdhost, domain & ttl in config file
public class PetshopGateway extends VxmsEndpoint {


    @Path("/pet/:id")
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

    @Path("/order")
    @POST
    public void order(RestHandler handler) {

    }
}
