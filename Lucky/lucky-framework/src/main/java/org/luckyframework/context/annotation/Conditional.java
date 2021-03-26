package org.luckyframework.context.annotation;

import java.lang.annotation.*;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/26 0026 14:32
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conditional {

    Class<? extends Condition>[] value();
}
