package org.luckyframework.context.annotation;

import java.lang.annotation.*;

/**
 * 支持嵌套代理
 * @author fk
 * @version 1.0
 * @date 2021/4/15 0015 11:07
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SupportNestedProxy {
}
