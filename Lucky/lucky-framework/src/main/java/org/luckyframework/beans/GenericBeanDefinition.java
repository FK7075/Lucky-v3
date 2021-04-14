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
    private boolean isPrimary = false;
    private ConstructorValue[] constructorValues;
    private PropertyValue[] propertyValues;
    private String initMethodName;
    private String destroyMethodName;
    private Constructor<?> cacheConstructor;
    private Method cacheFactoryMethod;
    private Object[] cacheConstructorArgumentRealValues;
    private PropertyValue[] cachePropertyRealValue;
    private Class<?> finallyClass;
    private String[] dependsOn = new String[0];
    private int primary = Ordered.LOWEST_PRECEDENCE;

    public GenericBeanDefinition() {
    }

    public GenericBeanDefinition(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

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

    public ConstructorValue[] getConstructorValues() {
        return this.constructorValues;
    }

    public void setConstructorValues(ConstructorValue[] constructorValues) {
        this.constructorValues = constructorValues;
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

    @Override
    public BeanDefinition copy() {
        try {
            return (BeanDefinition) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> getFinallyClass() {
        return this.finallyClass;
    }

    @Override
    public void setFinallyClass(Class<?> finallyClass) {
        this.finallyClass = finallyClass;
    }

    @Override
    public void setPrimary(boolean primary) {
        this.isPrimary = primary;
    }

    @Override
    public boolean isPrimary() {
        return this.isPrimary;
    }

    @Override
    public String[] getDependsOn() {
        return this.dependsOn;
    }

    @Override
    public void setDependsOn(String[] depends) {
        this.dependsOn = depends;
    }

    @Override
    public int getPriority() {
        return primary;
    }

    @Override
    public void setPriority(int priority) {
        this.primary = priority;
    }

}
