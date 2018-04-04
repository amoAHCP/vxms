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

/** Created by Andy Moncsek on 18.12.15. */
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.function.IntBinaryOperator;

public class TestMethodPerf {

  private static final int ITERATIONS = 50_000_000;
  private static final int WARM_UP = 10;

  public static void main(String... args) throws Throwable {
    // hold result to prevent too much optimizations
    final int[] dummy = new int[4];

    Method reflected = TestMethodPerf.class.getDeclaredMethod("myMethod", int.class, int.class);
    final MethodHandles.Lookup lookup = MethodHandles.lookup();
    reflected.setAccessible(true);
    MethodHandle mh = lookup.unreflect(reflected);
    IntBinaryOperator lambda =
        (IntBinaryOperator)
            LambdaMetafactory.metafactory(
                    lookup,
                    "applyAsInt",
                    MethodType.methodType(IntBinaryOperator.class),
                    mh.type(),
                    mh,
                    mh.type())
                .getTarget()
                .invokeExact();

    for (int i = 0; i < WARM_UP; i++) {
      dummy[0] += testDirect(dummy[0]);
      dummy[1] += testLambda(dummy[1], lambda);
      dummy[2] += testMH(dummy[1], mh);
      dummy[3] += testReflection(dummy[2], reflected);
    }
    long t0 = System.nanoTime();
    dummy[0] += testDirect(dummy[0]);
    long t1 = System.nanoTime();
    dummy[1] += testLambda(dummy[1], lambda);
    long t2 = System.nanoTime();
    dummy[2] += testMH(dummy[1], mh);
    long t3 = System.nanoTime();
    dummy[3] += testReflection(dummy[2], reflected);
    long t4 = System.nanoTime();
    System.out.printf(
        "direct: %.2fs, lambda: %.2fs, mh: %.2fs, reflection: %.2fs%n",
        (t1 - t0) * 1e-9, (t2 - t1) * 1e-9, (t3 - t2) * 1e-9, (t4 - t3) * 1e-9);

    // do something with the results
    if (dummy[0] != dummy[1] || dummy[0] != dummy[2] || dummy[0] != dummy[3]) {
      throw new AssertionError();
    }
  }

  private static int testMH(int v, MethodHandle mh) throws Throwable {
    for (int i = 0; i < ITERATIONS; i++) {
      v += (int) mh.invokeExact(1000, v);
    }
    return v;
  }

  private static int testReflection(int v, Method mh) throws Throwable {
    for (int i = 0; i < ITERATIONS; i++) {
      v += (int) mh.invoke(null, 1000, v);
    }
    return v;
  }

  private static int testDirect(int v) {
    for (int i = 0; i < ITERATIONS; i++) {
      v += myMethod(1000, v);
    }
    return v;
  }

  private static int testLambda(int v, IntBinaryOperator accessor) {
    for (int i = 0; i < ITERATIONS; i++) {
      v += accessor.applyAsInt(1000, v);
    }
    return v;
  }

  private static int myMethod(int a, int b) {
    return a < b ? a : b;
  }
}
