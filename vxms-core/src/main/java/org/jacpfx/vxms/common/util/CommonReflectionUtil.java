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

package org.jacpfx.vxms.common.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.jacpfx.vxms.common.exceptions.EndpointExecutionException;

public class CommonReflectionUtil {

  static final Logger log = Logger.getLogger(CommonReflectionUtil.class.getName());

  /**
   * invoke a method with given parameters
   *
   * @param method the method
   * @param parameters the parameters
   * @param invokeTo the target
   * @throws Throwable the exception
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

  /**
   * Finds a speciffic method of the provided Objects by parameters and method name
   * @param methodName the method name to look for
   * @param parameters the parameters of the method signature
   * @param invokeTo the Object
   * @return the Optional of the Method to look for
   */
  public static Optional<Method> findMethodBySignature(String methodName, Object[] parameters,
      Object invokeTo) {

    final Optional<Method> first = Stream.of(invokeTo.getClass().getMethods()).
        filter(method -> method.getName().equals(methodName)).
        filter(m -> {
          final Class<?>[] parameterTypes = m.getParameterTypes();
          if (parameters.length == 0 && parameterTypes.length == 0) {
            return true;
          } else if (parameters.length == parameterTypes.length) {
            for (int i = 0; i < parameterTypes.length; i++) {
              if (!parameterTypes[i].isAssignableFrom(parameters[i].getClass())) {
                return false;
              }
            }
            return true;
          } else {
            return false;
          }
        }).findFirst();

    if (first.isPresent()) {
      return first;
    } else {
      log.log(Level.FINE, String
          .format("method %s was not found in class %s with parameters %s", methodName,
              invokeTo.getClass(),
              Arrays.toString(Stream.of(parameters).map(Object::getClass).toArray()))
          );
      return Optional.empty();
    }

  }

}
