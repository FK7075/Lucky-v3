package org.jacklamb.lucky.beans.factory;

import com.lucky.utils.annotation.Nullable;
import com.lucky.utils.base.Assert;
import com.lucky.utils.base.StringUtils;
import com.lucky.utils.reflect.ClassUtils;
import com.lucky.utils.type.ResolvableType;
import org.jacklamb.lucky.beans.BeanDefinition;
import org.jacklamb.lucky.beans.BeanDefinitionRegister;
import org.jacklamb.lucky.beans.BeanReference;
import org.jacklamb.lucky.beans.aware.BeanFactoryAware;
import org.jacklamb.lucky.beans.postprocessor.BeanPostProcessor;
import org.jacklamb.lucky.exception.BeanDefinitionRegisterException;
import org.jacklamb.lucky.exception.BeansException;
import org.jacklamb.lucky.exception.MultipleMatchExceptions;
import org.jacklamb.lucky.exception.NoSuchBeanDefinitionException;

import java.io.Closeable;
import java.io.Serializable;
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
        Assert.notNull(requiredType, "Required type must not be null");
        return (T) getBeanProvider(ResolvableType.forRawClass(requiredType));
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
        return getBeanDefinitions(name).isSingleton();
    }

    @Override
    public boolean isPrototype(String name) throws BeansException {
        return getBeanDefinitions(name).isPrototype();
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
        NamedBeanHolder<T> namedBean = resolveNamedBean(requiredType, args, nonUniqueAsNull);
        if (namedBean != null) {
            return namedBean.getBeanInstance();
        }
        BeanFactory parent = getParentBeanFactory();
        if (parent instanceof DefaultListableBeanFactory) {
            return ((DefaultListableBeanFactory) parent).resolveBean(requiredType, args, nonUniqueAsNull);
        }
        else if (parent != null) {
            ObjectProvider<T> parentProvider = parent.getBeanProvider(requiredType);
            if (args != null) {
                return parentProvider.getObject(args);
            }
            else {
                return (nonUniqueAsNull ? parentProvider.getIfUnique() : parentProvider.getIfAvailable());
            }
        }
        return null;
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
        Assert.notNull(requiredType, "Required type must not be null");
        return getBeanProvider(ResolvableType.forRawClass(requiredType));
    }

    @Override
    public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
        return new BeanObjectProvider<T>() {
            @Override
            public T getObject() throws BeansException {
                T resolved = resolveBean(requiredType, null, false);
                if (resolved == null) {
                    throw new NoSuchBeanDefinitionException(requiredType);
                }
                return resolved;
            }
            @Override
            public T getObject(Object... args) throws BeansException {
                T resolved = resolveBean(requiredType, args, false);
                if (resolved == null) {
                    throw new NoSuchBeanDefinitionException(requiredType);
                }
                return resolved;
            }
            @Override
            @Nullable
            public T getIfAvailable() throws BeansException {
                return resolveBean(requiredType, null, false);
            }
            @Override
            @Nullable
            public T getIfUnique() throws BeansException {
                return resolveBean(requiredType, null, true);
            }
            @SuppressWarnings("unchecked")
            @Override
            public Stream<T> stream() {
                return Arrays.stream(getBeanNamesForTypedStream(requiredType))
                        .map(name -> (T) getBean(name))
                        .filter(bean -> !(bean instanceof NullBean));
            }
            @SuppressWarnings("unchecked")
            @Override
            public Stream<T> orderedStream() {
                String[] beanNames = getBeanNamesForTypedStream(requiredType);
                if (beanNames.length == 0) {
                    return Stream.empty();
                }
                Map<String, T> matchingBeans = new LinkedHashMap<>(beanNames.length);
                for (String beanName : beanNames) {
                    Object beanInstance = getBean(beanName);
                    if (!(beanInstance instanceof NullBean)) {
                        matchingBeans.put(beanName, (T) beanInstance);
                    }
                }
                Stream<T> stream = matchingBeans.values().stream();
                return stream;
            }
        };
    }

    private interface BeanObjectProvider<T> extends ObjectProvider<T>, Serializable {
    }


    private String[] getBeanNamesForTypedStream(ResolvableType requiredType) {
        return BeanFactoryUtils.beanNamesForTypeIncludingAncestors(this, requiredType);
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


        return StringUtils.toStringArray(result);
    }

    @Override
    public BeanDefinition getBeanDefinitions(String beanName) {
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
        return new HashSet<>();
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
