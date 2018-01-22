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

import io.vertx.core.Future;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Andy Moncsek
 */
public class ReflectionExecutionWrapper {

  private final String methodName;
  private final Object objectToInvoke;
  private final Object[] parameters;
  private final Future<Void> startFuture;

  public ReflectionExecutionWrapper(String methodName, Object objectToInvoke,
      Object[] parameters, Future<Void> startFuture) {
    this.methodName = methodName;
    this.objectToInvoke = objectToInvoke;
    this.parameters = parameters;
    this.startFuture = startFuture;
  }

  /**
   * Returns the Method name to invoke
   *
   * @return the method name
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * Returns the Object of invocation
   *
   * @return the Method to invoke
   */
  public Object getObjectToInvoke() {
    return objectToInvoke;
  }

  /**
   * Returns an array of parameters
   *
   * @return the method parameters
   */
  public Object[] getParameters() {
    return parameters;
  }

  /**
   * Checks if the requested method is existing
   *
   * @return true if the method exists
   */
  public boolean isPresent() {
    return CommonReflectionUtil
        .findMethodBySignature(methodName, parameters,
            objectToInvoke).isPresent();
  }

  /**
   * Returns a method when existing, otherwise throws an Exception
   *
   * @return the requested method
   */
  public Method getMethod() {
    return CommonReflectionUtil
        .findMethodBySignature(methodName, parameters,
            objectToInvoke).get();
  }

  /**
   * Invoke the method with it's signature
   */
  public void invoke() {
    final Method method = getMethod();
    final Object[] param = getParameters();
    try {
      if (param != null && param.length > 0) {
        method.invoke(getObjectToInvoke(), param);
      } else {
        method.invoke(getObjectToInvoke());
      }
    } catch (IllegalAccessException | InvocationTargetException e) {
      startFuture.fail(e);
    }
  }
}
