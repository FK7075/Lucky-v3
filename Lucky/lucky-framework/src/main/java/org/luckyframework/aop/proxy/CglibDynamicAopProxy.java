package org.luckyframework.aop.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.luckyframework.aop.advisor.Advisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/7 0007 14:28
 */
public class CglibDynamicAopProxy implements AopProxy, MethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JdkDynamicAopProxy.class);
    private static final Enhancer enhancer = new Enhancer();

    private Object target;
    private List<Advisor> matchAdvisors;
    private Class<?>[] constructorParameterTypes;
    private Object[] constructorParameterValues;

    public CglibDynamicAopProxy(Object target, List<Advisor> matchAdvisors) {
        this.target = target;
        this.matchAdvisors = matchAdvisors;
    }

    public void setConstructorParameterValues(Object[] constructorParameterValues) {
        this.constructorParameterValues = constructorParameterValues;
        int index = 0;
        constructorParameterTypes = new Class<?>[constructorParameterValues.length];
        for (Object value : constructorParameterValues) {
            constructorParameterTypes[index++] = value.getClass();
        }
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        return AopProxyUtils.applyAdvices(proxy, target, method, args,matchAdvisors);
    }

    @Override
    public Object getProxy() {
        return this.getProxy(target.getClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating cglib proxy for '"+target+"'");
        }
        Class<?> superClass = this.target.getClass();
        enhancer.setSuperclass(superClass);
        enhancer.setInterfaces(this.getClass().getInterfaces());
        enhancer.setCallback(this);
        if(constructorParameterValues == null){
            return enhancer.create();
        }
        return enhancer.create(constructorParameterTypes, constructorParameterValues);
    }
}
