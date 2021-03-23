package org.luckyframework.beans;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/21 下午11:53
 */
public class GenericBeanDefinition implements BeanDefinition {

    private Class<?> beanClass;
    private String factoryBeanName;
    private String factoryMethodName;
    private BeanScope beanScope = BeanScope.SINGLETON;
    private boolean lazyInit = false;
    private Object[] constructorArgumentValues;
    private PropertyValue[] propertyValues;
    private String initMethodName;
    private String destroyMethodName;
    private Constructor<?> cacheConstructor;
    private Method cacheFactoryMethod;
    private Object[] cacheConstructorArgumentRealValues;
    private PropertyValue[] cachePropertyRealValue;

    public Constructor<?> getCacheConstructor() {
        return cacheConstructor;
    }

    public void setCacheConstructor(Constructor<?> cacheConstructor) {
        this.cacheConstructor = cacheConstructor;
    }

    public Method getCacheFactoryMethod() {
        return cacheFactoryMethod;
    }

    public void setCacheFactoryMethod(Method cacheFactoryMethod) {
        this.cacheFactoryMethod = cacheFactoryMethod;
    }

    public Object[] getCacheConstructorArgumentRealValues() {
        return cacheConstructorArgumentRealValues;
    }

    public void setCacheConstructorArgumentRealValues(Object[] cacheConstructorArgumentRealValues) {
        this.cacheConstructorArgumentRealValues = cacheConstructorArgumentRealValues;
    }

    public PropertyValue[] getCachePropertyRealValue() {
        return cachePropertyRealValue;
    }

    public void setCachePropertyRealValue(PropertyValue[] cachePropertyRealValue) {
        this.cachePropertyRealValue = cachePropertyRealValue;
    }

    public  Class<?> getBeanClass() {
        return this.beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass=beanClass;
    }

    public String getFactoryBeanName() {
        return this.factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName=factoryBeanName;
    }

    public String getFactoryMethodName() {
        return this.factoryMethodName;
    }

    public void setFactoryMethodName(String factoryMethodName) {
        this.factoryMethodName=factoryMethodName;
    }

    public void setBeanScope(BeanScope beanScope) {
        this.beanScope = beanScope;
    }

    public BeanScope getScope() {
        return this.beanScope;
    }

    public void setScope(BeanScope scope) {
        this.beanScope=scope;
    }

    public boolean isLazyInit() {
        return this.lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit=lazyInit;
    }

    public Object[] getConstructorArgumentValues() {
        return this.constructorArgumentValues;
    }

    public void setConstructorArgumentValues(Object[] constructorArgumentValues) {
        this.constructorArgumentValues = constructorArgumentValues;
    }

    public PropertyValue[] getPropertyValues() {
        return this.propertyValues;
    }

    public void setPropertyValues(PropertyValue[] propertyValues) {
        this.propertyValues = propertyValues;
    }

    public void setInitMethodName(String initMethodName) {
        this.initMethodName=initMethodName;
    }

    public String getInitMethodName() {
        return this.initMethodName;
    }

    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName=destroyMethodName;
    }

    public String getDestroyMethodName() {
        return this.destroyMethodName;
    }

    public boolean isSingleton() {
        return BeanScope.SINGLETON == this.beanScope;
    }

    public boolean isPrototype() {
        return BeanScope.PROTOTYPE == this.beanScope;
    }

}
