package org.jacpfx.vertx.event.response;

import io.vertx.core.eventbus.Message;

/**
 * Created by Andy Moncsek on 12.01.16.
 * his class allows easy access to event-bus message to get the body and the reply address
 */
public class EventbusRequest {

    private final Message<Object> message;

    /**
     * init the EventbusBridgeRequest
     * @param message the event-bus message
     */
    public EventbusRequest(Message<Object> message) {
        this.message = message;
    }


    /**
     *  Returns the body of the message
     * @param <T> the type of payload
     * @return the payloasd
     */
    public <T> T body() {
        return (T) message.body();
    }

    /**
     * Returns the reply-address to reply to the incoming message
     * @return the reply address
     */
    public String replyAddress() {
        return message.replyAddress();
    }


}
