package org.jacklamb.lucky.beans;

import org.jacklamb.lucky.exception.BeanDefinitionRegisterException;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/12 0012 18:28
 */
public interface BeanDefinitionRegister {

    //注册bean定义
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionRegisterException;

    //获取bean定义
    BeanDefinition getBeanDefinition(String beanName);

    //判断是否包含bean定义
    boolean containsBeanDefinition(String beanName);
}
