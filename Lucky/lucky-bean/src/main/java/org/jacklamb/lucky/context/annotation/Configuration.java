package org.jacklamb.lucky.context.annotation;

import java.lang.annotation.*;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/22 上午1:32
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {

    @AliasFor(annotation = Component.class)
    String value() default "";

    boolean proxyBeanMethods() default true;
}
