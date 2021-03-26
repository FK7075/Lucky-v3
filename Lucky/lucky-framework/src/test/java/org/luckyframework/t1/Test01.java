package org.luckyframework.t1;

import com.lucky.utils.fileload.Resource;
import com.lucky.utils.fileload.resourceimpl.PathMatchingResourcePatternResolver;
import com.lucky.utils.reflect.FieldUtils;
import com.lucky.utils.type.ResolvableType;
import org.junit.Test;
import org.luckyframework.AppTest;
import org.luckyframework.beans.*;
import org.luckyframework.beans.factory.DefaultListableBeanFactory;
import org.luckyframework.context.AnnotationPackageScannerApplicationContext;
import org.luckyframework.context.ApplicationContext;
import org.luckyframework.context.ComponentBeanDefinitionReader;
import org.luckyframework.context.ConfigurationBeanDefinitionReader;
import org.luckyframework.context.annotation.Component;
import org.luckyframework.environment.DefaultEnvironment;
import org.luckyframework.environment.Environment;
import org.luckyframework.exception.BeanDefinitionRegisterException;
import org.luckyframework.t1.service.FileCommand;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/23 0023 14:11
 */
public class Test01 {

    public void initMethod(){
        System.out.println("Test01 Init ...");
    }

    public void destroyMethod(){
        System.out.println("Test01 Destroy ...");
    }

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

    @Test
    public void test6() throws IOException {
        ApplicationContext context = new AnnotationPackageScannerApplicationContext(AppTest.class);
        CBean bean = context.getBean(CBean.class);
        System.out.println(bean);
        CBean cBean = context.getBean(CBean.class);
        CBean cBean2 = context.getBean(CBean.class);
        System.out.println(cBean);
        System.out.println(cBean2);
        cBean.print();
        System.out.println(Arrays.toString(context.getBeanDefinitionNames()));
        for (int i = 0; i <3 ; i++) {
            System.out.println(context.getBean(Test01.class));
        }
        Environment environment = context.getBean(Environment.class);
        String[] split = environment.getProperty("java.class.path").toString().split(";");
        for (String s : split) {
            System.out.println(s);
        }
        context.close();

    }

    List<String> list = new ArrayList<>();

    @Test
    public void test7(){
        Field field = FieldUtils.getDeclaredField(this.getClass(),"list");
        ResolvableType type = ResolvableType.forField(field);
        System.out.println(type.resolveGeneric(0));
    }

    @Test
    public void test8(){
        ApplicationContext context = new AnnotationPackageScannerApplicationContext(AppTest.class);
        System.out.println(context.getType("tt2"));
        System.out.println(context.getBean("tt"));
        FileCommand fileCommand = context.getBean(FileCommand.class);
        System.out.println(fileCommand.getCommand());
    }
}
