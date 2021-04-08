package org.luckyframework.aop.proxy;

import org.luckyframework.aop.advisor.Advisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/7 0007 11:55
 */
public class JdkDynamicAopProxy implements InvocationHandler,AopProxy {

    private static final Logger logger = LoggerFactory.getLogger(JdkDynamicAopProxy.class);
    private final Object target;
    private List<Advisor> matchAdvisors;

    public JdkDynamicAopProxy(Object target, List<Advisor> matchAdvisors) {
        this.target = target;
        this.matchAdvisors = matchAdvisors;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return AopProxyUtils.applyAdvices(proxy,target,method,args,matchAdvisors);
    }


    @Override
    public Object getProxy() {
        return this.getProxy(target.getClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating jdk proxy for '"+target+"'");
        }
        return Proxy.newProxyInstance(classLoader,target.getClass().getInterfaces(),this);
    }


}
