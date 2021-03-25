package org.luckyframework.context.annotation;

import java.lang.annotation.*;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/25 0025 16:19
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertySources {
    PropertySource[] value();
}
