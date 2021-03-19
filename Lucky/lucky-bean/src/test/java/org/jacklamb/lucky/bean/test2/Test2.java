package org.jacklamb.lucky.bean.test2;

import org.jacklamb.lucky.beans.BeanReference;
import org.jacklamb.lucky.beans.GenericBeanDefinition;
import org.jacklamb.lucky.beans.PropertyValue;
import org.jacklamb.lucky.beans.factory.PreBuildBeanFactory;
import org.jacklamb.lucky.beans.BeanScope;
import org.jacklamb.lucky.exception.BeanDefinitionRegisterException;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/13 下午6:55
 */
public class Test2 {

    private static PreBuildBeanFactory pf=new PreBuildBeanFactory();

    @Test
    public void singleton() throws BeanDefinitionRegisterException {
        GenericBeanDefinition bd=new GenericBeanDefinition();
        bd.setBeanClass(CBean.class);
//        bd.setScope(Scope.PROTOTYPE);
        pf.registerBeanDefinition("p-c",bd);

        bd=new GenericBeanDefinition();
        bd.setBeanClass(BBean.class);
        bd.setScope(BeanScope.PROTOTYPE);
        pf.registerBeanDefinition("p-b",bd);
    }

    @Test
    public void initCBean() throws BeanDefinitionRegisterException {
        GenericBeanDefinition bd=new GenericBeanDefinition();
        Object[] args={1,"I'M CBean"};
        bd.setBeanClass(CBean.class);
        bd.setScope(BeanScope.PROTOTYPE);
        bd.setConstructorArgumentValues(Arrays.asList(args));
        pf.registerBeanDefinition("c",bd);
    }

    @Test
    public void initBBean() throws BeanDefinitionRegisterException {
        GenericBeanDefinition bd=new GenericBeanDefinition();
        BeanReference br=new BeanReference("c");
        Object[] args={br,"I'M BBean"};
        bd.setBeanClass(BBean.class);
//        bd.setScope(Scope.PROTOTYPE);
        bd.setConstructorArgumentValues(Arrays.asList(args));
        pf.registerBeanDefinition("b",bd);
    }

    @Test
    public void factoryBBean() throws BeanDefinitionRegisterException {
        GenericBeanDefinition bd=new GenericBeanDefinition();
        bd.setBeanClass(MyBeanFactory.class);
        pf.registerBeanDefinition("factory",bd);

        bd=new GenericBeanDefinition();
        bd.setBeanClass(MyBeanFactory.class);
        bd.setFactoryMethodName("getCBean");
        Object[] args={3,"HAHAHAHHA"};
        bd.setConstructorArgumentValues(Arrays.asList(args));
        pf.registerBeanDefinition("c2",bd);

        bd=new GenericBeanDefinition();
        bd.setFactoryBeanName("factory");
        bd.setFactoryMethodName("getBBean");
        BeanReference br=new BeanReference("c2");
        Object[] args1={br,"I'M BBean"};
        bd.setConstructorArgumentValues(Arrays.asList(args1));
        pf.registerBeanDefinition("b2",bd);
    }

    @Test
    public void ttt() throws BeanDefinitionRegisterException {
        GenericBeanDefinition bd=new GenericBeanDefinition();
        bd.setBeanClass(BBean.class);
        List<PropertyValue> pv =new ArrayList<>();
        pv.add(new PropertyValue("cBean",new BeanReference("s-c")));
        pv.add(new PropertyValue("name","BBEAN-FIELD"));
        bd.setPropertyValues(pv);
        bd.setScope(BeanScope.PROTOTYPE);
        pf.registerBeanDefinition("s-b",bd);

        bd=new GenericBeanDefinition();
        bd.setBeanClass(CBean.class);
        pv =new ArrayList<>();
        pv.add(new PropertyValue("bBean",new BeanReference("s-b")));
        pv.add(new PropertyValue("id",4));
        pv.add(new PropertyValue("name","CBEAN-FIELD"));
        bd.setPropertyValues(pv);
//        bd.setScope(Scope.PROTOTYPE);
        pf.registerBeanDefinition("s-c",bd);
    }

    @AfterClass
    public static void test() throws Exception {
        pf.preInstantiateSingletons();
//        for (int i = 0; i < 3 ; i++) {
//            System.out.println("sb ==> "+pf.getBean("s-b"));
//        }
//
//        for (int i = 0; i < 3 ; i++) {
//            System.out.println("sc ==> "+pf.getBean("s-c"));
//        }

//        BBean s_b = (BBean) pf.getBean("p-b");
        BBean s_b = (BBean) pf.getBean("s-b");
        System.out.println(s_b);

//        BBean bean = pf.getBean("p-b",BBean.class);
//        CBean s_c = pf.getBean(CBean.class);
//        System.out.println(bean);
//        System.out.println(s_c);
    }
}
