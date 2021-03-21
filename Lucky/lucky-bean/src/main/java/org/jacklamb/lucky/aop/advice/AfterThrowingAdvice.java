package org.jacklamb.lucky.aop.advice;


/**
 * 异常执行的后置增强
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 9:52
 */
public interface AfterThrowingAdvice extends MethodAdvice {

    void afterThrowing(Throwable e);
}
