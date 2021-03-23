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


        for (int i = 0; i < 3; i++) {
//            BBean b = sf.getBean("b", BBean.class);
//            System.out.println("b -> "+b+" , ab -> "+b.getA().getB());

            ABean a = sf.getBean("a", ABean.class);
            System.out.println("a -> "+a+" , b -> "+a.getB());
        }
    }
}
