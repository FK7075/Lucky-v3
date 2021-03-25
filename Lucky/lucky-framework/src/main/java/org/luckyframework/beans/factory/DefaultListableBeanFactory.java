package org.luckyframework.beans.factory;

import com.lucky.utils.annotation.Nullable;
import com.lucky.utils.base.Assert;
import com.lucky.utils.reflect.AnnotationUtils;
import com.lucky.utils.type.AnnotatedElementUtils;
import org.luckyframework.beans.BeanDefinition;
import org.luckyframework.exception.BeansException;
import org.luckyframework.exception.NoSuchBeanDefinitionException;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/23 0023 9:51
 */
public class DefaultListableBeanFactory extends StandardBeanFactory {

    public void singletonBeanInitialization() {
        for (String definitionName : getBeanDefinitionNames()) {
            BeanDefinition definition = getBeanDefinition(definitionName);
            if(definition.isSingleton() && !definition.isLazyInit()){
                getBean(definitionName);
            }
        }
    }

    public String[] getSingletonBeanNames(){
        List<String> names = new ArrayList<>();
        for (String definitionName : getBeanDefinitionNames()) {
            BeanDefinition definition = getBeanDefinition(definitionName);
            if(definition.isSingleton() ){
                names.add(definitionName);
            }
        }
        return names.toArray(new String[]{});
    }

    @Override
    public String[] getBeanNamesForType(@Nullable Class<?> type) {
        Assert.notNull(type,"type is null");
        List<String> typeNames =new ArrayList<>();
        String[] definitionNames = getBeanDefinitionNames();
        for (String name : definitionNames) {
            if(isTypeMatch(name,type)){
                typeNames.add(name);
            }
        }
        return typeNames.toArray(new String[]{});
    }

    @Override
    public String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons) {
        if(includeNonSingletons){
            return getBeanNamesForType(type);
        }
        Assert.notNull(type,"type is null");
        List<String> typeNames =new ArrayList<>();
        String[] definitionNames = getBeanDefinitionNames();
        for (String name : definitionNames) {
            if(getBeanDefinition(name).isSingleton()&&isTypeMatch(name,type)){
                typeNames.add(name);
            }
        }
        return typeNames.toArray(new String[]{});
    }

    @Override
    public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
        List<String> typeNames =new ArrayList<>();
        String[] definitionNames = getBeanDefinitionNames();
        for (String name : definitionNames) {
            if(AnnotationUtils.strengthenIsExist(getType(name),annotationType)){
                typeNames.add(name);
            }
        }
        return typeNames.toArray(new String[]{});
    }

    @Override
    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
        Map<String,T> matchMap = new HashMap<>();
        String[] definitionNames = getBeanDefinitionNames();
        for (String name : definitionNames) {
            if(isTypeMatch(name,type)){
                matchMap.put(name,getBean(name,type));
            }
        }
        return matchMap;
    }

    @Override
    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons) throws BeansException {
        if(includeNonSingletons){
            return getBeansOfType(type);
        }
        Map<String,T> matchMap = new HashMap<>();
        String[] definitionNames = getBeanDefinitionNames();
        for (String name : definitionNames) {
            if(getBeanDefinition(name).isSingleton() && isTypeMatch(name,type)){
                matchMap.put(name,getBean(name,type));
            }
        }
        return matchMap;
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException {
        Map<String, Object> matchMap = new HashMap<>();
        String[] definitionNames = getBeanDefinitionNames();
        for (String name : definitionNames) {
            if(getType(name).isAssignableFrom(annotationType)){
                matchMap.put(name,getBean(name));
            }
        }
        return matchMap;
    }

    @Nullable
    @Override
    public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException {
        return AnnotatedElementUtils.findMergedAnnotation(getType(beanName),annotationType);
    }

}
