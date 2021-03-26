package org.luckyframework.t1.condition;

import com.lucky.utils.type.AnnotatedTypeMetadata;
import org.luckyframework.context.annotation.Condition;
import org.luckyframework.context.annotation.ConditionContext;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/26 0026 15:31
 */
public class MacCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return context.getEnvironment().getProperty("os.name").toString().toUpperCase().contains("MAC");
    }
}
