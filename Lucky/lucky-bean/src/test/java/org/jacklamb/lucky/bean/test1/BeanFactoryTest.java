package org.jacklamb.lucky.bean.test1;

import org.jacklamb.lucky.beans.GenericBeanDefinition;
import org.jacklamb.lucky.beans.factory.PreBuildBeanFactory;
import org.jacklamb.lucky.beans.Scope;
import org.jacklamb.lucky.exception.BeanDefinitionRegisterException;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/13 上午2:49
 */
public class BeanFactoryTest {

    private static final PreBuildBeanFactory df=new PreBuildBeanFactory();

    @Test
    public void newInstance() throws BeanDefinitionRegisterException {
        GenericBeanDefinition bd=new GenericBeanDefinition();
        bd.setBeanClass(ABean.class);
        bd.setInitMethodName("init");
        bd.setDestroyMethodName("destroy");
        df.registerBeanDefinition("a1",bd);
    }

    @Test
    public void factoryMethod() throws BeanDefinitionRegisterException {
        GenericBeanDefinition bd=new GenericBeanDefinition();
        String factory="factory";
        bd.setBeanClass(ABeanFactory.class);
        df.registerBeanDefinition(factory,bd);
        bd=new GenericBeanDefinition();
        bd.setFactoryBeanName(factory);
        bd.setFactoryMethodName("getABean");
        df.registerBeanDefinition("a2",bd);

    }

    @Test
    public void staticFactoryMethod() throws BeanDefinitionRegisterException {
        GenericBeanDefinition bd=new GenericBeanDefinition();
        bd.setBeanClass(ABeanFactory.class);
        bd.setFactoryMethodName("getABeanByStatic");
        bd.setScope(Scope.PROTOTYPE);
        df.registerBeanDefinition("a3",bd);
    }

    @AfterClass
    public static void test() throws Exception {
        df.preInstantiateSingletons();
        System.out.println("-------构造器------");
        for (int i = 0; i < 3 ; i++) {
            ABean a1 = (ABean) df.getBean("a1");
            a1.doSomthing();
        }
        System.out.println("-------Bean工厂------");
        for (int i = 0; i < 3 ; i++) {
            ABean a1 = (ABean) df.getBean("a2");
            a1.doSomthing();
        }
        System.out.println("-------静态工厂------");
        for (int i = 0; i < 3 ; i++) {
            ABean a1 = (ABean) df.getBean("a3");
            a1.doSomthing();
        }
        df.close();
    }

}
