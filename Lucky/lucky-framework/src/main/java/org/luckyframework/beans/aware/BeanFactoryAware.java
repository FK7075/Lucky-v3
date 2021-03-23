package org.luckyframework.beans.aware;

import org.luckyframework.beans.factory.BeanFactory;

/**
 * BeanFactory的预处理器，该接口的实现类
 * 将会通过{@link BeanFactoryAware#setBeanFactory(BeanFactory)}
 * 方法注入当前的BeanFactory
 * @author fk
 * @version 1.0
 * @date 2021/3/16 0016 16:00
 */
public interface BeanFactoryAware extends Aware {

    void setBeanFactory(BeanFactory beanFactory);
}
