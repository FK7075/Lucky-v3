package org.luckyframework.beans;

import com.lucky.utils.base.Assert;

/**
 * bean的定义信息
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/21 下午11:06
 */
public interface BeanDefinition extends Cloneable {

    /** 获取当前的BeanClass */
    Class<?> getBeanClass();

    /** 设置当前的BeanClass */
    void setBeanClass( Class<?> beanClass);

    /**  返回factory bean name */
    String getFactoryBeanName();

    /**  设置factory bean name */
    void setFactoryBeanName(String factoryBeanName);

    /** 返回bean factory method name */
    String getFactoryMethodName();

    /** 设置bean factory method name */
    void setFactoryMethodName(String factoryMethodName);

    /** 获取bean scope */
    BeanScope getScope();

    /** 设置bean scope */
    void setScope(BeanScope scope);

    /** 是否延迟初始化 */
    boolean isLazyInit();

    /** 设置是否延迟初始化 */
    void setLazyInit(boolean lazyInit);

    /** 构造器参数 */
    ConstructorValue[] getConstructorValues();

    /** 属性设置参数 */
    PropertyValue[] getPropertyValues();

    /** 设置初始化方法 */
    void setInitMethodName(String initMethodName);

    /** 获取初始化方法 */
    String getInitMethodName();

    /** 设置销毁方法 */
    void setDestroyMethodName(String destroyMethodName);

    /** 获取销毁方法 */
    String getDestroyMethodName();

    /** 是否为单例 */
    boolean isSingleton();

    /** 是否为原型 */
    boolean isPrototype();

    /** 复制 */
    BeanDefinition copy();

    /** 最终返回bean的类型 */
    Class<?> getFinallyClass();

    /** 设置最终返回bean的类型 */
    void setFinallyClass(Class<?> finallyClass);

    /** 设置是否优先匹配 */
    void setPrimary(boolean primary);

    /** 是否优先匹配 */
    boolean isPrimary();

    /** 设置依赖bean的名称 */
    String[] getDependsOn();

    /** 返回依赖bean的名称 */
    void setDependsOn(String[] depends);

    /** 获取优先级*/
    int getPriority();

    /** 设置优先级 */
    void setPriority(int priority);


    default boolean validate(){
        boolean classIsNull = Assert.isNull(getBeanClass());
        boolean factoryNameIsNull = Assert.isBlankString(getFactoryBeanName());
        boolean factoryMethodIsNull = Assert.isBlankString(getFactoryMethodName());

        if(classIsNull){
            //没有定义class，工厂方法或者工厂bean没有指定 --> 不合法
            if(factoryNameIsNull || factoryMethodIsNull){
                return false;
            }
        }

        //即定义了class，又定义了工厂bean -->不合法
        if(!classIsNull && !factoryNameIsNull){
            return false;
        }
        return true;
    }
}
