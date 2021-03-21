package org.jacklamb.luckyframework.beans;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/21 下午11:44
 */
public abstract class DefaultBeanDefinition implements BeanDefinition {

    /** bean的名称 */
    private Class<?> beanClass;
    /** scope 默认单例 */
    private BeanScope beanScope = BeanScope.SINGLETON;
    /** 是否为懒加载 默认false */
    private boolean isLazy=false;
    /** 工厂bean名 */
    private String factoryBeanName;
    /** 工厂方法名 */
    private String factoryMethodName;
    /** 初始化方法 */
    private String initMethodName;
    /** 销毁方法 */
    private String destroyMethodName;


    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public void setBeanScope(BeanScope beanScope) {
        this.beanScope = beanScope;
    }

    public void setLazy(boolean lazy) {
        isLazy = lazy;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public void setFactoryMethodName(String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
    }

    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

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
        return BeanScope.SINGLETON.equals(beanScope);
    }

    @Override
    public boolean isPrototype() {
        return BeanScope.PROTOTYPE.equals(beanScope);
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
    public String getDestroyMethodName() {
        return this.destroyMethodName;
    }
}
