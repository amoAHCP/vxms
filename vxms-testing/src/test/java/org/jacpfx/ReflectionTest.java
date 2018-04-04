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

package org.jacpfx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;
import org.jacpfx.vxms.common.util.CommonReflectionUtil;
import org.junit.Assert;
import org.junit.Test;

public class ReflectionTest {

  @Test
  public void findByNameTest() {
    Assert.assertTrue(
        CommonReflectionUtil.findMethodBySignature("postConstruct", new Object[] {}, this)
            .isPresent());
  }

  @Test
  public void findByNameTestParam1() {
    Assert.assertTrue(
        CommonReflectionUtil.findMethodBySignature(
                "postConstruct", new Object[] {new String("hello"), new Integer(1)}, this)
            .isPresent());
  }

  @Test
  public void findByNameTestParam2() {
    Assert.assertTrue(
        CommonReflectionUtil.findMethodBySignature(
                "postConstruct", new Object[] {new Integer(1), new String("hello")}, this)
            .isPresent());
  }

  @Test
  public void notfindByNameTest() {
    Assert.assertFalse(
        CommonReflectionUtil.findMethodBySignature(
                "postConstruct1", new Object[] {new Integer(1), new String("hello")}, this)
            .isPresent());
  }

  @Test
  public void executeFirst() {
    final Stream<Optional<Method>> methodsBySignature =
        Stream.of(
            CommonReflectionUtil.findMethodBySignature(
                "postConstruct", new Object[] {new Integer(1), new String("hello")}, this),
            CommonReflectionUtil.findMethodBySignature("postConstruct", new Object[] {}, this));
    final Optional<Optional<Method>> first =
        methodsBySignature.filter(m -> m.isPresent()).findFirst();
    first.ifPresent(
        methodOpt -> {
          methodOpt.ifPresent(
              method -> {
                try {
                  method.invoke(this, 1, "hello");
                  Assert.assertTrue(true);
                } catch (IllegalAccessException e) {
                  e.printStackTrace();
                  Assert.assertTrue(false);
                } catch (InvocationTargetException e) {
                  e.printStackTrace();
                  Assert.assertTrue(false);
                }
              });
        });
  }

  public void postConstruct() {
    System.out.println("postConstruct()");
  }

  public void postConstruct(String s, Integer i) {
    System.out.println("postConstruct(String s, Integer i)" + " " + s + " " + i);
  }

  public void postConstruct(Integer i, String s) {
    System.out.println("postConstruct(Integer i,String s)" + " " + i + " " + s);
  }
}
