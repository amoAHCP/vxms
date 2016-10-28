package org.jacpfx.common.util;


import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jacpfx.common.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by amo on 05.11.14.
 */
public class JSONTool {

    public static JsonObject createOperationObject(Operation op) {
        final JsonObject result = new JsonObject().
                put("name", op.getName()).
                put("description", op.getDescription()).
                put("url", op.getUrl()).
                put("type", op.getType()).
                put("getServiceName",op.getServiceName()).
                put("connectionHost", op.getConnectionHost()).
                put("connectionPort",op.getConnectionPort());
        if(op.getProduces()!=null) {
            final JsonArray types = new JsonArray();
            Stream.of(op.getProduces()).map(m -> new JsonObject().put("produces", m)).forEach(jso -> types.add(jso));
            result.put("produces", types);
        }
        if(op.getConsumes()!=null) {
            final JsonArray types = new JsonArray();
            Stream.of(op.getConsumes()).map(m -> new JsonObject().put("consumes", m)).forEach(jso -> types.add(jso));
            result.put("consumes", types);
        }

        if(op.getParameter()!=null) {
            final JsonArray params = new JsonArray();
            Stream.of(op.getParameter()).map(m -> new JsonObject().put("param", m)).forEach(jso -> params.add(jso));
            result.put("param", params);
        }

        return result;


    }

    public static List<JsonObject> getObjectListFromArray(final JsonArray jsonarray) {
        List<JsonObject> l = new ArrayList<>();
        if(jsonarray==null) return l;
        for(int i=0; i<jsonarray.size();i++) {
            l.add(jsonarray.getJsonObject(i));
        }
        return l;
    }
}
