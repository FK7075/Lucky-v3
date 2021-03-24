package org.luckyframework.t1;

import org.junit.Test;
import org.luckyframework.beans.*;
import org.luckyframework.beans.factory.DefaultListableBeanFactory;
import org.luckyframework.beans.factory.StandardBeanFactory;
import org.luckyframework.context.ComponentBeanDefinitionReader;
import org.luckyframework.context.annotation.Component;
import org.luckyframework.exception.BeanDefinitionRegisterException;

import java.util.Arrays;
import java.util.Map;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/23 0023 14:11
 */
public class Test01 {

    @Test
    public void test1() throws BeanDefinitionRegisterException {
        DefaultListableBeanFactory sf =new DefaultListableBeanFactory();
        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setBeanClass(ABean.class);
        PropertyValue[] p ={
                new PropertyValue("id",12),
                new PropertyValue("b",new BeanReference("b",BBean.class))
        };
        Object[] c ={"A-NAME"};
        bd.setConstructorArgumentValues(c);
        bd.setPropertyValues(p);
//        bd.setBeanScope(BeanScope.PROTOTYPE);
        sf.registerBeanDefinition("a",bd);

        bd = new GenericBeanDefinition();
        bd.setBeanClass(BBean.class);
        PropertyValue[] p2 ={
                new PropertyValue("id",333),
                new PropertyValue("a",new BeanReference("a3",ABean.class))
        };
        bd.setPropertyValues(p2);
        bd.setBeanScope(BeanScope.PROTOTYPE);
        sf.registerBeanDefinition("b",bd);


        bd = new GenericBeanDefinition();
        bd.setBeanClass(ABean.class);
        sf.registerBeanDefinition("a1",bd);

        sf.singletonBeanInitialization();
        BBean bean = sf.getBean(BBean.class);
        System.out.println(bean);
    }

    @Test
    public void test2(){
        DefaultListableBeanFactory df =new DefaultListableBeanFactory();
        GenericBeanDefinition bd = new GenericBeanDefinition(CBean.class);
        bd.setFactoryMethodName("getABean");
        df.registerBeanDefinition("a",bd);

        bd = new GenericBeanDefinition(CBean.class);
        df.registerBeanDefinition("c",bd);

        bd = new GenericBeanDefinition(BBean.class);
        df.registerBeanDefinition("b2",bd);

        bd = new GenericBeanDefinition();
        bd.setFactoryBeanName("c");
        bd.setFactoryMethodName("getBBean");
        df.registerBeanDefinition("b",bd);

//        df.singletonBeanInitialization();
        System.out.println(Arrays.toString(df.getBeanNamesForType(BBean.class)));
        System.out.println("");

    }

    @Test
    public void test3(){
        ComponentBeanDefinitionReader cr = new ComponentBeanDefinitionReader(CBean.class);
        Map<String, BeanDefinition> beanDefinitions = cr.getBeanDefinitions();
        DefaultListableBeanFactory sf =new DefaultListableBeanFactory();
        for (Map.Entry<String,BeanDefinition> entry : beanDefinitions.entrySet()){
            sf.registerBeanDefinition(entry.getKey(),entry.getValue());
        }
        sf.singletonBeanInitialization();
        System.out.println(Arrays.toString(sf.getSingletonBeanNames()));

    }
}
