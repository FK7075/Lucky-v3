package org.luckyframework.context;

import com.lucky.utils.base.Assert;
import org.luckyframework.beans.BeanDefinition;
import org.luckyframework.exception.BeanDefinitionRegisterException;

import java.util.Map;

/**
 * BeanDefinition的读取器
 * @author fk
 * @version 1.0
 * @date 2021/3/24 0024 11:19
 */
public interface BeanDefinitionReader {

    /**
     * 获取所有解析得到的BeanDefinition
     */
    Map<String, BeanDefinition> getBeanDefinitions();

    default boolean containsBeanDefinitionName(String name){
        return false;
    }

    default void check(String name,BeanDefinition beanDefinition) {
        Assert.notNull(name,"BeanDefinition name is null");
        Assert.notNull(beanDefinition,"BeanDefinition is null");
        if(containsBeanDefinitionName(name)){
            throw new BeanDefinitionRegisterException("The bean definition information named '"+name+"' already exists");
        }
    }
}
