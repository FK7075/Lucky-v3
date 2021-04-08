package org.luckyframework.aop.advice;

import java.lang.reflect.Method;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/7 0007 11:30
 */
public interface MethodInterceptor extends Advice {

    Object invoke(Object target,Method method,Object[] args);

}
