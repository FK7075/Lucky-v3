package org.luckyframework.context.annotation;

import org.luckyframework.beans.Ordered;

import java.lang.annotation.*;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/14 0014 15:56
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Documented
public @interface Order {

    /**
     * The order value.
     * <p>Default is {@link Ordered#LOWEST_PRECEDENCE}.
     * @see Ordered#getOrder()
     */
    int value() default Ordered.LOWEST_PRECEDENCE;
}
