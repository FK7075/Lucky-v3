package org.luckyframework.beans;

import com.lucky.utils.base.Assert;
import org.luckyframework.exception.BeanDefinitionIllegalException;
import org.luckyframework.exception.BeanDefinitionRegisterException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/23 0023 10:05
 */
public class DefaultBeanDefinitionRegister implements BeanDefinitionRegister {

    private final Map<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionRegisterException {
        Assert.notNull(beanName,"beanName is null.");
        Assert.notNull(beanName,"BeanDefinition is null.");
        if(!beanDefinition.validate()){
            throw new BeanDefinitionIllegalException(beanName, beanDefinition);
        }
        if(containsBeanDefinition(beanName)){
            throw new BeanDefinitionRegisterException("The bean definition information with the name '"+beanName+"' has been registered");
        }
        this.beanDefinitionMap.put(beanName,beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return this.beanDefinitionMap.get(beanName);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public void removeBeanDefinition(String beanName) {
        this.beanDefinitionMap.remove(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[0]);
    }

}
