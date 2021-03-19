package org.jacklamb.lucky.aop.advice;

import org.jacklamb.lucky.aop.advisor.AspectJPointcutAdvisor;
import org.jacklamb.lucky.aop.proxy.AdvisorAutoProxyCreator;
import org.jacklamb.lucky.beans.GenericBeanDefinition;
import org.jacklamb.lucky.beans.factory.PreBuildBeanFactory;
import org.jacklamb.lucky.exception.BeanDefinitionRegisterException;
import org.junit.Test;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/19 0019 14:07
 */
public class AopTest {


    public static void main(String[] args) throws BeanDefinitionRegisterException {
        PreBuildBeanFactory pf=new PreBuildBeanFactory();
        AdvisorAutoProxyCreator creator = new AdvisorAutoProxyCreator();
        creator.registryAdvisor(new AspectJPointcutAdvisor(new MyAfter(),
                "execution(* org.jacklamb.lucky.aop.advice.MyBean.show())"));
        pf.registerBeanPostProcessor(creator);
        GenericBeanDefinition bd=new GenericBeanDefinition();
        bd.setBeanClass(MyBean.class);
        pf.registerBeanDefinition("bean",bd);
        MyBean bean = pf.getBean("bean", MyBean.class);
        bean.show();
    }
}
