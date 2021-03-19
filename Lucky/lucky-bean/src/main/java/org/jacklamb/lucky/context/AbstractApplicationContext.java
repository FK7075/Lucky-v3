package org.jacklamb.lucky.context;

import org.jacklamb.lucky.beans.factory.BeanFactory;
import org.jacklamb.lucky.beans.factory.PreBuildBeanFactory;
import org.jacklamb.lucky.beans.postprocessor.BeanPostProcessor;
import org.jacklamb.lucky.exception.BeansException;

import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/19 0019 15:09
 */
public abstract class AbstractApplicationContext implements ApplicationContext {

    protected BeanFactory beanFactory;

    public AbstractApplicationContext() {
        super();
        this.beanFactory = new PreBuildBeanFactory();
    }

    //获取bean定义对象
    @Override
    public Object getBean(String name) throws BeansException {
        return beanFactory.getBean(name);
    }

    //注册bean定义信息到bean工厂
    @Override
    public void registerBeanPostProcessor(BeanPostProcessor bpp) {
        this.beanFactory.registerBeanPostProcessor(bpp);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return null;
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return beanFactory.getBean(requiredType);
    }

    @Override
    public Class<?> getType(String name) throws BeansException {
        return beanFactory.getType(name);
    }

    @Override
    public boolean containsBean(String name) {
        return beanFactory.containsBean(name);
    }

    @Override
    public boolean isSingleton(String name) throws BeansException {
        return beanFactory.isSingleton(name);
    }

    @Override
    public boolean isPrototype(String name) throws BeansException {
        return beanFactory.isPrototype(name);
    }

    @Override
    public List<BeanPostProcessor> getBeanPostProcessors() {
        return beanFactory.getBeanPostProcessors();
    }
}
