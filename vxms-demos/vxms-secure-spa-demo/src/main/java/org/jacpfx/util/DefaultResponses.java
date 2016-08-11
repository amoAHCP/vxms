package org.jacpfx.util;

import io.vertx.core.json.JsonObject;

/**
 * Created by Andy Moncsek on 01.04.16.
 */
public class DefaultResponses {

    public static JsonObject defaultErrorResponse() {
        JsonObject message = new JsonObject();
        message.put("username", "no connection").
                put("firstName", "no connection").
                put("lastName", "no connection").
                put("address", "no connection");
        return message;
    }


    public static  JsonObject mapToUser(JsonObject input, String id) {
        return  new JsonObject().
                put("username", input.getString("username")).
                put("firstName", input.getString("firstName")).
                put("lastName", input.getString("lastName")).
                put("address", input.getString("address")).
                put("_id", id);
    }
}
