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

package org.jacpfx.vxms.k8swrite.util;

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

    public static JsonObject defaultErrorResponse(String userMessage) {
        JsonObject message = new JsonObject();
        message.put("username", userMessage).
                put("firstName", userMessage).
                put("lastName", userMessage).
                put("address", userMessage);
        return message;
    }

    public static JsonObject mapToUser(JsonObject input, String id) {
        return new JsonObject().
                put("username", input.getString("username")).
                put("firstName", input.getString("firstName")).
                put("lastName", input.getString("lastName")).
                put("address", input.getString("address")).
                put("id", id);
    }
}
