package org.jacklamb.lucky.aop.advice;

import org.aspectj.lang.JoinPoint;

/**
 * 前置增强通知
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 9:49
 */
public interface MethodBeforeAdvice extends Advice {

    void before();
}
