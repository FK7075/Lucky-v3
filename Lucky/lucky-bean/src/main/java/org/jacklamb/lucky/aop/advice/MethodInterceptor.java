package org.jacklamb.lucky.aop.advice;

import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

/**
 * 环绕增强通知
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 9:50
 */
public interface MethodInterceptor extends Advice {

    Object invoke(Object target, Method method,Object[] args);

}
