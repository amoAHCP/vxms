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

package org.jacpfx.vxms.k8s.util;

import java.lang.reflect.Field;

/**
 * Created by amo on 13.04.17.
 */
public class FieldUtil {
  public static void setFieldValue(Object bean, Field serviceNameField, Object value) {
    serviceNameField.setAccessible(true);
    try {
      serviceNameField.set(bean, value);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
