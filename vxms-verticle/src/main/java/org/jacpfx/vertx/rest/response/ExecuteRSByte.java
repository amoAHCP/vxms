package org.jacpfx.vertx.rest.response;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
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
public class ExecuteRSByte extends ExecuteRSBasicByte {


    public ExecuteRSByte(Vertx vertx, Throwable t, Consumer<Throwable> errorMethodHandler, RoutingContext context, Map<String, String> headers, boolean async, ThrowableSupplier<byte[]> byteSupplier, Encoder encoder, Consumer<Throwable> errorHandler, Function<Throwable, byte[]> errorHandlerByte, int retryCount) {
        super(vertx, t, errorMethodHandler, context, headers, async, byteSupplier, encoder, errorHandler, errorHandlerByte, retryCount);
    }

    @Override
    public void execute() {

        // TODO add sync impl.
        Optional.ofNullable(byteSupplier).
                ifPresent(supplier -> {


                    this.vertx.executeBlocking(handler ->{




                    },false,value->{});




                            int retry = retryCount > 0 ? retryCount : 0;
                            byte[] result = new byte[0];
                            while (retry >= 0) {
                                try {
                                    result = supplier.get();

                                    retry = -1;
                                } catch (Throwable e) {
                                    retry--;
                                    if (retry < 0) {
                                        result = RESTExecutionHandler.handleError(context.response(), result, errorHandler, errorHandlerByte, errorMethodHandler, e);
                                    } else {
                                        RESTExecutionHandler.handleError(errorHandler, e);
                                    }
                                }
                            }
                            if (!context.response().ended()) {
                                updateResponseHaders();
                                context.response().end(Buffer.buffer(result));
                            }
                        }
                );


    }


}
