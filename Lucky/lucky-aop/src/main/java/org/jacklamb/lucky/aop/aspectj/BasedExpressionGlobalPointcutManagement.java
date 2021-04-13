package org.jacklamb.lucky.aop.aspectj;

import org.luckyframework.aop.pointcut.Pointcut;

/**
 * 基于表达式的全局Pointcut管理器
 * @author fk
 * @version 1.0
 * @date 2021/4/13 0013 11:58
 */
public interface BasedExpressionGlobalPointcutManagement extends GlobalPointcutManagement {

    String CONNECTOR = "#";

    Pointcut getPointcutByExpression(String prefix,String expression);
}
