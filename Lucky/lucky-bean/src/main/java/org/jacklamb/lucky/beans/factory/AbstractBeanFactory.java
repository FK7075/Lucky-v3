package org.jacklamb.lucky.beans.factory;

import com.lucky.utils.base.Assert;
import org.jacklamb.lucky.beans.BeanDefinition;
import org.jacklamb.lucky.beans.BeanDefinitionRegister;
import org.jacklamb.lucky.beans.BeanReference;
import org.jacklamb.lucky.beans.postprocessor.BeanPostProcessor;
import org.jacklamb.lucky.beans.postprocessor.BeanPostProcessorRegistry;
import org.jacklamb.lucky.exception.BeanDefinitionRegisterException;
import org.jacklamb.lucky.exception.MultipleMatchExceptions;
import org.jacklamb.lucky.exception.NoSuchBeanDefinitionException;

import java.io.Closeable;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/16 0016 16:08
 */
public abstract class AbstractBeanFactory
        implements BeanFactory, BeanDefinitionRegister, BeanPostProcessorRegistry, Closeable {

    // bean定义信息
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
    private final List<BeanPostProcessor> beanPostProcessorList=new ArrayList<>(50);


    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<T> requiredType) throws Exception {
        return (T) getBean(name);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws Exception {
        Set<String> names = getBeanDefinitionNames();
        List<T> beans=new ArrayList<>();
        for (String name : names) {
            try {
                T instance = getBean(name,requiredType);
                beans.add(instance);
            }catch (Exception ignored){}
        }
        int count = beans.size();
        if(count == 1) return beans.get(0);
        if(count == 0) throw  new NoSuchBeanDefinitionException(requiredType);
        int modifiers = requiredType.getModifiers();
        if(Modifier.isAbstract(modifiers)||Modifier.isInterface(modifiers)){
            throw new MultipleMatchExceptions(requiredType);
        }
        beans.removeIf(b->!(b.getClass().equals(requiredType)));
        count=beans.size();
        if(count == 1) return beans.get(0);
        throw new MultipleMatchExceptions(requiredType);
    }

    @Override
    public Class<?> getType(String name) throws Exception {
        return getBean(name).getClass();
    }

    @Override
    public boolean containsBean(String name) {
        return containsBeanDefinition(name);
    }

    @Override
    public boolean isSingleton(String name) throws Exception {
        return getBeanDefinition(name).isSingleton();
    }

    @Override
    public boolean isPrototype(String name) throws Exception {
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
        this.beanPostProcessorList.add(processor);
    }

    @Override
    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessorList;
    }

    @Override
    public Collection<BeanDefinition> getBeanDefinition() {
        return this.beanDefinitionMap.values();
    }

    @Override
    public Set<String> getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet();
    }

    //获取构造器的执行参数
    protected Object[] getConstructorArgumentValues(BeanDefinition beanDefinition) throws Exception {
        return getRealValues(beanDefinition.getConstructorArgumentValues());
    }

    //获取构造器参数的真实值，将引用值替换为真实值
    protected Object[] getRealValues(List<?> constructorArgumentValues) throws Exception {
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
    protected Object getRealValue(Object ref) throws Exception {
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
}
