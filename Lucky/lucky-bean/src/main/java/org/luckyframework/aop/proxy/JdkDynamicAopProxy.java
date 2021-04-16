package org.luckyframework.aop.proxy;

import org.luckyframework.aop.advisor.Advisor;
import org.jacklamb.lucky.beans.factory.BeanFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * 基于JDK的动态代理
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 14:49
 */
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

    //切面的名字
    private String beanName;
    //真实对象
    private Object target;
    //所有Advisor
    private List<Advisor> matchAdvisors;
    //BeanFactory
    private BeanFactory beanFactory;

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public List<Advisor> getMatchAdvisors() {
        return matchAdvisors;
    }

    public void setMatchAdvisors(List<Advisor> matchAdvisors) {
        this.matchAdvisors = matchAdvisors;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public JdkDynamicAopProxy(String beanName, Object target, List<Advisor> matchAdvisors, BeanFactory beanFactory) {
        this.beanName = beanName;
        this.target = target;
        this.matchAdvisors = matchAdvisors;
        this.beanFactory = beanFactory;
    }

    @Override
    public Object getProxy() {
        return getProxy(target.getClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader,target.getClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return AopProxyUtils.applyAdvices(target,method,args,matchAdvisors,proxy,beanFactory);
    }
}
