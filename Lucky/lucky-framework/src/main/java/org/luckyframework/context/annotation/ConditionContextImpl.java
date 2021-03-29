package org.luckyframework.context.annotation;

import com.lucky.utils.annotation.Nullable;
import com.lucky.utils.fileload.ResourceLoader;
import org.luckyframework.beans.BeanDefinitionRegistry;
import org.luckyframework.beans.factory.ListableBeanFactory;
import org.luckyframework.context.ApplicationContext;
import org.luckyframework.environment.Environment;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/26 0026 14:47
 */
public class ConditionContextImpl implements ConditionContext {

    private final BeanDefinitionRegistry registry;
    private final Environment environment;
    private final ClassLoader loader;
    private final ListableBeanFactory beanFactory;
    private final ResourceLoader resourceLoader;

    public ConditionContextImpl(BeanDefinitionRegistry registry, Environment environment, ListableBeanFactory beanFactory, ResourceLoader resourceLoader) {
        this.registry = registry;
        this.environment = environment;
        this.beanFactory = beanFactory;
        this.resourceLoader = resourceLoader;
        this.loader = resourceLoader.getClassLoader();
    }

    public ConditionContextImpl(Environment environment, ApplicationContext context) {
        this(context,environment,context,context);
    }

    @Override
    public BeanDefinitionRegistry getRegistry() {
        return this.registry;
    }

    @Override
    public Environment getEnvironment() {
        return this.environment;
    }

    @Nullable
    @Override
    public ListableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    @Override
    public ResourceLoader getResourceLoader() {
        return this.resourceLoader;
    }

    @Nullable
    @Override
    public ClassLoader getClassLoader() {
        return this.loader;
    }
}
