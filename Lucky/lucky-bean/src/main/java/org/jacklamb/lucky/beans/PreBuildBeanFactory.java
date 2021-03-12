package org.jacklamb.lucky.beans;

import org.jacklamb.lucky.exception.BeanDefinitionRegisterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 提前实例化单例bean
 * @author fk
 * @version 1.0
 * @date 2021/3/12 0012 19:06
 */
public class PreBuildBeanFactory extends DefaultBeanFactory {

    private final static Logger log= LoggerFactory.getLogger(PreBuildBeanFactory.class);

    private final List<String> beanNames = new ArrayList<>();

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
            throws BeanDefinitionRegisterException {
        super.registerBeanDefinition(beanName, beanDefinition);
        synchronized (beanNames){
            beanNames.add(beanName);
        }
    }

}
