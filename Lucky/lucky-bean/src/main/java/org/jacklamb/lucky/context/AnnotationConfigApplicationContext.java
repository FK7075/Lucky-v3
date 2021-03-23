package org.jacklamb.lucky.context;

import com.lucky.utils.base.ArrayUtils;
import com.lucky.utils.type.ResolvableType;
import org.jacklamb.lucky.exception.BeansException;
import org.jacklamb.lucky.exception.NoSuchBeanDefinitionException;

import java.util.HashSet;
import java.util.Set;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/19 0019 15:15
 */
public class AnnotationConfigApplicationContext extends AbstractApplicationContext {

    private Set<Class<?>> configClasses = new HashSet<>();
    private Set<String> packageNames = new HashSet<>();

    public void setConfigClasses(Set<Class<?>> configClasses) {
        this.configClasses = configClasses;
    }

    public void setPackageNames(Set<String> packageNames) {
        this.packageNames = packageNames;
    }

    public void addConfigClass(Class<?> configClass) {
        this.configClasses.add(configClass);
    }

    public void addPackageName(String packageName) {
        this.packageNames.add(packageName);
    }

    public AnnotationConfigApplicationContext(Class<?>... configClasses){
        super();
        setConfigClasses(ArrayUtils.arrayToSet(configClasses));
        refresh();
    }

    public AnnotationConfigApplicationContext(String... basePackages){
        super();
        setPackageNames(ArrayUtils.arrayToSet(basePackages));
        refresh();
    }

    public void refresh(){

    }

    @Override
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        return beanFactory.getBean(requiredType, args);
    }

    @Override
    public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
        return beanFactory.isTypeMatch(name,typeToMatch);
    }
}
