package org.luckyframework.beans.factory;

import com.lucky.utils.annotation.Nullable;
import org.luckyframework.beans.BeanDefinition;
import org.luckyframework.beans.BeanDefinitionRegister;
import org.luckyframework.exception.BeanDefinitionRegisterException;
import org.luckyframework.exception.BeansException;
import org.luckyframework.exception.NoSuchBeanDefinitionException;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/23 0023 9:51
 */
public class DefaultListableBeanFactory implements ListableBeanFactory, BeanDefinitionRegister {




    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionRegisterException {

    }

    public BeanDefinition getBeanDefinition(String beanName) {
        return null;
    }

    public void removeBeanDefinition(String beanName) {

    }

    public Class<?> getType(String name) throws BeansException {
        return null;
    }

    public Object getBean(String name) throws BeansException {
        return null;
    }

    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return null;
    }

    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return null;
    }

    public Object getBean(String name, Object... args) throws BeansException {
        return null;
    }

    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        return null;
    }

    public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        return false;
    }

    public boolean containsBean(String name) {
        return false;
    }

    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return false;
    }

    public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        return false;
    }

    public boolean containsBeanDefinition(String beanName) {
        return false;
    }

    public int getBeanDefinitionCount() {
        return 0;
    }

    public String[] getBeanDefinitionNames() {
        return new String[0];
    }

    public String[] getBeanNamesForType(@Nullable Class<?> type) {
        return new String[0];
    }

    public String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons) {
        return new String[0];
    }

    public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
        return new String[0];
    }

    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
        return null;
    }

    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons) throws BeansException {
        return null;
    }

    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException {
        return null;
    }

    @Nullable
    public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException {
        return null;
    }
}
