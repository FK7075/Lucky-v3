package org.luckyframework.context.annotation;

import com.lucky.utils.type.AnnotationMetadata;
import org.luckyframework.beans.BeanDefinitionRegistry;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/29 0029 9:44
 */
public interface ImportBeanDefinitionRegistrar {

    default void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    }

}
