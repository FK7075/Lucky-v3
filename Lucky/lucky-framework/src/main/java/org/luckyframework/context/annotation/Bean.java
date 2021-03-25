package org.luckyframework.context.annotation;

import com.lucky.utils.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/22 上午1:37
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {

    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    boolean autowireCandidate() default true;

    String initMethod() default "";

    String destroyMethod() default "";
}
