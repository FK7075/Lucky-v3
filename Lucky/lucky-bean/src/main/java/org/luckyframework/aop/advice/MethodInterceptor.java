package org.luckyframework.aop.advice;

import java.lang.reflect.Method;

/**
 * 环绕增强通知
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 9:50
 */
public interface MethodInterceptor extends MethodAdvice {

    Object invoke(Object target, Method method,Object[] args);

}
