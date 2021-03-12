package org.jacklamb.lucky.beans;

import com.lucky.utils.base.Assert;
import org.jacklamb.lucky.exception.BeanDefinitionRegisterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/12 0012 18:31
 */
public class DefaultBeanFactory implements BeanFactory,BeanDefinitionRegister, Closeable {

    private final static Logger log= LoggerFactory.getLogger(DefaultBeanFactory.class);

    //bean定义信息
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
    //单例池
    private final Map<String, Object> singletonPool = new ConcurrentHashMap<>(256);

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
            throws BeanDefinitionRegisterException {
        Objects.requireNonNull(beanName, "注册bean需要给入beanName");
        Objects.requireNonNull(beanDefinition, "注册bean需要给入beanDefinition");

        if(!beanDefinition.validate()){
            throw new BeanDefinitionRegisterException("名字为[" + beanName + "] 的bean定义不合法：" + beanDefinition);
        }

        this.beanDefinitionMap.put(beanName,beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return this.beanDefinitionMap.get(beanName);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public Object getBean(String name) throws Exception {
        return this.doGetBean(name);
    }

    protected Object doGetBean(String name) throws Exception {
        //判断给入的bean名字不能为空
        Objects.requireNonNull(name, "beanName不能为空");
        Object instance = this.singletonPool.get(name);
        if(instance != null){
            return instance;
        }

        BeanDefinition beanDefinition = this.getBeanDefinition(name);
        Objects.requireNonNull(beanDefinition, name+"的BeanDefinition为空");

        Class<?> beanClass = beanDefinition.getBeanClass();
        if(beanClass != null){
            //构造器构造
            if(Assert.isBlankString(beanDefinition.getFactoryMethodName())){
                instance = createInstanceByConstructor(beanDefinition);
            }
            //静态工厂方法构造
            else{
                instance = createInstanceByStaticFactoryMethod(beanDefinition);
            }
        }
        //非静态工厂bean方法构造
        else{
            instance = createInstanceByFactoryBean(beanDefinition);
        }

        // 执行初始化方法
        this.doInit(beanDefinition, instance);

        //存放单例的bean到beanMap
        if (beanDefinition.isSingleton()) {
            singletonPool.put(name, instance);
        }

        return instance;

    }

    private void doInit(BeanDefinition beanDefinition, Object instance) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if(!Assert.isBlankString(beanDefinition.getInitMethodName())){
            Method initMethod = beanDefinition.getBeanClass().getMethod(beanDefinition.getInitMethodName());
            initMethod.invoke(instance);
        }
    }

    private Object createInstanceByFactoryBean(BeanDefinition beanDefinition) throws Exception {
        Object beanFactory=doGetBean(beanDefinition.getFactoryBeanName());
        Method factoryMethod = beanFactory.getClass().getMethod(beanDefinition.getFactoryMethodName());
        return factoryMethod.invoke(beanFactory);
    }

    // 静态工厂方法
    private Object createInstanceByStaticFactoryMethod(BeanDefinition beanDefinition)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> beanClass = beanDefinition.getBeanClass();
        Method staticFactoryMethod = beanClass.getMethod(beanDefinition.getFactoryMethodName());
        return staticFactoryMethod.invoke(beanClass);
    }

    // 构造方法来构造对象
    private Object createInstanceByConstructor(BeanDefinition beanDefinition)
            throws IllegalAccessException, InstantiationException {
        try {
            //拿到bean的类型,然后调用newInstance通过反射来创建bean实例
            return beanDefinition.getBeanClass().newInstance();
        } catch (SecurityException e1) {
            log.error("创建bean的实例异常,beanDefinition：" + beanDefinition, e1);
            throw e1;
        }
    }


    @Override
    public void close() throws IOException {
        for (Map.Entry<String,BeanDefinition> entry:beanDefinitionMap.entrySet()){
            String beanName = entry.getKey();
            BeanDefinition bd = entry.getValue();

            if(bd.isSingleton() && !Assert.isBlankString(bd.getDestroyMethodName())){
                Object instance = this.singletonPool.get(beanName);
                try {
                    Method destroyMethod = instance.getClass().getMethod(bd.getDestroyMethodName());
                    destroyMethod.invoke(instance);
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e1) {
                    log.error("执行bean[" + beanName + "] " + bd + " 的销毁方法执行异常！", e1);
                }
            }

        }
    }
}
