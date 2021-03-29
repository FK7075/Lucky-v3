package org.luckyframework.impotrBeanDefinitionRegistrar;

import com.lucky.utils.type.AnnotationMetadata;
import org.luckyframework.beans.BeanDefinitionRegistry;
import org.luckyframework.beans.GenericBeanDefinition;
import org.luckyframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.luckyframework.t1.postProcessor.MyBeanPostProcessor;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/29 0029 11:58
 */
public class MyAutoBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        registry.registerBeanDefinition("myBeanPostProcessor",new GenericBeanDefinition(MyBeanPostProcessor.class));
    }
}
