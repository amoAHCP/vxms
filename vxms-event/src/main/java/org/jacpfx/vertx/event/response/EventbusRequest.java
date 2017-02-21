package org.jacpfx.vertx.event.response;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

import java.util.Set;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class EventbusRequest {

    private final Message<Object> message;

    public EventbusRequest(Message<Object> message) {
        this.message = message;
    }


    public <T> T body() {
        return (T) message.body();
    }

    public String replyAddress() {
        return message.replyAddress();
    }


}
