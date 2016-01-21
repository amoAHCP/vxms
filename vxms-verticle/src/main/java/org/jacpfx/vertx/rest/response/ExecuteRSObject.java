package org.jacpfx.vertx.rest.response;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.util.RESTExecutionHandler;
import org.jacpfx.vertx.websocket.encoder.Encoder;
import org.jacpfx.vertx.websocket.util.WebSocketExecutionUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSObject extends ExecuteRSBasicObject{



    public ExecuteRSObject(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async, ThrowableSupplier<Serializable> objectSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, Serializable> errorHandlerObject, int retryCount) {
        super(vertx, t, errorMethodHandler, context, headers, async, objectSupplier, encoder, errorHandler, errorHandlerObject, retryCount);
    }


    @Override
    public void execute() {
        // TODO implement async
        Optional.ofNullable(objectSupplier).
                ifPresent(supplier -> {

                    this.vertx.executeBlocking(handler ->{




                    },false,value->{});




                    int retry = retryCount > 0 ? retryCount : 0;
                            Serializable result = "";
                            while (retry >= 0) {
                                try {
                                    result = supplier.get();
                                    retry = -1;
                                } catch (Throwable e) {
                                    retry--;
                                    if (retry < 0) {
                                        result = RESTExecutionHandler.handleError(context.response(), result, errorHandler, errorHandlerObject, e);
                                    } else {
                                        RESTExecutionHandler.handleError(errorHandler, e);
                                    }
                                }
                            }
                            if (!context.response().ended()) {
                                updateResponseHaders();
                                WebSocketExecutionUtil.encode(result, encoder).ifPresent(value -> RESTExecutionHandler.sendObjectResult(value, context.response()));
                            }

                        }
                );
    }


}
