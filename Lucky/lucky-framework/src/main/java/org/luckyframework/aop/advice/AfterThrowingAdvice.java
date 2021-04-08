package org.luckyframework.aop.advice;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/7 0007 11:26
 */
public interface AfterThrowingAdvice extends Advice {

    void afterThrowing(Throwable e);

}
