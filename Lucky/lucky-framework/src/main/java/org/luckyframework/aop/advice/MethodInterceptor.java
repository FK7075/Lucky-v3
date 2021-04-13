package org.luckyframework.aop.advice;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/7 0007 11:30
 */
public interface MethodInterceptor extends Advice {

    Object invoke(ProceedingJoinPoint joinPoint) throws Throwable;

}
