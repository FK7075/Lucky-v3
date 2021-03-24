package org.luckyframework.context.annotation;

import com.lucky.utils.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/19 0019 15:32
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {

    @AliasFor(annotation = Component.class)
    String value() default "";
}
