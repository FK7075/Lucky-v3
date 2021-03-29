package org.luckyframework.t1.postProcessor;

import org.luckyframework.beans.BeanPostProcessor;
import org.luckyframework.beans.aware.EnvironmentAware;
import org.luckyframework.context.annotation.Component;
import org.luckyframework.environment.Environment;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/26 下午10:30
 */
//@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

}
