package org.jacklamb.luckyframework.beans.factory;

import com.lucky.utils.base.Assert;
import com.lucky.utils.reflect.MethodUtils;
import org.jacklamb.luckyframework.beans.BeanDefinition;
import org.jacklamb.luckyframework.beans.BeanDefinitionRegister;
import org.jacklamb.luckyframework.exception.BeanDefinitionRegisterException;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/22 上午12:05
 */
public abstract class AbstractBeanFactory
        implements BeanFactory, BeanDefinitionRegister, Closeable {

    // bean定义信息
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionRegisterException {
        Assert.notNull(beanName,"'beanName' is null");
        Assert.notNull(beanDefinition,"'BeanDefinition' is null");
        if(!beanDefinition.validate()){
            throw new BeanDefinitionRegisterException("The bean definition with the name '"+beanName+"' is invalid");
        }
        beanDefinitionMap.put(beanName, beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitionMap.get(beanName);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public void removeBeanDefinition(String beanName) {
        beanDefinitionMap.remove(beanName);
    }

    @Override
    public Collection<BeanDefinition> getBeanDefinitions() {
        return beanDefinitionMap.values();
    }

    @Override
    public Set<String> getBeanDefinitionNames() {
        return beanDefinitionMap.keySet();
    }

    @Override
    public void close() throws IOException {

    }
}
