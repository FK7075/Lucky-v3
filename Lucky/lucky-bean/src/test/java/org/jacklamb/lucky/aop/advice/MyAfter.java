package org.jacklamb.lucky.aop.advice;

import java.lang.reflect.Method;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/19 0019 11:35
 */
public class MyAfter implements MethodAfterAdvice{

    @Override
    public void after() {
        System.out.println("After......");
    }
}
