/*
 * Copyright [2017] [Andy Moncsek]
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

package org.jacpfx.vertx.rest.util;

import io.vertx.ext.web.RoutingContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import org.jacpfx.common.exceptions.EndpointExecutionException;
import org.jacpfx.vertx.rest.response.RestHandler;

/**
 * Created by Andy Moncsek on 25.11.15.
 * Utility class for handling invocation of vxms methods
 */
public class ReflectionUtil {


  /**
   * Invoke a vxms rest method parameters
   *
   * @param context the vertx routing context
   * @param method the method to invoke
   * @param failure the exception to pass
   * @param handler the rest handler instamce
   * @return the array of parameters to pass to method invokation
   */
  public static Object[] invokeRESTParameters(RoutingContext context, Method method,
      Throwable failure, RestHandler handler) {
    method.setAccessible(true);
    final java.lang.reflect.Parameter[] parameters = method.getParameters();
    final Object[] parameterResult = new Object[parameters.length];
    int i = 0;
    for (java.lang.reflect.Parameter p : parameters) {
      if (RestHandler.class.equals(p.getType())) {
        parameterResult[i] = handler;
      } else if (RoutingContext.class.equals(p.getType())) {
        parameterResult[i] = context;
      }
      if (Throwable.class.isAssignableFrom(p.getType())) {
        parameterResult[i] = failure;
      }
      i++;
    }
    return parameterResult;
  }

  /**
   * Invokes a method with passed parameters
   *
   * @param method the method to invoke
   * @param parameters the parameters to pass
   * @param invokeTo the invokation target
   * @throws Throwable the invocation exception
   */
  public static void genericMethodInvocation(Method method, Supplier<Object[]> parameters,
      Object invokeTo) throws Throwable {
    try {
      method.invoke(invokeTo, parameters.get());
    } catch (IllegalAccessException e) {
      e.printStackTrace();

    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof EndpointExecutionException) {
        throw e.getCause().getCause();
      }
      throw e.getTargetException();
    } catch (Exception e) {
      throw e;
    }
  }


}
