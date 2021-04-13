package org.luckyframework.aop.proxy;

import com.lucky.utils.base.Assert;
import com.lucky.utils.reflect.MethodUtils;
import org.luckyframework.aop.advice.Advice;
import org.luckyframework.aop.advisor.Advisor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/7 0007 14:17
 */
public class AopProxyUtils {

    public static Object applyAdvices(Object proxy, Object target, Method method,Object[] args,
                                      List<Advisor> matchAdvisor) throws Throwable{
        List<Advice> advices = getShouldApplyAdvices(target.getClass(),method,args,matchAdvisor);
        if(Assert.isEmptyCollection(advices)){
            return MethodUtils.invoke(target,method,args);
        }else{
            AopAdviceChainInvocation chain = new AopAdviceChainInvocation(proxy,target,method,args,advices);
            return chain.invoke();
        }
    }

    private static List<Advice> getShouldApplyAdvices(Class<?> targetClass, Method method,Object[] args,
                                                     List<Advisor> matchAdvisors){
        if (Assert.isEmptyCollection(matchAdvisors)) {
            return null;
        }
        List<Advice> advices = new ArrayList<>();
        for (Advisor ad : matchAdvisors) {
            if(ad.getPointcut().matchMethod(targetClass,method,args)){
                advices.add(ad.getAdvice());
            }
        }
        return advices;
    }

}
