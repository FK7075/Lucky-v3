package org.jacklamb.lucky.beans.factory;

import org.jacklamb.lucky.exception.BeansException;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/16 0016 16:39
 */
@FunctionalInterface
public interface ObjectFactory<T> {

    T getObject() throws BeansException;
}
