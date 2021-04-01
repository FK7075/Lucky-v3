package org.luckyframework.context;

import com.lucky.utils.annotation.Nullable;
import com.lucky.utils.base.Assert;
import com.lucky.utils.fileload.Resource;
import com.lucky.utils.fileload.resourceimpl.PathMatchingResourcePatternResolver;
import com.lucky.utils.reflect.AnnotationUtils;
import com.lucky.utils.reflect.ClassUtils;
import com.lucky.utils.type.AnnotatedElementUtils;
import org.luckyframework.beans.BeanPostProcessor;
import org.luckyframework.beans.aware.*;
import org.luckyframework.beans.factory.DefaultListableBeanFactory;
import org.luckyframework.context.annotation.Component;
import org.luckyframework.context.annotation.Configuration;
import org.luckyframework.context.annotation.Import;
import org.luckyframework.context.annotation.PropertySource;
import org.luckyframework.environment.DefaultEnvironment;
import org.luckyframework.environment.Environment;
import org.luckyframework.exception.LuckyIOException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/25 0025 16:21
 */
public class RootBasedAnnotationApplicationContext extends DefaultListableBeanFactory implements ApplicationContext {

    private final static String CLASS_CONF_TEMP = "classpath:%s/**/*.class";
    private final static int basePackageIndex = RootBasedAnnotationApplicationContext.class.getResource("/").toString().length();
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final String basePackage;
    private final Set<Class<?>> componentClasses = new HashSet<>(225);
    private final Set<Class<?>> configurationClasses = new HashSet<>(100);
    private final Set<Class<?>> importClasses = new HashSet<>(20);
    private Environment environment;


    public RootBasedAnnotationApplicationContext(String basePackage){
        Assert.notNull(basePackage,"basePackage is null");
        this.basePackage=basePackage;
        refresh();
    }

    public RootBasedAnnotationApplicationContext(Class<?> baseClass){
        this(baseClass.getPackage().getName());
    }


    public void refresh(){
        scanner();
        initEnvironment();
        loadInternalComponent();
        loadBeanDefinition();
        registeredBeanPostProcessor();
        singletonBeanInitialization();
    }

    private void registeredBeanPostProcessor() {
        String[] names = getBeanNamesForType(BeanPostProcessor.class);
        for (String name : names) {
            registerBeanPostProcessor((BeanPostProcessor) getBean(name));
        }
    }

    private void loadInternalComponent(){
        addInternalComponent(environment);
        addInternalComponent(this);
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
                Class<?> aClass = ClassUtils.getClass(fullClass);
                if(AnnotationUtils.strengthenIsExist(aClass,Configuration.class)){
                    configurationClasses.add(aClass);
                }
                if(AnnotationUtils.strengthenIsExist(aClass, Component.class)){
                    componentClasses.add(aClass);
                    if(AnnotationUtils.strengthenIsExist(aClass, Import.class)){
                        importClasses.add(aClass);
                    }
                }
            }
        } catch (IOException e) {
            throw new LuckyIOException(e);
        }
    }

    public void initEnvironment(){
        Set<PropertySource> propertySources = new HashSet<>();
        for (Class<?> aClass : componentClasses) {
            PropertySource propertySource = AnnotatedElementUtils.findMergedAnnotation(aClass, PropertySource.class);
            if(propertySource != null){
                propertySources.add(propertySource);
            }
        }
        environment = new DefaultEnvironment(propertySources.toArray(new PropertySource[]{}));
    }

    public void loadBeanDefinition(){
        List<BeanDefinitionReader.BeanDefinitionPojo> importBeanDefinitions = new ArrayList<>();
        for (Class<?> configurationClass : componentClasses) {
            ConfigurationBeanDefinitionReader gr = new ConfigurationBeanDefinitionReader(this, environment,this, configurationClass);
            if (gr.conditionJudgeByClass()) {
                List<BeanDefinitionReader.BeanDefinitionPojo> definitions = gr.getBeanDefinitions();
                for (BeanDefinitionReader.BeanDefinitionPojo pojo : definitions) {
                    registerBeanDefinition(pojo.getBeanName(), pojo.getDefinition());
                }
                importBeanDefinitions.addAll(gr.getBeanDefinitionsByImport());
            }
        }
        importBeanDefinitions.forEach(bd->registerBeanDefinition(bd.getBeanName(),bd.getDefinition()));
    }

    @Override
    public void setAware(Object instance) {
        if(instance instanceof Aware){
            if(instance instanceof EnvironmentAware){
                ((EnvironmentAware)instance).setEnvironment(this.environment);
            }
            if(instance instanceof BeanFactoryAware){
                ((BeanFactoryAware)instance).setBeanFactory(this);
            }

            if(instance instanceof ApplicationContextAware){
                ((ApplicationContextAware)instance).setApplicationContext(this);
            }

            if(instance instanceof ResourceLoaderAware){
                ((ResourceLoaderAware)instance).setResourceLoader(this);
            }

            if(instance instanceof ClassLoaderAware){
                ((ClassLoaderAware)instance).setClassLoader(this.getClassLoader());
            }
        }
    }

    @Override
    public Environment getEnvironment() {
        return this.environment;
    }

    @Nullable
    @Override
    public ClassLoader getClassLoader() {
        return resolver.getClassLoader();
    }

    @Override
    public Resource getResource(String location) {
        return resolver.getResource(location);
    }
}
