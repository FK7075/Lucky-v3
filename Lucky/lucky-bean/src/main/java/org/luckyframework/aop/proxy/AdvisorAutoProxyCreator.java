package org.luckyframework.aop.proxy;

import com.lucky.utils.base.Assert;
import com.lucky.utils.reflect.ClassUtils;
import org.luckyframework.aop.advisor.Advisor;
import org.luckyframework.aop.advisor.AdvisorRegistry;
import org.luckyframework.aop.advisor.PointcutAdvisor;
import org.luckyframework.aop.pointcut.Pointcut;
import org.jacklamb.lucky.beans.aware.BeanFactoryAware;
import org.jacklamb.lucky.beans.factory.BeanFactory;
import org.jacklamb.lucky.beans.postprocessor.BeanPostProcessor;
import org.jacklamb.lucky.exception.BeanCreationException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 切面自动代理的创建者
 * @author fk
 * @version 1.0
 * @date 2021/3/19 0019 9:48
 */
public class AdvisorAutoProxyCreator implements AdvisorRegistry, BeanPostProcessor, BeanFactoryAware {

    private List<Advisor> advisors;
    private BeanFactory beanFactory;

    public AdvisorAutoProxyCreator(){
        advisors = new ArrayList<>();
    }

    @Override
    public void registryAdvisor(Advisor advisor) {
        advisors.add(advisor);
    }

    @Override
    public List<Advisor> getAdvisors() {
        return advisors;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory=beanFactory;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        List<Advisor> matchedAdvisor = getMatchedAdvisor(bean, beanName);
        if(Assert.isEmptyCollection(matchedAdvisor)){
            return bean;
        }
        try {
            return createProxy(bean,beanName,matchedAdvisor);
        } catch (Throwable e) {
            throw new BeanCreationException(beanName,"An exception occurred when creating a proxy object for '"+beanName+"'",e);
        }
    }

    /**
     * 创建代理对象
     * @param bean 原对象
     * @param beanName 原对象的name
     * @param matchAdvisors 通过检验的所有切面
     * @return 代理对象
     * @throws Throwable
     */
    private Object createProxy(Object bean,String beanName,List<Advisor> matchAdvisors) throws Throwable {
        return AopProxyFactory.getDefaultAopFactory().createAopProxy(bean,beanName,matchAdvisors,beanFactory).getProxy();
    }

    private List<Advisor> getMatchedAdvisor(Object bean,String beanName){
        if(Assert.isEmptyCollection(advisors)){
            return null;
        }
        Class<?> targetClass = bean.getClass();
        List<Method> allMethods = ClassUtils.getAllMethods(targetClass);
        List<Advisor> matchAdvisors =new ArrayList<>();
        for (Advisor advisor : advisors) {
            if(isPointcutMatchBean((PointcutAdvisor) advisor,targetClass,allMethods)){
                matchAdvisors.add(advisor);
            }
        }
        return matchAdvisors;
    }

    private boolean isPointcutMatchBean(PointcutAdvisor pa,Class<?> beanClass,List<Method> methods){
        Pointcut pointcut =pa.getPointcut();
        if(!pointcut.matchClass(beanClass)){
            return false;
        }

        for (Method method : methods) {
            if(pointcut.matchMethod(beanClass,method)){
                return true;
            }
        }
        return false;
    }
}
