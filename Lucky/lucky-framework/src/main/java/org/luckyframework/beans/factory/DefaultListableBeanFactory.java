package org.luckyframework.beans.factory;

import com.lucky.utils.annotation.Nullable;
import com.lucky.utils.base.Assert;
import com.lucky.utils.reflect.AnnotationUtils;
import com.lucky.utils.reflect.MethodUtils;
import com.lucky.utils.type.AnnotatedElementUtils;
import org.luckyframework.beans.BeanDefinition;
import org.luckyframework.beans.SupportSortObject;
import org.luckyframework.beans.aware.Aware;
import org.luckyframework.beans.aware.BeanFactoryAware;
import org.luckyframework.beans.aware.ClassLoaderAware;
import org.luckyframework.exception.BeanDisposableException;
import org.luckyframework.exception.BeansException;
import org.luckyframework.exception.NoSuchBeanDefinitionException;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/23 0023 9:51
 */
public class DefaultListableBeanFactory extends StandardBeanFactory {

    // bean类型与名称的映射
    private final Map<Class<?>,String[]> forTypeNamesMap = new ConcurrentHashMap<>(30);
    // 注解与名称的映射
    private final Map<Class<? extends Annotation>,String[]> forAnnotationNamesMap = new ConcurrentHashMap<>(16);
    // 所有单例bean的名称
    private List<String> singletonBeanNames;

    /**
     * 实例化所有的单例bean
     */
    public void singletonBeanInitialization() {
        for (String singletonBeanName : getSingletonBeanNames()) {
            doGetBean(singletonBeanName);
        }
    }

    @Override
    public void removerSingletonObject(String name){
        super.removerSingletonObject(name);
        singletonBeanNames.remove(name);
    }

    /**
     * 获取所有单例bean的名称
     * @return 所有单例bean的名称
     */
    public String[] getSingletonBeanNames(){
        if(singletonBeanNames == null){
            List<SupportSortObject<String>>  sortNames = new ArrayList<>(225);
            for (String definitionName : getBeanDefinitionNames()) {
                BeanDefinition definition = getBeanDefinition(definitionName);
                if(definition.isSingleton() ){
                    sortNames.add(new SupportSortObject<>(definition.getPriority(),definitionName));
                }
            }
            singletonBeanNames=sortNames.stream().sorted(Comparator.comparingInt(SupportSortObject::getPriority)).map(SupportSortObject::getObject).collect(Collectors.toList());
        }
        return singletonBeanNames.toArray(EMPTY_STRING_ARRAY);
    }

    @Override
    public String[] getBeanNamesForType(@Nullable Class<?> type) {
        Assert.notNull(type,"type is null");
        String[] forTypeNames = forTypeNamesMap.get(type);
        if(forTypeNames == null){
            List<String> typeNames =new ArrayList<>();
            String[] definitionNames = getBeanDefinitionNames();
            for (String name : definitionNames) {
                if(isTypeMatch(name,type)){
                    typeNames.add(name);
                }
            }
            forTypeNames = typeNames.toArray(EMPTY_STRING_ARRAY);
            forTypeNamesMap.put(type,forTypeNames);
        }
        return forTypeNames;
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
        String[] forAnnotationNames = forAnnotationNamesMap.get(annotationType);
        if(forAnnotationNames == null){
            List<String> typeNames =new ArrayList<>();
            String[] definitionNames = getBeanDefinitionNames();
            for (String name : definitionNames) {
                if(AnnotationUtils.strengthenIsExist(getType(name),annotationType)){
                    typeNames.add(name);
                }
            }
            forAnnotationNames = typeNames.toArray(new String[]{});
            forAnnotationNamesMap.put(annotationType,forAnnotationNames);
        }

        return forAnnotationNames;
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

    @Override
    public void setAware(Object instance) {
        if(instance instanceof Aware){
            if(instance instanceof BeanFactoryAware){
                ((BeanFactoryAware)instance).setBeanFactory(this);
            }

            if(instance instanceof ClassLoaderAware){
                ((ClassLoaderAware)instance).setClassLoader(this.getClass().getClassLoader());
            }
        }
    }

    @Override
    public void close() throws IOException {
        for (String singletonBeanName : getSingletonBeanNames()) {
            Object bean = getBean(singletonBeanName);
            if(bean instanceof DisposableBean){
                try {
                    ((DisposableBean)bean).destroy();
                } catch (Exception e) {
                    throw new BeanDisposableException("An exception occurred when using the 'DisposableBean#destroy()' destruction method of the bean named '"+singletonBeanName+"'.",e);
                }
            }

            String destroyMethodName = getBeanDefinition(singletonBeanName).getDestroyMethodName();
            if(!Assert.isBlankString(destroyMethodName)){
                try{
                    Method method = MethodUtils.getMethod(bean.getClass(), destroyMethodName);
                    MethodUtils.invoke(bean,method);
                }catch (Exception e){
                    throw new BeanDisposableException("An exception occurred when using the destroy method in the bean definition. bean: '"+singletonBeanName+"'",e);
                }
            }
        }
        clear();
    }
}
