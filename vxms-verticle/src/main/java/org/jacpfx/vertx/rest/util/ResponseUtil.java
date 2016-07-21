package org.jacpfx.vertx.rest.util;

import org.jacpfx.common.ThrowableSupplier;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Andy Moncsek on 21.07.16.
 */
public class ResponseUtil {

     public static  <T> T createResponse(int retry, T result, ThrowableSupplier<T> supplier, Consumer<Throwable> errorHandler, Function<Throwable, T> onFailureRespond, Consumer<Throwable> errorMethodHandler) {
         while (retry >= 0) {
             try {
                 result = supplier.get();
                 retry = -1;
             } catch (Throwable e) {
                 retry--;
                 if (retry < 0) {
                     result = RESTExecutionUtil.handleError(result, errorHandler, onFailureRespond, errorMethodHandler, e);
                 } else {
                     RESTExecutionUtil.handleError(errorHandler, e);
                 }
             }
         }

         return result;
     }
}
