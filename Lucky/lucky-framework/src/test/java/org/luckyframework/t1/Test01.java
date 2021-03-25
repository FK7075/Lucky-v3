package org.luckyframework.t1;

import com.lucky.utils.fileload.Resource;
import com.lucky.utils.fileload.resourceimpl.PathMatchingResourcePatternResolver;
import org.junit.Test;
import org.luckyframework.beans.*;
import org.luckyframework.beans.factory.DefaultListableBeanFactory;
import org.luckyframework.context.ComponentBeanDefinitionReader;
import org.luckyframework.context.ConfigurationBeanDefinitionReader;
import org.luckyframework.context.annotation.Component;
import org.luckyframework.environment.DefaultEnvironment;
import org.luckyframework.environment.Environment;
import org.luckyframework.exception.BeanDefinitionRegisterException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

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
        ConfigurationBeanDefinitionReader cr = new ConfigurationBeanDefinitionReader(CBean.class);
        ComponentBeanDefinitionReader ar = new ComponentBeanDefinitionReader(ABean.class);
        ComponentBeanDefinitionReader br = new ComponentBeanDefinitionReader(BBean.class);
        DefaultListableBeanFactory sf =new DefaultListableBeanFactory();
        sf.registerBeanDefinition(cr.getBeanDefinition().getBeanName(),cr.getBeanDefinition().getDefinition());
        sf.registerBeanDefinition(ar.getBeanDefinition().getBeanName(),ar.getBeanDefinition().getDefinition());
        sf.registerBeanDefinition(br.getBeanDefinition().getBeanName(),br.getBeanDefinition().getDefinition());
        sf.singletonBeanInitialization();
        System.out.println(Arrays.toString(sf.getBeanDefinitionNames()));
        String[] names = sf.getBeanNamesForAnnotation(Component.class);

        for (String name : names) {
            System.out.println(sf.findAnnotationOnBean(name, Component.class));
        }

    }


    @Test
    public void test4(){
        Map<String, String> getenv = System.getenv();
        for (Map.Entry<String,String> e : getenv.entrySet()){
            System.out.println(e.getKey()+" = "+e.getValue());
        }

        System.out.println("-------------------------------------");
        Properties properties = System.getProperties();

        for (Map.Entry<Object,Object> e : properties.entrySet()){
            System.out.println(e.getKey()+" = "+e.getValue());
        }

    }

    @Test
    public void test5() throws IOException {
        Environment env =new DefaultEnvironment("classpath:**/*.*");
        System.out.println(env.parsing("hahah-<${java.path}><${info}>-okok"));
        System.out.println(env.parsing("info"));
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        for (Resource resource : resolver.getResources("classpath:**/*.*")) {
            System.out.println(resource);
        }
    }
}
