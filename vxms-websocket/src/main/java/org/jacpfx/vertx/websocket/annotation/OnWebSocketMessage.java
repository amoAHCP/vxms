package org.jacpfx.vertx.websocket.annotation;

import java.lang.annotation.*;


@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnWebSocketMessage {
    String value();
}
