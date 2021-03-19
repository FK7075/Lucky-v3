package org.jacklamb.lucky.context;

import com.lucky.utils.fileload.Resource;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/19 0019 15:15
 */
public class AnnotationConfigApplicationContext extends AbstractApplicationContext {

    private ClassPathBeanDefinitionScanner scanner;

    private AnnotationBeanDefinitionReader reader;

    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

    @Override
    public Resource getResource(String location) {
        return null;
    }


    public AnnotationConfigApplicationContext(Class<?>... componentClasses){

    }

    public AnnotationConfigApplicationContext(String... basePackages){

    }

}
