package org.luckyframework.context;

import com.lucky.utils.annotation.Nullable;
import com.lucky.utils.base.Assert;
import com.lucky.utils.fileload.Resource;
import com.lucky.utils.fileload.resourceimpl.PathMatchingResourcePatternResolver;
import com.lucky.utils.reflect.ClassUtils;
import org.luckyframework.beans.factory.DefaultListableBeanFactory;
import org.luckyframework.exception.BeansException;
import org.luckyframework.exception.LuckyIOException;
import org.luckyframework.exception.NoSuchBeanDefinitionException;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/25 0025 16:21
 */
public class AnnotationPackageScannerApplicationContext implements ApplicationContext {

    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final static int basePackageIndex = AnnotationPackageScannerApplicationContext.class.getResource("/").toString().length();
    private final static String CLASS_CONF_TEMP = "classpath:%s/**/*.class";
    private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    private final String basePackage;
    private Set<Class<?>> allClasses;

    public AnnotationPackageScannerApplicationContext(String basePackage){
        Assert.notNull(basePackage,"");
        this.basePackage=basePackage;
    }

    public AnnotationPackageScannerApplicationContext(Class<?> baseClass){
        this(baseClass.getPackage().getName());
    }

    private void scanner(){
        String root = basePackage.replaceAll("\\.", "/");
        String scanRule=String.format(CLASS_CONF_TEMP,root);
        try {
            Resource[] classResources = resolver.getResources(scanRule);
            for (Resource resource : classResources) {
                String fullClass = resource.getURL().toString();
                fullClass = fullClass.substring(basePackageIndex,fullClass.lastIndexOf("."))
                        .replaceAll("/",".");
                allClasses.add(ClassUtils.getClass(fullClass));
            }
        } catch (IOException e) {
            throw new LuckyIOException(e);
        }
    }


    public void loadBeanDefinition(){


    }

    @Override
    public Class<?> getType(String name) throws BeansException {
        return this.beanFactory.getType(name);
    }

    @Override
    public Object getBean(String name) throws BeansException {
        return this.beanFactory.getBean(name);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return this.beanFactory.getBean(name, requiredType);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return this.beanFactory.getBean(requiredType);
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        return this.beanFactory.getBean(name, args);
    }

    @Override
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        return this.beanFactory.getBean(requiredType, args);
    }

    @Override
    public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        return this.beanFactory.isTypeMatch(name, typeToMatch);
    }

    @Override
    public boolean containsBean(String name) {
        return this.beanFactory.containsBean(name);
    }

    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return this.beanFactory.isSingleton(name);
    }

    @Override
    public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        return this.beanFactory.isPrototype(name);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanFactory.containsBeanDefinition(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return this.beanFactory.getBeanDefinitionCount();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return this.beanFactory.getBeanDefinitionNames();
    }

    @Override
    public String[] getBeanNamesForType(@Nullable Class<?> type) {
        return this.beanFactory.getBeanNamesForType(type);
    }

    @Override
    public String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons) {
        return this.beanFactory.getBeanNamesForType(type,includeNonSingletons);
    }

    @Override
    public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
        return this.beanFactory.getBeanNamesForAnnotation(annotationType);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
        return this.beanFactory.getBeansOfType(type);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons) throws BeansException {
        return this.beanFactory.getBeansOfType(type, includeNonSingletons);
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException {
        return this.beanFactory.getBeansWithAnnotation(annotationType);
    }

    @Nullable
    @Override
    public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException {
        return this.beanFactory.findAnnotationOnBean(beanName,annotationType);
    }
}
