package org.luckyframework.context.annotation;

import org.luckyframework.beans.BeanScope;

import java.lang.annotation.*;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/19 0019 15:37
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scope {

    BeanScope value() default BeanScope.SINGLETON;
}
