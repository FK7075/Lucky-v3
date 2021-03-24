package org.luckyframework.beans;

import org.luckyframework.beans.factory.BeanFactory;

/**
 * 用来描述依赖
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/13 上午10:06
 */
public class BeanReference {

    private String beanName;
    private Autowire autowire;
    private Class<?> type;

    public BeanReference(String beanName) {
        this.beanName = beanName;
        autowire=Autowire.BY_NAME;
    }

    public BeanReference(String beanName,Class<?> type) {
        this.beanName = beanName;
        this.type = type;
        autowire=Autowire.BY_TYPE;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Autowire getAutowire() {
        return autowire;
    }

    public void setAutowire(Autowire autowire) {
        this.autowire = autowire;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Class<?> getReferenceType(BeanFactory beanFactory){
        if(autowire == Autowire.BY_TYPE){
            return type;
        }
        return beanFactory.getType(beanName);
    }
}
