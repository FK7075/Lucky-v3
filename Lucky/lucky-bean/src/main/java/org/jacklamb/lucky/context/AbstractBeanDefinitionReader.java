package org.jacklamb.lucky.context;

import org.jacklamb.lucky.beans.BeanDefinitionRegister;

/**
 * 具体的bean定义读取器抽象
 * @author fk
 * @version 1.0
 * @date 2021/3/19 0019 15:23
 */
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader {

    protected BeanDefinitionRegister registry;

    public AbstractBeanDefinitionReader(BeanDefinitionRegister registry) {
        super();
        this.registry = registry;
    }
}
