package org.jacklamb.lucky.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/12 0012 18:15
 */
public class GenericBeanDefinition implements BeanDefinition {

    // bean的名称
    private Class<?> beanClass;
    // scope 默认单例
    private BeanScope beanScope = BeanScope.SINGLETON;
    // 是否为懒加载 默认false
    private boolean isLazy=false;
    // 工厂bean名
    private String factoryBeanName;
    // 工厂方法名
    private String factoryMethodName;
    // 初始化方法
    private String initMethodName;
    // 销毁方法
    private String destroyMethodName;
    // 构造器的参数值
    private List<?> constructorArgumentValues;
    // 属性依赖的值
    private List<PropertyValue> propertyValues;
    // 构造器缓存(实例为·原型·类型时可以直接获取)
    private Constructor<?> constructor;
    // 工厂方法缓存(实例为·原型·类型时可以直接获取)
    private Method factoryMethod;
    //没有无参构造函数时,传递构造参数的类型和值到cglib动态代理里面去获取有参构造函数进行增强
    private ThreadLocal<Object[]> realConstructorArgumentValues = new ThreadLocal<>();


    //设置属性依赖的值
    public void setPropertyValues(List<PropertyValue> propertyValues) {
        this.propertyValues = propertyValues;
    }

    // 为构造器设置参数值
    public void setConstructorArgumentValues(List<?> constructorArgumentValues) {
        this.constructorArgumentValues = constructorArgumentValues;
    }

    //设置工厂bean名
    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    //设置scope
    public void setScope(BeanScope beanScope) {
        this.beanScope = beanScope;
    }

    //设置是否懒加载
    public void setLazy(boolean lazy) {
        isLazy = lazy;
    }

    //设置工厂bean名
    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    //设置工厂方法名
    public void setFactoryMethodName(String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
    }

    //设置初始化方法
    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    //设置销毁方法
    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }

    @Override
    public Class<?> getBeanClass() {
        return this.beanClass;
    }

    @Override
    public BeanScope getScope() {
        return this.beanScope;
    }

    @Override
    public boolean isSingleton() {
        return BeanScope.SINGLETON.equals(this.beanScope);
    }

    @Override
    public boolean isPrototype() {
        return BeanScope.PROTOTYPE.equals(this.beanScope);
    }

    @Override
    public boolean isLazy() {
        return this.isLazy;
    }

    @Override
    public String getFactoryBeanName() {
        return this.factoryBeanName;
    }

    @Override
    public String getFactoryMethodName() {
        return this.factoryMethodName;
    }

    @Override
    public String getInitMethodName() {
        return this.initMethodName;
    }

    @Override
    public List<?> getConstructorArgumentValues() {
        return this.constructorArgumentValues;
    }

    @Override
    public List<PropertyValue> getPropertyValues() {
        return this.propertyValues;
    }

    @Override
    public String getDestroyMethodName() {
        return this.destroyMethodName;
    }

    @Override
    public Constructor<?> getConstructor() {
        return this.constructor;
    }

    @Override
    public void setConstructor(Constructor<?> constructor) {
        this.constructor=constructor;
    }

    @Override
    public Method getFactoryMethod() {
        return this.factoryMethod;
    }

    @Override
    public void setFactoryMethod(Method factoryMethod) {
        this.factoryMethod=factoryMethod;
    }

    @Override
    public Object[] getConstructorArgumentRealValues() {
        return realConstructorArgumentValues.get();
    }

    @Override
    public void setConstructorArgumentRealValues(Object[] values) {
        realConstructorArgumentValues.set(values);
    }

    @Override
    public String toString() {
        return "GenericBeanDefinition{" +
                "beanClass=" + beanClass +
                ", scope=" + beanScope +
                ", factoryBeanName='" + factoryBeanName + '\'' +
                ", factoryMethodName='" + factoryMethodName + '\'' +
                ", initMethodName='" + initMethodName + '\'' +
                ", destroyMethodName='" + destroyMethodName + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((beanClass == null) ? 0 : beanClass.hashCode());
        result = prime * result + ((destroyMethodName == null) ? 0 : destroyMethodName.hashCode());
        result = prime * result + ((factoryBeanName == null) ? 0 : factoryBeanName.hashCode());
        result = prime * result + ((factoryMethodName == null) ? 0 : factoryMethodName.hashCode());
        result = prime * result + ((initMethodName == null) ? 0 : initMethodName.hashCode());
        result = prime * result + ((beanScope == null) ? 0 : beanScope.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GenericBeanDefinition other = (GenericBeanDefinition) obj;
        if (beanClass == null) {
            if (other.beanClass != null)
                return false;
        } else if (!beanClass.equals(other.beanClass))
            return false;
        if (destroyMethodName == null) {
            if (other.destroyMethodName != null)
                return false;
        } else if (!destroyMethodName.equals(other.destroyMethodName))
            return false;
        if (factoryBeanName == null) {
            if (other.factoryBeanName != null)
                return false;
        } else if (!factoryBeanName.equals(other.factoryBeanName))
            return false;
        if (factoryMethodName == null) {
            if (other.factoryMethodName != null)
                return false;
        } else if (!factoryMethodName.equals(other.factoryMethodName))
            return false;
        if (initMethodName == null) {
            if (other.initMethodName != null)
                return false;
        } else if (!initMethodName.equals(other.initMethodName))
            return false;
        if (beanScope == null) {
            return other.beanScope == null;
        } else {
            return beanScope.equals(other.beanScope);
        }
    }
}
