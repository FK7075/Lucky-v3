package org.jacklamb.lucky.aop.advice;

import java.lang.reflect.Method;

/**
 * 后置增强通知
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 9:48
 */
public interface MethodAfterAdvice extends Advice {

    void after(Object target, Method method,Object[] args);
}
