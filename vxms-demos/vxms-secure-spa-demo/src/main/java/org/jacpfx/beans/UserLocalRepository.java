package org.jacpfx.beans;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Andy Moncsek on 22.04.16.
 */
@Component
public class UserLocalRepository {
    private static List<JsonObject> users = new CopyOnWriteArrayList<>();


    @PostConstruct
    public void init() {
        users.add(new JsonObject()
                .put("_id", "1")
                .put("username", "pmlopes")
                .put("firstName", "Paulo")
                .put("lastName", "Lopes")
                .put("address", "The Netherlands"));

        users.add(new JsonObject()
                .put("_id", "2")
                .put("username", "timfox")
                .put("firstName", "Tim")
                .put("lastName", "Fox")
                .put("address", "The Moon"));
    }


    public JsonArray getAll() {
        JsonArray result = new JsonArray();
        for (JsonObject o : users) {
            result.add(o);
        }
        return result;
    }

    public JsonObject getUserById(String id) {
        return users.stream().filter(u -> u.getString("_id").equals(id)).findFirst().orElse(new JsonObject()
                .put("_id", "345345")
                .put("username", "not found")
                .put("firstName", "not found")
                .put("lastName", "not found")
                .put("address", "not found"));
    }

    public JsonObject addUser(JsonObject user) {
        user.put("_id", UUID.randomUUID().toString());
        users.add(user);
        return user;
    }

    public JsonObject updateUser(JsonObject user) {
        users.stream().filter(u -> u.getString("_id").equals(user.getString("_id"))).findFirst().ifPresent(us -> users.remove(us));
        users.add(user);
        return user;
    }

    public void deleteUser(String id) {
        users.stream().filter(u -> u.getString("_id").equals(id)).findFirst().ifPresent(user -> users.remove(user));
    }
}
