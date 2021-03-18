package org.jacklamb.lucky.aop.advice;

import java.lang.reflect.Method;

/**
 * 异常执行的后置增强
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 9:52
 */
public interface AfterThrowingAdvice extends Advice {

    void afterThrowing(Object target, Method[] method,Object[] args,Throwable e);
}
