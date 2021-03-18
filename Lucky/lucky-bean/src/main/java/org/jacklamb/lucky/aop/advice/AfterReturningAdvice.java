package org.jacklamb.lucky.aop.advice;

import java.lang.reflect.Method;

/**
 * 正常执行后的后置增强通知
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 9:47
 */
public interface AfterReturningAdvice extends Advice {

    void afterReturning(Object target, Method method,Object[] args,Object returning);
}
