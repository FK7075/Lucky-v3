package org.jacklamb.lucky.aop.pointcut;

import java.lang.reflect.Method;

/**
 * 切入点
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 14:25
 */
public interface Pointcut {

    /***
     * 类校验
     * @param targetClass 真实类的Class
     * @return
     */
    boolean matchClass(Class<?> targetClass);

    /**
     * 类和方法校验
     * @param targetClass 真实类的Class
     * @param method 真实类的Method
     * @return
     */
    boolean matchMethod(Class<?> targetClass, Method method);

}
