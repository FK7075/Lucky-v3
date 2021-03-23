package org.luckyframework.t1;

import org.junit.Test;
import org.luckyframework.beans.BeanReference;
import org.luckyframework.beans.BeanScope;
import org.luckyframework.beans.GenericBeanDefinition;
import org.luckyframework.beans.PropertyValue;
import org.luckyframework.beans.factory.StandardBeanFactory;
import org.luckyframework.exception.BeanDefinitionRegisterException;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/23 0023 14:11
 */
public class Test01 {

    @Test
    public void test1() throws BeanDefinitionRegisterException {
        StandardBeanFactory sf =new StandardBeanFactory();
        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setBeanClass(ABean.class);
        PropertyValue[] p ={
                new PropertyValue("id",12),
                new PropertyValue("b",new BeanReference("b"))
        };
        Object[] c ={"A-NAME"};
        bd.setConstructorArgumentValues(c);
        bd.setPropertyValues(p);
        bd.setBeanScope(BeanScope.PROTOTYPE);
        sf.registerBeanDefinition("a",bd);

        bd = new GenericBeanDefinition();
        bd.setBeanClass(BBean.class);
        PropertyValue[] p2 ={
                new PropertyValue("id",333),
                new PropertyValue("a",new BeanReference("a"))
        };
        bd.setPropertyValues(p2);
//        bd.setBeanScope(BeanScope.PROTOTYPE);
        sf.registerBeanDefinition("b",bd);
        sf.getBean("b");
        ABean bean = sf.getBean(ABean.class);
        Object bean1 = sf.getBean("a", "AAAA_NAME");
        System.out.println(bean);
        System.out.println(bean1);
    }
}
