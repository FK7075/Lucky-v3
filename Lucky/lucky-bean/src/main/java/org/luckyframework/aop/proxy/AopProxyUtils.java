package org.luckyframework.aop.proxy;

import com.lucky.utils.base.Assert;
import org.luckyframework.aop.advisor.Advisor;
import org.luckyframework.aop.advisor.PointcutAdvisor;
import org.jacklamb.lucky.beans.factory.BeanFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 15:10
 */
public class AopProxyUtils {

    public static Object applyAdvices(Object target, Method method, Object[] args, List<Advisor> matchAdvisors,
                                      Object proxy, BeanFactory beanFactory) throws Throwable {
        List<Object> advices = getShouldApplyAdvices(target.getClass(), method, matchAdvisors, beanFactory);
        if(Assert.isEmptyCollection(advices)){
            return method.invoke(target,args);
        }
        AopAdviceChainInvocation chain=new AopAdviceChainInvocation(proxy,target,method,args,advices);
        return chain.invoke();
    }


    public static List<Object> getShouldApplyAdvices(Class<?> beanClass, Method method, List<Advisor> matchAdvisors,
                                                     BeanFactory beanFactory) throws Throwable {
        if (Assert.isEmptyCollection(matchAdvisors)) {
            return null;
        }
        List<Object> advices = new ArrayList<>();
        for (Advisor ad : matchAdvisors) {
            if (ad instanceof PointcutAdvisor) {
                //如果当前方法和切入点匹配就是要加入增强功能的方法
                if (((PointcutAdvisor) ad).getPointcut().matchMethod(beanClass,method)) {
                    Object advice = ad.getAdvisor();
                    advice= advice == null?beanFactory.getBean(ad.getAdvisorBeanName()):advice;
                    advices.add(advice);
                }
            }
        }
        return advices;
    }
}
