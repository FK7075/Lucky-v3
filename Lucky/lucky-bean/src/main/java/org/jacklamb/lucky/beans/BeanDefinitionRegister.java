package org.jacklamb.lucky.beans;

import org.jacklamb.lucky.exception.BeanDefinitionRegisterException;

import java.util.Collection;
import java.util.Set;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/12 0012 18:28
 */
public interface BeanDefinitionRegister {

    //注册bean定义
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionRegisterException;

    //获取bean定义
    BeanDefinition getBeanDefinitions(String beanName);

    //判断是否包含bean定义
    boolean containsBeanDefinition(String beanName);

    //移除bean的定义
    void removeBeanDefinition(String beanName);

    //获取所有bean的定义
    Collection<BeanDefinition> getBeanDefinitions();

    //获取所有bean定义的注册名
    Set<String> getBeanDefinitionNames();
}
