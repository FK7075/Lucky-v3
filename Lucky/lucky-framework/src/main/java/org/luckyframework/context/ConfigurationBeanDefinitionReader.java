package org.luckyframework.context;

import com.lucky.utils.base.Assert;
import com.lucky.utils.base.BaseUtils;
import com.lucky.utils.type.AnnotatedElementUtils;
import org.luckyframework.beans.BeanDefinition;
import org.luckyframework.beans.GenericBeanDefinition;
import org.luckyframework.context.annotation.Configuration;
import org.luckyframework.exception.BeanDefinitionRegisterException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/24 0024 11:22
 */
public class ConfigurationBeanDefinitionReader extends ComponentBeanDefinitionReader {

    private final Map<String, BeanDefinition> configurationBeanDefinitions = new HashMap<>(10);


    public ConfigurationBeanDefinitionReader(Class<?> configurationBeanClass){
        super(configurationBeanClass);
        Configuration annotation = AnnotatedElementUtils.findMergedAnnotation(configurationBeanClass, Configuration.class);
        Assert.notNull(annotation,"'"+configurationBeanClass+"' type is illegal, legal type should be marked by '@org.luckyframework.context.annotation.Configuration' annotation");
    }

    public List<BeanDefinitionPojo> getBeanDefinitions(){
        List<BeanDefinitionPojo> beanDefinitions = new ArrayList<>();
        beanDefinitions.add(this.getBeanDefinition());
        return beanDefinitions;
    }
}
