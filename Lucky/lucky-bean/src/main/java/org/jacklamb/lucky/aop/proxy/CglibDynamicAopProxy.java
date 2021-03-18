package org.jacklamb.lucky.aop.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.jacklamb.lucky.aop.advisor.Advisor;
import org.jacklamb.lucky.beans.BeanDefinition;
import org.jacklamb.lucky.beans.factory.BeanFactory;
import org.jacklamb.lucky.beans.factory.DefaultBeanFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 基于Cglib的动态代理
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 14:59
 */
public class CglibDynamicAopProxy implements AopProxy , MethodInterceptor {

    private static Enhancer enhancer = new Enhancer();
    private final String beanName;
    private final Object target;
    private List<Advisor> matchAdvisors;
    private final BeanFactory beanFactory;

    public CglibDynamicAopProxy(String beanName, Object target, List<Advisor> matchAdvisors, BeanFactory beanFactory) {
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
        Class<?> targetClass = target.getClass();
        enhancer.setSuperclass(targetClass);
        enhancer.setInterfaces(targetClass.getInterfaces());
        enhancer.setCallback(this);
        Constructor<?> constructor = null;
        try {
            constructor=targetClass.getConstructor(new Class[]{});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if(constructor!=null){
            return enhancer.create();
        }
        else {
            BeanDefinition bd = ((DefaultBeanFactory)beanFactory).getBeanDefinition(beanName);
            return enhancer.create(bd.getConstructor().getParameterTypes(),bd.getConstructorArgumentRealValues());
        }
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        return AopProxyUtils.applyAdvices(target,method,args,matchAdvisors,proxy,beanFactory);
    }
}
