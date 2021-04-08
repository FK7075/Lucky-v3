package org.luckyframework.aop.proxy;

import com.lucky.utils.base.Assert;
import org.luckyframework.aop.advisor.Advisor;
import org.luckyframework.aop.advisor.AdvisorRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/7 0007 14:45
 */
public class ProxyFactory implements AdvisorRegistry {

    private final List<Advisor> advisors = new ArrayList<>();
    private Object target;

    public ProxyFactory(Object target) {
        this.target = target;
    }

    public ProxyFactory(){

    }

    public void setTarget(Object target) {
        this.target = target;
    }

    @Override
    public void registryAdvisor(Advisor advisor) {
        this.advisors.add(advisor);
    }

    @Override
    public List<Advisor> getAdvisors() {
        return this.advisors;
    }

    public Object getProxy(){
        return createAopProxy(target,advisors).getProxy();
    }

    private AopProxy createAopProxy(Object target,List<Advisor> matchAdvisors) {
        // 是该用jdk动态代理还是cglib？
        if (shouldUseJDKDynamicProxy(target)) {
            return new JdkDynamicAopProxy(target, matchAdvisors);
        } else {
            return new CglibDynamicAopProxy(target, matchAdvisors);
        }
    }

    private boolean shouldUseJDKDynamicProxy(Object bean) {
        Class<?> aClass = bean.getClass();
        return !Assert.isEmptyArray(aClass.getInterfaces());
    }
}
