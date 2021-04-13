package org.jacklamb.lucky.aop.aspectj.advice;

import com.lucky.utils.type.AnnotatedElementUtils;
import org.aspectj.lang.annotation.After;
import org.jacklamb.lucky.aop.aspectj.pointcut.AspectJExpressionPointcut;
import org.luckyframework.aop.advice.Advice;
import org.luckyframework.aop.advice.AfterAdvice;
import org.luckyframework.aop.advisor.Advisor;
import org.luckyframework.aop.pointcut.Pointcut;

import java.lang.reflect.Method;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/13 0013 11:34
 */
public abstract class AspectJAdvisor implements Advisor {

    private String aspectName;
    private Method aspectMethod;


    public AspectJAdvisor(String aspectName,Method aspectMethod){
        this.aspectName = aspectName;
        this.aspectMethod = aspectMethod;
    }

    private Advice doGetAdvice(){
        return null;
    }

    private AfterAdvice getAfterAdvice(After after){
        return null;
    }


}
