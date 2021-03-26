package org.luckyframework.context.annotation;

import com.lucky.utils.annotation.Nullable;
import com.lucky.utils.fileload.ResourceLoader;
import org.luckyframework.beans.BeanDefinitionRegistry;
import org.luckyframework.beans.factory.ListableBeanFactory;
import org.luckyframework.environment.Environment;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/26 0026 14:39
 */
public interface ConditionContext {

    BeanDefinitionRegistry getRegistry();

    Environment getEnvironment();

    @Nullable
    ListableBeanFactory getBeanFactory();

    ResourceLoader getResourceLoader();

    @Nullable
    ClassLoader getClassLoader();
}
