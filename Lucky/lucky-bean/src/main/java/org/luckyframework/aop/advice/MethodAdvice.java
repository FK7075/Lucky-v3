package org.luckyframework.aop.advice;

import org.aspectj.lang.JoinPoint;

import java.lang.reflect.Method;

/**
 * 基于方法的增强
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/19 下午11:15
 */
public interface MethodAdvice extends Advice {

    default void setAspectMethod(Method method){

    }

    default void setAspectArgs(Object[] args){

    }

    default void setJoinPoint(JoinPoint joinPoint){

    }

    default void setReturning(Object returning){

    }

    default void setThrowing(Throwable throwable){

    }

}
