package org.jacklamb.lucky.beans.factory;

import com.lucky.utils.annotation.Nullable;
import com.lucky.utils.base.Assert;
import com.lucky.utils.base.StringUtils;
import com.lucky.utils.reflect.ClassUtils;
import com.lucky.utils.reflect.MethodUtils;
import com.lucky.utils.type.AnnotationUtils;
import com.lucky.utils.type.ResolvableType;
import org.jacklamb.lucky.beans.BeanDefinition;
import org.jacklamb.lucky.beans.BeanDefinitionRegister;
import org.jacklamb.lucky.beans.BeanReference;
import org.jacklamb.lucky.beans.aware.BeanFactoryAware;
import org.jacklamb.lucky.beans.postprocessor.BeanPostProcessor;
import org.jacklamb.lucky.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/16 0016 16:08
 */
public abstract class DefaultListableBeanFactory
        implements ListableBeanFactory, BeanDefinitionRegister, Closeable {

    private final static Logger logger = LoggerFactory.getLogger(DefaultListableBeanFactory.class);
    // bean定义信息
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
    private final List<BeanPostProcessor> beanPostProcessors =new ArrayList<>(50);
    private volatile boolean configurationFrozen;
    /** Map of singleton and non-singleton bean names, keyed by dependency type. */
    private final Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<>(64);
    /** Map of singleton-only bean names, keyed by dependency type. */
    private final Map<Class<?>, String[]> singletonBeanNamesByType = new ConcurrentHashMap<>(64);
    /** ClassLoader to resolve bean class names with, if necessary. */
    @Nullable
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return (T) getBean(name);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return getBean(requiredType, (Object[]) null);
    }

    @Override
    public Class<?> getType(String name) throws BeansException {
        return getBean(name).getClass();
    }

    @Override
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        Assert.notNull(requiredType, "Required type must not be null");
        Object resolved = resolveBean(ResolvableType.forRawClass(requiredType), args, false);
        if (resolved == null) {
            throw new NoSuchBeanDefinitionException(requiredType);
        }
        return (T) resolved;
    }

    @Override
    public boolean containsBean(String name) {
        return containsBeanDefinition(name);
    }

    @Override
    public boolean isSingleton(String name) throws BeansException {
        return getBeanDefinition(name).isSingleton();
    }

    @Override
    public boolean isPrototype(String name) throws BeansException {
        return getBeanDefinition(name).isPrototype();
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
            throws BeanDefinitionRegisterException {
        Objects.requireNonNull(beanName, "注册bean需要给入beanName");
        Objects.requireNonNull(beanDefinition, "注册bean需要给入beanDefinition");

        if(!beanDefinition.validate()){
            throw new BeanDefinitionRegisterException("名字为[" + beanName + "] 的bean定义不合法：" + beanDefinition);
        }

        this.beanDefinitionMap.put(beanName,beanDefinition);
    }

    @Nullable
    private <T> T resolveBean(ResolvableType requiredType, @Nullable Object[] args, boolean nonUniqueAsNull) {
        return null;
    }


    @Override
    public String[] getBeanNamesForType(ResolvableType type) {
        return getBeanNamesForType(type, true, true);
    }

    @Override
    public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
        Class<?> resolved = type.resolve();
        if (resolved != null && !type.hasGenerics()) {
            return getBeanNamesForType(resolved, includeNonSingletons, allowEagerInit);
        }
        else {
            return doGetBeanNamesForType(type, includeNonSingletons, allowEagerInit);
        }
    }

    @Override
    public String[] getBeanNamesForType(@Nullable Class<?> type) {
        return getBeanNamesForType(type, true, true);
    }

    @Override
    public String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        if (!isConfigurationFrozen() || type == null || !allowEagerInit) {
            return doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, allowEagerInit);
        }
        Map<Class<?>, String[]> cache =
                (includeNonSingletons ? this.allBeanNamesByType : this.singletonBeanNamesByType);
        String[] resolvedBeanNames = cache.get(type);
        if (resolvedBeanNames != null) {
            return resolvedBeanNames;
        }
        resolvedBeanNames = doGetBeanNamesForType(ResolvableType.forRawClass(type), includeNonSingletons, true);
        if (ClassUtils.isCacheSafe(type, getBeanClassLoader())) {
            cache.put(type, resolvedBeanNames);
        }
        return resolvedBeanNames;
    }


    private String[] doGetBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
        List<String> result = new ArrayList<>();
        // Check manually registered singletons too.
        for (String beanName : getBeanDefinitionNames()) {
            try {
                Object beanInstance = getBean(beanName);
                // In case of FactoryBean, match object created by FactoryBean.
                if (beanInstance instanceof FactoryBean) {
                    if ((includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type)) {
                        result.add(beanName);
                        // Match found for this bean: do not match FactoryBean itself anymore.
                        continue;
                    }
                    // In case of FactoryBean, try to match FactoryBean itself next.
                    beanName = FACTORY_BEAN_PREFIX + beanName;
                }
                // Match raw bean instance (might be raw FactoryBean).
                if (isTypeMatch(beanName, type)) {
                    result.add(beanName);
                }
            }
            catch (NoSuchBeanDefinitionException ex) {
                // Shouldn't happen - probably a result of circular reference resolution...
                if (logger.isTraceEnabled()) {
                    logger.trace("Failed to check manually registered singleton with name '" + beanName + "'", ex);
                }
            }
        }

        return StringUtils.toStringArray(result);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return this.beanDefinitionMap.get(beanName);
    }



    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public void removeBeanDefinition(String beanName) {
        this.beanDefinitionMap.remove(beanName);
    }

    @Override
    public void registerBeanPostProcessor(BeanPostProcessor processor) {
        if (processor instanceof BeanFactoryAware){
            ((BeanFactoryAware)processor).setBeanFactory(this);
        }
        this.beanPostProcessors.add(processor);
    }

    @Override
    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }

    @Override
    public Collection<BeanDefinition> getBeanDefinitions() {
        return this.beanDefinitionMap.values();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[0]);
    }

    @Override
    public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
        return false;
    }

    @Override
    public int getBeanDefinitionCount() {
        return beanDefinitionMap.size();
    }

    @Override
    public <T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException {
        return getBeansOfType(type, true, true);
    }


    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException {
        String[] beanNames = getBeanNamesForAnnotation(annotationType);
        Map<String, Object> result = new LinkedHashMap<>(beanNames.length);
        for (String beanName : beanNames) {
            Object beanInstance = getBean(beanName);
            if (!(beanInstance instanceof NullBean)) {
                result.put(beanName, beanInstance);
            }
        }
        return result;
    }

    @Nullable
    @Override
    public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException {

        A ann = null;
        Class<?> beanType = getType(beanName);
        if (beanType != null) {
            ann = AnnotationUtils.findAnnotation(beanType, annotationType);
        }
        return ann;
    }

    public boolean isConfigurationFrozen() {
        return this.configurationFrozen;
    }

    public ClassLoader getBeanClassLoader() {
        return this.beanClassLoader;
    }

    //获取构造器的执行参数
    protected Object[] getConstructorArgumentValues(BeanDefinition beanDefinition) throws BeansException {
        return getRealValues(beanDefinition.getConstructorArgumentValues());
    }

    //获取构造器参数的真实值，将引用值替换为真实值
    protected Object[] getRealValues(List<?> constructorArgumentValues) throws BeansException {
        //空值
        if(Assert.isEmptyCollection(constructorArgumentValues)){
            return null;
        }
        Object[] values=new Object[constructorArgumentValues.size()];
        int index=0;
        for (Object ref : constructorArgumentValues) {
            values[index++]=getRealValue(ref);
        }
        return values;
    }

    //将引用值转化为真实值
    protected Object getRealValue(Object ref) throws BeansException {
        if(ref==null){
            return null;
        }else if(ref instanceof BeanReference){
            return getBean(((BeanReference)ref).getBeanName());
        }else if (ref instanceof Object[]) {
            Object[] refArray= (Object[]) ref;
            Object[] targetArray=new Object[refArray.length];
            int i=0;
            for (Object element : refArray) {
                targetArray[i++]=getRealValue(element);
            }
            return targetArray;

        } else if (ref instanceof Collection) {
            Collection<?> refCollection = (Collection<?>) ref;
            for (Object element : refCollection) {
                getRealValue(element);
            }
            return refCollection;
        } else if (ref instanceof Properties) {
            return ref;
        } else if (ref instanceof Map) {
            return ref;
        } else {
            return ref;
        }
    }

    protected Object applyPostProcessAfterInitialization(Object instance, String beanName) {
        for (BeanPostProcessor postProcessor : beanPostProcessors) {
            instance=postProcessor.postProcessAfterInitialization(instance,beanName);
            if(instance==null){
                return null;
            }
        }
        return instance;

    }

    protected Object applyPostProcessBeforeInitialization(Object instance, String beanName){
        for (BeanPostProcessor postProcessor : beanPostProcessors) {
            instance=postProcessor.postProcessBeforeInitialization(instance,beanName);
            if(instance==null){
                return null;
            }
        }
        return instance;
    }
}
