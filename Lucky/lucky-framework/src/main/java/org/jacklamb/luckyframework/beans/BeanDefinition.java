package org.jacklamb.luckyframework.beans;

import com.lucky.utils.base.Assert;

/**
 * bean的定义信息
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/21 下午11:06
 */
public interface BeanDefinition {

    /**
     * 获取实例的Class对象，用于向bean工厂提供实例的Class信息
     */
    Class<?> getBeanClass();

    /**
     * Scope
     */
    BeanScope getScope();

    /**
     * 是否为单例
     */
    boolean isSingleton();

    /**
     * 是否原型
     */
    boolean isPrototype();

    /**
     * 是否是懒加载的
     */
    boolean isLazy();

    /**
     * 工厂bean名：表示该bean的实例是由工厂生产的，该方将会法返回该工厂的name
     * 校验：
     * getFactoryBeanName()不为null时，getFactoryMethodName()必然不能为null，getBeanClass()必然为null
     */
    String getFactoryBeanName();

    /**
     * 工厂方法名
     * 校验：
     * getFactoryMethodName()!=null && getBeanClass()!=null && getFactoryMethodName()==null
     *                              ==> 静态工厂方法
     * getFactoryMethodName()!=null && getBeanClass()==null && getFactoryMethodName()！=null
     *                              ==> 非静态工厂方法
     */
    String getFactoryMethodName();

    /**
     * 初始化方法
     */
    String getInitMethodName();

    /**
     * 销毁方法
     */
    String getDestroyMethodName();

    /**
     * 检验bean定义的合法性
     */
    default boolean validate(){
        //没有定义class，工厂方法或者工厂bean没有指定 --> 不合法
        if(getBeanClass()==null){
            if(Assert.isBlankString(getFactoryBeanName())||Assert.isBlankString(getFactoryMethodName())){
                return false;
            }
        }

        //即定义了class，又定义了工厂bean -->不合法
        if(getBeanClass() != null && !Assert.isBlankString(getFactoryBeanName())){
            return false;
        }
        return true;
    }


}