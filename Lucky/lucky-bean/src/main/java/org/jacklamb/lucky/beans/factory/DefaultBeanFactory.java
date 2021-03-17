package org.jacklamb.lucky.beans.factory;

import com.lucky.utils.base.Assert;
import com.lucky.utils.reflect.FieldUtils;
import org.jacklamb.lucky.beans.*;
import org.jacklamb.lucky.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/12 0012 18:31
 */
public class DefaultBeanFactory extends DefaultSingletonBeanRegistry {

    private final static Logger log= LoggerFactory.getLogger(DefaultBeanFactory.class);

    @Override
    public Object getBean(String name) throws BeansException {
        return this.doGetBean(name);
    }

    @SuppressWarnings("unchecked")
    protected <T> T doGetBean(String name) throws BeansException {
        Object bean;
        Object sharedInstance = getSingleton(name,true);
        if(sharedInstance != null){
            bean=sharedInstance;
        }else{
            BeanDefinition definition = getBeanDefinition(name);
            Assert.notNull(definition,"can not find the definition of bean '" + name + "'");
            bean = getSingleton(name,()->doCreateBean(name,definition));
        }
        return (T) bean;
//        //判断给入的bean名字不能为空
//        Objects.requireNonNull(name, "beanName不能为空");
//
//        //从单例容器中获取实例，如果存在则直接返回此单例对象
//        Object instance = this.singletonObjects.get(name);
//        if(instance != null){
//            return instance;
//        }
//
//        //从早期对象中获取实例
//        instance = this.earlySingletonObjects.get(name);
//        BeanDefinition beanDefinition = this.getBeanDefinition(name);
//        Objects.requireNonNull(beanDefinition, "`"+name+"`的BeanDefinition为空");
//        if(instance == null ){
//
//            //创建实例
//            instance = doCreateInstance(name,beanDefinition);
//
//            // 设置属性依赖
//            this.setPropertyDIValues(beanDefinition,instance);
//
//            // 执行初始化方法
//            this.doInit(beanDefinition, instance);
//
//            /*
//                实例创建完毕，初始化完毕
//                1.将此对象的早期对象删除
//                2.并将此实例加入到单例对象列表中
//             */
//
//            if (beanDefinition.isSingleton()) {
//                earlySingletonObjects.remove(name);
//                singletonObjects.put(name, instance);
//            }
//            //实例初始化结束后，移除该实例的创建日志
//            singletonsCurrentlyInCreation.remove(name);
//        }
//        return instance;
    }

    private Object doCreateBean(String name,BeanDefinition definition){
        Object bean = createBeanInstance(name, definition);
        boolean earlySingletonExposure = definition.isSingleton() && isSingletonCurrentlyInCreation(name);
        if(earlySingletonExposure){
            addSingletonFactory(name,()->bean);
        }
        Object exposedObject = bean;
        if (earlySingletonExposure) {
            Object earlySingletonReference = getSingleton(name, false);
            if (earlySingletonReference != null) {
                exposedObject = earlySingletonReference;
            }
        }

        populateBean(definition,exposedObject);

        exposedObject=applyPostProcessBeforeInitialization(exposedObject,name);
        doInit(definition,exposedObject);
        exposedObject=applyPostProcessAfterInitialization(exposedObject,name);

        singletonsCurrentlyInCreation.remove(name);
        return exposedObject;
    }

    private Object createBeanInstance(String name,BeanDefinition beanDefinition){
        singletonsCurrentlyInCreation.add(name);
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
        return instance;
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

    // 使用非静态工厂方法创建实例
    private Object createInstanceByFactoryBean(String name,BeanDefinition beanDefinition){
        try {
            Object beanFactory=doGetBean(beanDefinition.getFactoryBeanName());
            Object[] args = getConstructorArgumentValues(beanDefinition);
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
            Object[] args = getConstructorArgumentValues(beanDefinition);
            Method staticFactoryMethod = determineFactoryMethod(beanDefinition, args, beanClass);
            return staticFactoryMethod.invoke(beanClass,args);
        }catch (Exception e){
            throw new BeanCreationException("An exception occurred while creating Bean '"+name+"'. ["+beanDefinition+"]",e);
        }

    }

    // 使用构造方法来构造对象
    private Object createInstanceByConstructor(String name,BeanDefinition beanDefinition) {
        try {
            Object[] args = getConstructorArgumentValues(beanDefinition);
            return determineConstructor(beanDefinition, args).newInstance(args);
        } catch (Exception e) {
            throw new BeanCreationException("An exception occurred while creating Bean '"+name+"'. ["+beanDefinition+"]",e);
        }
    }

    // 设置属性依赖值
    private void populateBean(BeanDefinition diValues,Object instance) {
        List<PropertyValue> propertyValues = diValues.getPropertyValues();
        // 没有属性依赖直接返回
        if(Assert.isEmptyCollection(propertyValues)){
            return;
        }

        Class<?> beanClass = instance.getClass();
        for (PropertyValue ref : propertyValues) {
            Field field = FieldUtils.getDeclaredField(beanClass, ref.getName());
            FieldUtils.setValue(instance,field,getRealValue(ref.getValue()));
        }
    }


    // 根据参数值确定使用的工厂方法(type 为工厂方法所在的类，type为null时使用BeanDefinition中配置的beanCLass)
    private Method determineFactoryMethod(BeanDefinition definition,Object[] args,Class<?> type)
            throws Exception {
        Method method = definition.getFactoryMethod();
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
            if(definition.isPrototype()){
                definition.setFactoryMethod(method);
            }
            return method;
        }else{
            throw new Exception("不存在对应的方法！" + definition);
        }

    }

    // 根据参数值确定使用的构造器
    private Constructor<?> determineConstructor(BeanDefinition definition,Object[] args)
            throws Exception {

        Constructor<?> ct = definition.getConstructor();
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
            if (definition.isPrototype()) {
                definition.setConstructor(ct);
            }
            return ct;
        } else {
            throw new Exception("不存在对应的构造方法！" + definition);
        }

    }

    @Override
    public void close() throws IOException {
        for (String beanName : getBeanDefinitionNames()) {
            BeanDefinition bd = getBeanDefinition(beanName);
            if(bd.isSingleton() && !Assert.isBlankString(bd.getDestroyMethodName())){
                try {
                    Object instance = this.getSingleton(beanName,true);
                    Method destroyMethod = instance.getClass().getMethod(bd.getDestroyMethodName());
                    destroyMethod.invoke(instance);
                } catch (Exception e) {
                    log.error("执行bean[" + beanName + "] " + bd + " 的销毁方法执行异常！", e);
                }
            }
        }
    }
}
