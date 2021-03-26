package org.luckyframework.context.annotation;

import com.lucky.utils.type.AnnotatedTypeMetadata;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/26 0026 14:33
 */
@FunctionalInterface
public interface Condition {

    boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata);

}
