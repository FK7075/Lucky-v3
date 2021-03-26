package org.luckyframework.context;

import com.lucky.utils.annotation.Nullable;
import com.lucky.utils.base.Assert;
import com.lucky.utils.fileload.Resource;
import com.lucky.utils.fileload.resourceimpl.PathMatchingResourcePatternResolver;
import com.lucky.utils.reflect.AnnotationUtils;
import com.lucky.utils.reflect.ClassUtils;
import com.lucky.utils.type.AnnotatedElementUtils;
import org.luckyframework.beans.aware.ApplicationContextAware;
import org.luckyframework.beans.aware.Aware;
import org.luckyframework.beans.aware.BeanFactoryAware;
import org.luckyframework.beans.aware.EnvironmentAware;
import org.luckyframework.beans.factory.DefaultListableBeanFactory;
import org.luckyframework.context.annotation.Component;
import org.luckyframework.context.annotation.Configuration;
import org.luckyframework.context.annotation.PropertySource;
import org.luckyframework.environment.DefaultEnvironment;
import org.luckyframework.environment.Environment;
import org.luckyframework.exception.LuckyIOException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/25 0025 16:21
 */
public class AnnotationPackageScannerApplicationContext extends DefaultListableBeanFactory implements ApplicationContext {

    private final static String CLASS_CONF_TEMP = "classpath:%s/**/*.class";
    private final static int basePackageIndex = AnnotationPackageScannerApplicationContext.class.getResource("/").toString().length();
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final String basePackage;
    private final Set<Class<?>> allClasses = new HashSet<>();
    private Environment environment;


    public AnnotationPackageScannerApplicationContext(String basePackage){
        Assert.notNull(basePackage,"basePackage is null");
        this.basePackage=basePackage;
        refresh();
    }

    public AnnotationPackageScannerApplicationContext(Class<?> baseClass){
        this(baseClass.getPackage().getName());
    }


    public void refresh(){
        scanner();
        initEnvironment();
        loadInternalComponent();
        loadBeanDefinition();
        singletonBeanInitialization();
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
                allClasses.add(ClassUtils.getClass(fullClass));
            }
        } catch (IOException e) {
            throw new LuckyIOException(e);
        }
    }

    public void initEnvironment(){
        Set<PropertySource> propertySources = new HashSet<>();
        for (Class<?> aClass : allClasses) {
            PropertySource propertySource = AnnotatedElementUtils.findMergedAnnotation(aClass, PropertySource.class);
            if(propertySource != null){
                propertySources.add(propertySource);
            }
        }
        environment = new DefaultEnvironment(propertySources.toArray(new PropertySource[]{}));
    }

    public void loadBeanDefinition(){
        for (Class<?> aClass : allClasses) {
            if(AnnotationUtils.strengthenIsExist(aClass, Configuration.class)){
                ConfigurationBeanDefinitionReader gr = new ConfigurationBeanDefinitionReader(this,environment,aClass);
                if(gr.conditionJudgeByClass()){
                    List<BeanDefinitionReader.BeanDefinitionPojo> definitions = gr.getBeanDefinitions();
                    for (BeanDefinitionReader.BeanDefinitionPojo pojo : definitions) {
                        registerBeanDefinition(pojo.getBeanName(),pojo.getDefinition());
                    }
                }
                continue;
            }
            if(AnnotationUtils.strengthenIsExist(aClass, Component.class)){
                ComponentBeanDefinitionReader cr = new ComponentBeanDefinitionReader(this,environment,aClass);
                if(cr.conditionJudgeByClass()){
                    registerBeanDefinition(cr.getBeanDefinition().getBeanName(),cr.getBeanDefinition().getDefinition());
                }
            }
        }
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
