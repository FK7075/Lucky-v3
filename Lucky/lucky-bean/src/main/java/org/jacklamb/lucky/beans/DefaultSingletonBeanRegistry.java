package org.jacklamb.lucky.beans;

import com.lucky.utils.base.Assert;
import org.jacklamb.lucky.beans.factory.DefaultListableBeanFactory;
import org.jacklamb.lucky.beans.factory.ObjectFactory;
import org.jacklamb.lucky.exception.BeanCreationNotAllowedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/16 0016 16:57
 */
public abstract class DefaultSingletonBeanRegistry extends DefaultListableBeanFactory implements SingletonBeanRegistry  {

    private final static Logger log= LoggerFactory.getLogger(DefaultSingletonBeanRegistry.class);

    // 单例池
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
    // 早期的单例对象
    private final Map<String,Object> earlySingletonObjects = new HashMap<>();
    // bean的工厂对象
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>();
    // 当前正在创建的Bean
    protected final Set<String> singletonsCurrentlyInCreation = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // 已近注册的实例名称集合
    private final Set<String> registeredSingletons = new LinkedHashSet<>(256);

    private boolean singletonsCurrentlyInDestruction =false;


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

    @Override
    public void registerSingleton(String beanName, Object singletonObject) {
        Assert.notNull(beanName,"Bean name must not be null");
        Assert.notNull(singletonObject,"Singleton object must not be null");
        synchronized (this.singletonObjects){
            Object oldObject = this.singletonObjects.get(beanName);
            if(oldObject != null){
                throw new IllegalStateException("Could not register object [" + singletonObject +
                        "] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
            }
        }
        addSingleton(beanName, singletonObject);
    }

    protected void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName,singletonObject);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.add(beanName);
        }
    }

    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(singletonFactory, "Singleton factory must not be null");
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {
                this.singletonFactories.put(beanName, singletonFactory);
                this.earlySingletonObjects.remove(beanName);
                this.registeredSingletons.add(beanName);
            }
        }
    }


    @Override
    public Object getSingleton(String beanName) {
        return getSingleton(beanName,true);
    }

    protected Object getSingleton(String beanName,boolean allowEarlyReference){
        Object singletonObject = this.singletonObjects.get(beanName);
        //一级缓存中不存在bean实例，而且当前实例正在创建中
        if(singletonObject == null && isSingletonCurrentlyInCreation(beanName)){
            synchronized (this.singletonObjects){
                singletonObject = earlySingletonObjects.get(beanName);
                //二级缓存中也不存在bean的实例，而且该bean允许提前暴露
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
        return singletonObject;
    }

    protected Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(beanName, "Bean name must not be null");
        synchronized (this.singletonObjects) {
            Object singletonObject = this.singletonObjects.get(beanName);
            if (singletonObject == null) {
                if (this.singletonsCurrentlyInDestruction) {
                    throw new BeanCreationNotAllowedException(beanName,
                            "Singleton bean creation not allowed while singletons of this factory are in destruction " +
                                    "(Do not request a bean from a BeanFactory in a destroy method implementation!)");
                }
                if (log.isDebugEnabled()){
                    log.debug("Creating shared instance of singleton bean '" + beanName + "'");
                }
                boolean isSingleton = getBeanDefinitions(beanName).isSingleton();
//                if(isSingleton){
//
//                }
//                this.singletonsCurrentlyInCreation.add(beanName);
                singletonObject = singletonFactory.getObject();
//
                if(getBeanDefinitions(beanName).isSingleton()){
                    addSingleton(beanName, singletonObject);
                }
            }
//            this.singletonsCurrentlyInCreation.remove(beanName);
            return singletonObject;
        }
    }

    public boolean containsSingleton(String name) {
        return this.singletonObjects.containsKey(name);
    }

    @Override
    public String[] getSingletonNames() {
        synchronized (this.singletonObjects){
            return this.registeredSingletons.toArray(new String[]{});
        }
    }

    @Override
    public int getSingletonCount() {
        synchronized (this.singletonObjects) {
            return this.registeredSingletons.size();
        }
    }

    @Override
    public Object getSingletonMutex() {
        return this.singletonObjects;
    }

    @Override
    public void close() throws IOException {
        singletonsCurrentlyInDestruction = true;
    }

    protected void clearSingletonCache() {
        synchronized (this.singletonObjects) {
            this.singletonObjects.clear();
            this.singletonFactories.clear();
            this.earlySingletonObjects.clear();
            this.registeredSingletons.clear();
            this.singletonsCurrentlyInDestruction = false;
        }
    }
}
