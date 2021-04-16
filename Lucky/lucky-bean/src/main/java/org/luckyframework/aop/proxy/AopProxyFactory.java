package org.luckyframework.aop.proxy;

import org.luckyframework.aop.advisor.Advisor;
import org.jacklamb.lucky.beans.factory.BeanFactory;

import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/19 0019 9:44
 */
public interface AopProxyFactory {

    AopProxy createAopProxy(Object bean, String beanName, List<Advisor> matchAdvisor, BeanFactory beanFactory)
            throws Throwable;

    static AopProxyFactory getDefaultAopFactory(){
        return new DefAopProxyFactory();
    }
}
