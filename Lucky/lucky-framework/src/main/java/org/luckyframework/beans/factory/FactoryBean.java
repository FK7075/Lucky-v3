package org.luckyframework.beans.factory;

import com.lucky.utils.annotation.Nullable;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/24 0024 9:04
 */
public interface FactoryBean<T> {

    @Nullable
    T getObject() throws Exception;

    @Nullable
    Class<?> getObjectType();

    default boolean isSingleton() {
        return true;
    }
}
