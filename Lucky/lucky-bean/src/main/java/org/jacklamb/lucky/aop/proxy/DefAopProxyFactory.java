package org.jacklamb.lucky.aop.proxy;

import com.lucky.utils.base.ArrayUtils;
import com.lucky.utils.base.Assert;
import org.jacklamb.lucky.aop.advisor.Advisor;
import org.jacklamb.lucky.beans.factory.BeanFactory;

import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/19 0019 9:45
 */
public class DefAopProxyFactory implements AopProxyFactory {
    @Override
    public AopProxy createAopProxy(Object bean, String beanName, List<Advisor> matchAdvisor, BeanFactory beanFactory) throws Throwable {
        if(shouldUseJDKDynamicProxy(bean,beanName)){
            return new JdkDynamicAopProxy(beanName,bean,matchAdvisor,beanFactory);
        }
        return new CglibDynamicAopProxy(beanName,bean,matchAdvisor,beanFactory);
    }

    //默认使用cglib
    private boolean shouldUseJDKDynamicProxy(Object bean, String beanName) {
        Class<?> aClass = bean.getClass();
        if(Assert.isEmptyArray(aClass.getInterfaces())){
            return false;
        }
        return true;
    }
}
