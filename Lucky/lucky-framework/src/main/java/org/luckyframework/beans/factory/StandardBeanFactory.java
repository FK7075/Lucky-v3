package org.luckyframework.beans.factory;

import com.lucky.utils.base.Assert;
import com.lucky.utils.reflect.ClassUtils;
import com.lucky.utils.reflect.FieldUtils;
import org.luckyframework.beans.BeanDefinition;
import org.luckyframework.beans.DefaultBeanDefinitionRegister;
import org.luckyframework.beans.GenericBeanDefinition;
import org.luckyframework.beans.PropertyValue;
import org.luckyframework.exception.BeanCreationException;
import org.luckyframework.exception.BeanCurrentlyInCreationException;
import org.luckyframework.exception.BeansException;
import org.luckyframework.exception.NoSuchBeanDefinitionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/23 0023 9:59
 */
public class StandardBeanFactory extends DefaultBeanDefinitionRegister implements BeanFactory {

    //单例池
    private final Map<String,Object> singletonObjects = new ConcurrentHashMap<>(256);
    //实例化但未初始化的早期对象
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);
    //正在创建的对象
    private final Set<String> inCreationCheckExclusions = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    private Object doGetBean(String name) {
        Object bean = singletonObjects.get(name);
        if(bean == null){
            BeanDefinition definition = getBeanDefinition(name);
            Assert.notNull(definition,"can not find the definition of bean '" + name );
            bean = doCreateBean(name,definition);
        }
        return bean;
    }

    private Object doCreateBean(String name, BeanDefinition definition) {
        Object instance = earlySingletonObjects.get(name);
        if(instance != null){
            return instance;
        }
        instance = createBeanInstance(name,definition);
        populateBean(definition,instance);
        doInit(definition,instance);
        inCreationCheckExclusions.remove(name);
        if(definition.isSingleton()){
            singletonObjects.put(name,instance);
            earlySingletonObjects.remove(name);
        }

        return instance;
    }

    // 设置属性依赖值
    private void populateBean(BeanDefinition definition,Object instance) {
        PropertyValue[] propertyValues = definition.getPropertyValues();
        // 没有属性依赖直接返回
        if(Assert.isEmptyArray(propertyValues)){
            return;
        }
        Class<?> beanClass = instance.getClass();
        for (PropertyValue ref : propertyValues) {
            Field field = FieldUtils.getDeclaredField(beanClass, ref.getName());
            FieldUtils.setValue(instance,field,getRealValue(ref.getValue()));
        }
    }

    // 初始化
    private void doInit(BeanDefinition beanDefinition, Object instance) {
        if(!Assert.isBlankString(beanDefinition.getInitMethodName())){
            try {
                Method initMethod = beanDefinition.getBeanClass().getMethod(beanDefinition.getInitMethodName());
                initMethod.invoke(instance);
            }catch (Exception e){
                throw new BeanCreationException("An exception occurred during bean initialization ["+beanDefinition+"]",e);
            }

        }
    }

    private Object createBeanInstance(String name,BeanDefinition beanDefinition){
        // 检测循环依赖
        if(inCreationCheckExclusions.contains(name)){
            throw new BeanCurrentlyInCreationException("Error creating bean with name '"+name+"': Requested bean is currently in creation: Is there an unresolvable circular reference? '"+name+"' ↔ "+inCreationCheckExclusions);
        }
        inCreationCheckExclusions.add(name);
        Class<?> beanClass = beanDefinition.getBeanClass();
        Object instance;
        if(beanClass != null){
            boolean isAbstract = Modifier.isAbstract(beanClass.getModifiers()) || Modifier.isInterface(beanClass.getModifiers());
            //构造器构造
            if(Assert.isBlankString(beanDefinition.getFactoryMethodName())){
                if(isAbstract){
                    throw new BeansException("Specified class '" + name + "' is an abstract class or interface");
                }
                instance = createInstanceByConstructor(name,beanDefinition);
            }
            //静态工厂方法构造
            else{
                instance = createInstanceByStaticFactoryMethod(name,beanDefinition);
            }
        }
        //非静态工厂bean方法构造
        else{
            instance = createInstanceByFactoryBean(name,beanDefinition);
        }
        if(beanDefinition.isSingleton()){
            earlySingletonObjects.put(name,instance);
        }
        return instance;
    }

    // 使用非静态工厂方法创建实例
    private Object createInstanceByFactoryBean(String name,BeanDefinition beanDefinition){
        try {
            Object beanFactory=doGetBean(beanDefinition.getFactoryBeanName());
            Object[] args = getConstructorArgumentRealValues(beanDefinition);
            Method factoryMethod =determineFactoryMethod(beanDefinition,args,beanFactory.getClass());
            return factoryMethod.invoke(beanFactory,args);
        }catch (Exception e){
            throw new BeanCreationException("An exception occurred while creating Bean '"+name+"'. ["+beanDefinition+"]",e);
        }
    }

    // 使用静态工厂方法创建实例
    private Object createInstanceByStaticFactoryMethod(String name,BeanDefinition beanDefinition) {
        try {
            Class<?> beanClass = beanDefinition.getBeanClass();
            Object[] args = getConstructorArgumentRealValues(beanDefinition);
            Method staticFactoryMethod = determineFactoryMethod(beanDefinition, args, beanClass);
            return staticFactoryMethod.invoke(beanClass,args);
        }catch (Exception e){
            throw new BeanCreationException("An exception occurred while creating Bean '"+name+"'. ["+beanDefinition+"]",e);
        }

    }

    // 使用构造方法来构造对象
    private Object createInstanceByConstructor(String name,BeanDefinition beanDefinition) {
        try {
            Object[] args = getConstructorArgumentRealValues(beanDefinition);
            if(beanDefinition instanceof GenericBeanDefinition){
                ((GenericBeanDefinition)beanDefinition).setCacheConstructorArgumentRealValues(args);
            }
            return determineConstructor(beanDefinition, args).newInstance(args);
        } catch (Exception e) {
            throw new BeanCreationException("An exception occurred while creating Bean '"+name+"'. ["+beanDefinition+"]",e);
        }
    }

    // 根据参数值确定使用的构造器
    private Constructor<?> determineConstructor(BeanDefinition definition, Object[] args)
            throws Exception {
        Constructor<?> ct=null;
        boolean isGbd=(definition instanceof GenericBeanDefinition);
        if(isGbd){
            ct = ((GenericBeanDefinition)definition).getCacheConstructor();
        }
        if(ct != null){
            return ct;
        }

        Class<?> beanClass=definition.getBeanClass();
        //参数为null，返回默认构造器
        if(args == null){
            ct= beanClass.getConstructor();
        }
        //参数不为null，需要匹配参数类型来获取构造器
        else {
            Class<?>[] paramTypes=new Class<?>[args.length];
            for (int i = 0 , j=args.length; i < j; i++) {
                paramTypes[i]=args[i].getClass();
            }
            //使用参数的类型进行精确查找
            try {
                ct=beanClass.getConstructor(paramTypes);
            }catch (Exception ignored){
                //精确查找找不到结果
            }

            if(ct == null){

                Constructor<?>[] constructors = beanClass.getConstructors();
                out:for (Constructor<?> constructor : constructors) {
                    Class<?>[] parameterTypes = constructor.getParameterTypes();
                    if(paramTypes.length==parameterTypes.length){
                        for (int i = 0,j= parameterTypes.length; i < j; i++) {
                            if(!parameterTypes[i].isAssignableFrom(paramTypes[i])){
                                continue out;
                            }
                        }
                        ct=constructor;
                        break;
                    }
                }
            }

        }

        //如果找到构造方法了，并且是原型的就缓存起来
        if (ct != null) {
            // 对于原型bean,可以缓存找到的构造方法，方便下次构造实例对象。在BeanDefinition中获取设置所用构造方法的方法。
            // 同时在上面增加从beanDefinition中获取的逻辑。
            if (definition.isPrototype() && isGbd) {
                ((GenericBeanDefinition)definition).setCacheConstructor(ct);
            }
            return ct;
        } else {
            throw new Exception("There is no corresponding construction method" + definition);
        }

    }


    // 根据参数值确定使用的工厂方法(type 为工厂方法所在的类，type为null时使用BeanDefinition中配置的beanCLass)
    private Method determineFactoryMethod(BeanDefinition definition,Object[] args,Class<?> type)
            throws Exception {
        Method method = null;
        boolean isGbd=(definition instanceof GenericBeanDefinition);
        if(isGbd){
            method = ((GenericBeanDefinition)definition).getCacheFactoryMethod();
        }
        if(method!=null){
            return method;
        }
        type=type==null?definition.getBeanClass():type;
        String factoryMethodName=definition.getFactoryMethodName();
        if(args == null){
            method=type.getMethod(factoryMethodName,null);
        }
        else{
            Class<?>[] paramTypes=new Class[args.length];
            for (int i = 0 ,j =args.length; i < j; i++) {
                paramTypes[i]=args[i].getClass();
            }

            try {
                method=type.getMethod(factoryMethodName,paramTypes);
            }catch (Exception ignored){

            }
            if(method == null){
                Method[] methods = type.getMethods();
                out:for (Method m : methods) {
                    Class<?>[] parameterTypes = m.getParameterTypes();
                    if(parameterTypes.length == paramTypes.length){
                        for (int i = 0 ,j= parameterTypes.length; i < j; i++) {
                            if(!parameterTypes[i].isAssignableFrom(paramTypes[i])){
                                continue out;
                            }
                        }
                        method=m;
                        break;
                    }
                }
            }
        }
        if(method != null){
            if(definition.isPrototype() && isGbd){
                ((GenericBeanDefinition) definition).setCacheFactoryMethod(method);
            }
            return method;
        }else{
            throw new Exception("There is no corresponding method" + definition);
        }

    }

    //获取构造器的执行参数
    protected Object[] getConstructorArgumentRealValues(BeanDefinition beanDefinition) throws BeansException {
        return getRealValues(beanDefinition.getConstructorArgumentValues());
    }

    //---------------------------------------------------------------------
    // BeanFactory methods
    //---------------------------------------------------------------------

    @Override
    public Object getBean(String name) throws BeansException {
        return this.doGetBean(name);
    }


    @Override
    public Class<?> getType(String name) throws BeansException {
        return getBean(name).getClass();
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return (T) getBean(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        List<String> matchNames = new ArrayList<>();
        List<Object> matchObjects = new ArrayList<>();
        List<Object> equalsObjects = new ArrayList<>();
        String[] names = getBeanDefinitionNames();
        for (String name : names) {
            Object bean = getBean(name);
            if(bean == null){
                continue;
            }
            Class<?> beanClass = bean.getClass();
            if(beanClass.equals(requiredType)){
                equalsObjects.add(bean);
                matchNames.add(name);
                continue;
            }
            if(requiredType.isAssignableFrom(bean.getClass())){
                matchObjects.add(bean);
                matchNames.add(name);
            }
        }
        if(equalsObjects.size() == 1){
            return (T) equalsObjects.get(0);
        }

        if(matchObjects.size() == 1){
            return (T) matchObjects.get(0);
        }
        if(equalsObjects.size()==0 && matchObjects.size()==0){
            throw new NoSuchBeanDefinitionException(requiredType);
        }
        throw new NoSuchBeanDefinitionException("There are multiple beans matching the type '"+requiredType+"' "+matchNames);
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        BeanDefinition definition = getBeanDefinition(name);
        if(definition == null){
            throw new NoSuchBeanDefinitionException("No definition information found for bean name '"+name+"'");
        }
        if(definition instanceof GenericBeanDefinition){
            ((GenericBeanDefinition)definition).setConstructorArgumentValues(args);
        }
        return doCreateBean(name,definition);
    }

    @Override
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        return null;
    }

    @Override
    public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        Object bean = getBean(name);
        if(bean == null){
            throw new NoSuchBeanDefinitionException("No definition information found for bean name '"+name+"'");
        }
        return typeToMatch.isAssignableFrom(getBean(name).getClass());
    }

    @Override
    public boolean containsBean(String name) {
        return containsBeanDefinition(name);
    }

    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return getBeanDefinition(name).isSingleton();
    }

    @Override
    public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        return getBeanDefinition(name).isPrototype();
    }
}
