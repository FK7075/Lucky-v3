package org.jacklamb.lucky.beans.factory;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/16 0016 16:39
 */
@FunctionalInterface
public interface ObjectFactory<T> {

    T getObject() throws Exception;
}
