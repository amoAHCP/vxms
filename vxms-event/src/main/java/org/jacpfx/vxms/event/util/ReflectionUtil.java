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

package org.jacpfx.vxms.event.util;

import java.lang.reflect.Method;
import java.util.function.Supplier;
import org.jacpfx.vxms.common.util.CommonReflectionUtil;

/** Created by Andy Moncsek on 25.11.15. Utility class for handling invocation of vxms methods */
public class ReflectionUtil {

  /**
   * Invoke a vxms event-bus method parameters
   *
   * @param method the method identifier
   * @param t the failure
   * @param handler the handler
   * @param <T> the type
   * @return the array of parameters to pass to method invokation
   */
  public static <T> Object[] invokeParameters(Method method, Throwable t, T handler) {
    final java.lang.reflect.Parameter[] parameters = method.getParameters();
    final Object[] parameterResult = new Object[parameters.length];
    int i = 0;
    for (java.lang.reflect.Parameter p : parameters) {
      if (handler != null && handler.getClass().isAssignableFrom(p.getType())) {
        parameterResult[i] = handler;
      }
      if (Throwable.class.isAssignableFrom(p.getType())) {
        parameterResult[i] = t;
      }
      i++;
    }
    return parameterResult;
  }

  /**
   * invoke a method with given parameters
   *
   * @param method the method
   * @param parameters the parameters
   * @param invokeTo the target
   * @throws Throwable the exception
   */
  public static void genericMethodInvocation(
      Method method, Supplier<Object[]> parameters, Object invokeTo) throws Throwable {
    CommonReflectionUtil.genericMethodInvocation(method, parameters, invokeTo);
  }
}
