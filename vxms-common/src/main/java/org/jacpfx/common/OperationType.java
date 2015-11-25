package org.jacpfx.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by amo on 04.11.14.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Deprecated
public @interface OperationType {
     Type value();
}
