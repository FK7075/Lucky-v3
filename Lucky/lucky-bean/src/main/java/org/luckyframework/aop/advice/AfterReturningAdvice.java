package org.luckyframework.aop.advice;


/**
 * 正常执行后的后置增强通知
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 9:47
 */
public interface AfterReturningAdvice extends MethodAdvice {

    void afterReturning(Object returning);
}
