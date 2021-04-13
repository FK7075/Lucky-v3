package org.luckyframework.aop.advisor;

import org.luckyframework.aop.advice.Advice;
import org.luckyframework.aop.advice.MethodInterceptor;
import org.luckyframework.aop.pointcut.Pointcut;

/**
 * 编程式的切面
 * @author fk
 * @version 1.0
 * @date 2021/4/13 0013 15:19
 */
public interface ProgrammaticAdvisor extends Advisor, Pointcut, MethodInterceptor {

    @Override
    default Advice getAdvice() {
        return this;
    }

    @Override
    default void setAdvice(Advice advice) {
        //不做任何事情
    }

    @Override
    default Pointcut getPointcut() {
        return this;
    }

    @Override
    default void setPointcut(Pointcut pointcut) {
        //不做任何事情
    }
}
