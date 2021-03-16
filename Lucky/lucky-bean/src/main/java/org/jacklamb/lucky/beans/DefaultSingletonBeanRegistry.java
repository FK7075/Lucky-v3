package org.jacklamb.lucky.beans;

import org.jacklamb.lucky.beans.factory.AbstractBeanFactory;
import org.jacklamb.lucky.beans.factory.DefaultBeanFactory;
import org.jacklamb.lucky.beans.factory.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/16 0016 16:57
 */
public abstract class DefaultSingletonBeanRegistry extends AbstractBeanFactory {

    private final static Logger log= LoggerFactory.getLogger(DefaultSingletonBeanRegistry.class);

    private static final Object NULL_OBJECT = new Object();

    // 单例池
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
    // 早期的单例对象
    private final Map<String,Object> earlySingletonObjects = new HashMap<>();
    // bean的工厂对象
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>();
    // 当前正在创建的Bean
    private final Set<String> singletonsCurrentlyInCreation = Collections.newSetFromMap(new ConcurrentHashMap<>());

    protected Object getSingleton(String beanName,boolean allowEarlyReference) throws Exception {
        Object singletonObject = this.singletonObjects.get(beanName);
        if(singletonObject == null && isSingletonCurrentlyInCreation(beanName)){
            synchronized (this.singletonObjects){
                singletonObject = earlySingletonObjects.get(beanName);
                if(singletonObject == null && allowEarlyReference){
                    ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                    if(singletonFactory != null){
                        singletonObject=singletonFactory.getObject();
                        this.earlySingletonObjects.put(beanName,singletonObject);
                        this.singletonFactories.remove(beanName);
                    }
                }
            }
        }
        return (singletonObject != NULL_OBJECT ? singletonObject : null);
    }

    protected Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) throws Exception {
        synchronized (this.singletonObjects) {
            Object singletonObject = this.singletonObjects.get(beanName);
            if (singletonObject == null) {
                this.singletonsCurrentlyInCreation.add(beanName);
                singletonObject = singletonFactory.getObject();
                this.singletonsCurrentlyInCreation.remove(beanName);
                addSingleton(beanName, singletonObject);
            }
            return (singletonObject != NULL_OBJECT ? singletonObject : null);
        }
    }

    protected void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, (singletonObject != null ? singletonObject : NULL_OBJECT));
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
        }
    }

    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {
                this.singletonFactories.put(beanName, singletonFactory);
                this.earlySingletonObjects.remove(beanName);
            }
        }
    }

    protected void removeSingleton(String beanName) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.remove(beanName);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
        }
    }

    protected boolean isSingletonCurrentlyInCreation(String beanName) {
        return this.singletonsCurrentlyInCreation.contains(beanName);
    }

    protected boolean containsSingleton(String name) {
        return this.singletonObjects.containsKey(name);
    }

}
