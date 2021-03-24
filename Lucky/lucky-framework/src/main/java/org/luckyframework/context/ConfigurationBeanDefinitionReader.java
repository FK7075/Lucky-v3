package org.luckyframework.context;

import com.lucky.utils.base.Assert;
import com.lucky.utils.base.BaseUtils;
import com.lucky.utils.type.AnnotatedElementUtils;
import org.luckyframework.beans.BeanDefinition;
import org.luckyframework.beans.GenericBeanDefinition;
import org.luckyframework.context.annotation.Configuration;
import org.luckyframework.exception.BeanDefinitionRegisterException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/24 0024 11:22
 */
public class ConfigurationBeanDefinitionReader implements BeanDefinitionReader{


    private final Class<?> configurationBeanClass;

    private final Map<String, BeanDefinition> configurationBeanDefinitions = new HashMap<>(10);


    public ConfigurationBeanDefinitionReader(Class<?> configurationBeanClass){
        Assert.notNull(configurationBeanClass,"configurationBeanClass is null");
        this.configurationBeanClass=configurationBeanClass;
        Configuration annotation = AnnotatedElementUtils.findMergedAnnotation(configurationBeanClass, Configuration.class);
        Assert.notNull(annotation,"'"+configurationBeanClass+"' type is illegal, legal type should be marked by '@org.luckyframework.context.annotation.Configuration' annotation");
        String name = Assert.isBlankString(annotation.value())
                ? BaseUtils.lowercaseFirstLetter(configurationBeanClass.getSimpleName())
                : annotation.value();
        addBeanDefinition(name,new GenericBeanDefinition(configurationBeanClass));
    }


    @Override
    public Map<String, BeanDefinition> getBeanDefinitions() {
        return null;
    }

    @Override
    public boolean containsBeanDefinitionName(String name) {
        return configurationBeanDefinitions.containsKey(name);
    }

    public void addBeanDefinition(String name, BeanDefinition beanDefinition) {
        check(name, beanDefinition);
        configurationBeanDefinitions.put(name,beanDefinition);
    }
}
