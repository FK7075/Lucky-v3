package org.luckyframework.context.annotation;

import java.lang.annotation.*;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/25 0025 16:17
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(PropertySources.class)
public @interface PropertySource {

    String[] value();

    boolean ignoreResourceNotFound() default false;

    String encoding() default "UTF-8";
}
