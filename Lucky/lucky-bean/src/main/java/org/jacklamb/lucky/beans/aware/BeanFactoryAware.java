package org.jacklamb.lucky.beans.aware;

import org.jacklamb.lucky.beans.factory.BeanFactory;

/**
 *
 * @author fk
 * @version 1.0
 * @date 2021/3/16 0016 16:00
 */
public interface BeanFactoryAware extends Aware {

    void setBeanFactory(BeanFactory beanFactory);
}
