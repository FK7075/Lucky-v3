package org.jacklamb.luckyframework.beans.factory;

import org.jacklamb.luckyframework.exception.BeansException;

/**
 * Bean工厂
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/21 下午10:59
 */
public interface BeanFactory {

    String FACTORY_BEAN_PREFIX = "&";

    /**
     * 获取一个bean的实例
     * @param name bean的唯一ID
     * @return bean实例
     * @throws BeansException bean异常
     */
    Object getBean(String name) throws BeansException;

    /**
     * 获取一个bean实例，并将其转化为对应的类型
     * @param name bean的唯一ID
     * @param requiredType bean的类型，可以是接口或者抽象类
     * @return bean实例
     */
    <T> T getBean(String name,Class<T> requiredType) throws BeansException;

    /**
     * 根据bean的类型来获得实例
     * @param requiredType bean的类型
     * @return bean实例
     */
    <T> T getBean(Class<T> requiredType) throws BeansException;

    /**
     * 获取bean的类型
     * @param name bean的名称
     * @return bean的类型
     */
    Class<?> getType(String name)throws BeansException;

    /**
     * 是否包含该名称的bean实例
     * @param name 待检验的bean实例名称
     * @return
     */
    boolean containsBean(String name);

    /**
     * 是否是单例bean
     * @param name bean的名称
     * @return 是否为单例
     * @throws Exception
     */
    boolean isSingleton(String name) throws BeansException;

    /**
     * 是否是原型bean
     * @param name bean的名称
     * @return 是否为原型
     * @throws Exception
     */
    boolean isPrototype(String name) throws BeansException;

}
