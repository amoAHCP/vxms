package org.jacpfx.vertx.rest.response;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.jacpfx.common.ThrowableSupplier;
import org.jacpfx.vertx.rest.util.RESTExecutionHandler;
import org.jacpfx.vertx.websocket.encoder.Encoder;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 12.01.16.
 */
public class ExecuteRSString extends ExecuteRSBasicString{


    public ExecuteRSString(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async,  ThrowableSupplier<String> stringSupplier,  Encoder encoder, Consumer<Throwable> errorHandler,  Function<Throwable, String> errorHandlerString, int retryCount) {
        super(vertx, t, errorMethodHandler, context, headers, async, stringSupplier, encoder, errorHandler, errorHandlerString, retryCount);
    }

    @Override
    public void execute() {
          // TODO impl async
        Optional.ofNullable(stringSupplier).
                ifPresent(supplier -> {

                    this.vertx.executeBlocking(handler ->{




                    },false,value->{});




                    int retry = retryCount > 0 ? retryCount : 0;
                            String result = null;
                            while (retry >= 0) {
                                try {
                                    result = supplier.get();
                                    retry = -1;
                                } catch (Throwable e) {
                                    retry--;
                                    if (retry < 0) {
                                        result = RESTExecutionHandler.handleError(context.response(), result, errorHandler, errorHandlerString, e);
                                    } else {
                                        RESTExecutionHandler.handleError(errorHandler, e);
                                    }
                                }
                            }
                            if (!context.response().ended()) {
                                updateResponseHaders();
                                if (result != null) {
                                    context.response().end(result);
                                } else {
                                    context.response().end();
                                }
                            }

                        }
                );


    }

    protected void updateResponseHaders() {
        Optional.ofNullable(headers).ifPresent(h -> h.entrySet().stream().forEach(entry -> context.response().putHeader(entry.getKey(), entry.getValue())));
    }

}
