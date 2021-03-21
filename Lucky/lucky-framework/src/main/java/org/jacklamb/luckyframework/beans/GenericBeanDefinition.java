package org.jacklamb.luckyframework.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/21 下午11:53
 */
public class GenericBeanDefinition extends DefaultBeanDefinition {

    /** 最终bean的类型*/
    private Class<?> resultBeanClass;
    /** 创建bean的构造器*/
    private Constructor<?> constructor;
    /** 工厂方法*/
    private Method factoryMethod;
    /** 构造器的执行参数*/
    private List<?> constructorArgumentValues;
    /** 属性依赖的配置*/
    private List<PropertyValue> propertyValues;

    public Class<?> getResultBeanClass() {
        return resultBeanClass;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    public Method getFactoryMethod() {
        return factoryMethod;
    }

    public void setFactoryMethod(Method factoryMethod) {
        this.factoryMethod = factoryMethod;
    }

    public List<?> getConstructorArgumentValues() {
        return constructorArgumentValues;
    }

    public void setConstructorArgumentValues(List<?> constructorArgumentValues) {
        this.constructorArgumentValues = constructorArgumentValues;
    }

    public List<PropertyValue> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(List<PropertyValue> propertyValues) {
        this.propertyValues = propertyValues;
    }

}
