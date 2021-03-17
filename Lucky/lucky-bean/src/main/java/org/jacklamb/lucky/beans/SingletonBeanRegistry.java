package org.jacklamb.lucky.beans;

/**
 * 单例bean注册器
 * @author fk
 * @version 1.0
 * @date 2021/3/17 0017 9:48
 */
public interface SingletonBeanRegistry {

    /**
     * 注册一个单例bean
     * @param beanName bean的名称
     * @param singletonObject 单例bean
     */
    void registerSingleton(String beanName, Object singletonObject);

    /**
     * 获取一个单例bean
     * @param beanName bean的名称
     * @return 该名称对应的单例bean
     */
    Object getSingleton(String beanName);

    /**
     * 判断是否存在该名称的单例bean
     * @param beanName bean的名称
     * @return Y:true/N:false
     */
    boolean containsSingleton(String beanName);

    /**
     * 返回所有单例bean的名称
     * @return 所有单例bean的名称
     */
    String[] getSingletonNames();

    /**
     * 获取所有单例bean的数量
     * @return 所有单例bean的数量
     */
    int getSingletonCount();

    /**
     * 返回所有的单例bean
     * @return 返回所有的单例bean
     */
    Object getSingletonMutex();
}
