package org.jacklamb.lucky.beans.factory;

import com.lucky.utils.base.Assert;
import com.lucky.utils.reflect.FieldUtils;
import org.jacklamb.lucky.beans.BeanDefinition;
import org.jacklamb.lucky.beans.BeanDefinitionRegister;
import org.jacklamb.lucky.beans.BeanReference;
import org.jacklamb.lucky.beans.PropertyValue;
import org.jacklamb.lucky.exception.BeanCurrentlyInCreationException;
import org.jacklamb.lucky.exception.BeanDefinitionRegisterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/12 0012 18:31
 */
public class DefaultBeanFactory implements BeanFactory, BeanDefinitionRegister, Closeable {

    private final static Logger log= LoggerFactory.getLogger(DefaultBeanFactory.class);

    // bean定义信息
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
    // 单例池
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
    // 早期的单例对象
    private final Map<String,Object> earlySingletonObjects = new HashMap<>();
    // 当前正在创建的Bean
    private final ThreadLocal<Set<String>> buildingBeans=new ThreadLocal<>();

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
    public Object getBean(String name) throws Exception {
        return this.doGetBean(name);
    }

    protected Object doGetBean(String name) throws Exception {
        //判断给入的bean名字不能为空
        Objects.requireNonNull(name, "beanName不能为空");

        //从单例容器中获取实例，如果存在则直接返回此单例对象
        Object instance = this.singletonObjects.get(name);
        if(instance != null){
            return instance;
        }

        //从早期对象中获取实例
        instance = this.earlySingletonObjects.get(name);
        BeanDefinition beanDefinition = this.getBeanDefinition(name);
        Objects.requireNonNull(beanDefinition, "`"+name+"`的BeanDefinition为空");
        if(instance == null ){


            //创建实例
            instance = doCreateInstance(name,beanDefinition);

            // 设置属性依赖
            this.setPropertyDIValues(beanDefinition,instance);

            // 执行初始化方法
            this.doInit(beanDefinition, instance);

            /*
                实例创建完毕，初始化完毕
                1.将此对象的早期对象删除
                2.并将此实例加入到单例对象列表中
             */

            if (beanDefinition.isSingleton()) {
                earlySingletonObjects.remove(name);
                singletonObjects.put(name, instance);
            }
            //实例初始化结束后，移除该实例的创建日志
            buildingBeans.get().remove(name);
        }
        return instance;
    }

    private Object doCreateInstance(String name,BeanDefinition beanDefinition) throws Exception {
        // 初始化创建日志
        Set<String> ingBeans = buildingBeans.get();
        if(ingBeans==null){
            ingBeans =new HashSet<>();
            buildingBeans.set(ingBeans);
        }
        //检查循环依赖
        if(ingBeans.contains(name)){
            throw new BeanCurrentlyInCreationException("`"+name + "' 循环依赖！" + ingBeans);
        }

        //添加当前实例的创建记录
        ingBeans.add(name);
        Class<?> beanClass = beanDefinition.getBeanClass();
        Object instance;
        if(beanClass != null){
            //构造器构造
            if(Assert.isBlankString(beanDefinition.getFactoryMethodName())){
                instance = createInstanceByConstructor(beanDefinition);
            }
            //静态工厂方法构造
            else{
                instance = createInstanceByStaticFactoryMethod(beanDefinition);
            }
        }
        //非静态工厂bean方法构造
        else{
            instance = createInstanceByFactoryBean(beanDefinition);
        }
        //将实例化但是还未初始化的早期对象存入缓存
        if(beanDefinition.isSingleton()){
            earlySingletonObjects.put(name,instance);
        }
        return instance;
    }

    // 初始化
    private void doInit(BeanDefinition beanDefinition, Object instance) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if(!Assert.isBlankString(beanDefinition.getInitMethodName())){
            Method initMethod = beanDefinition.getBeanClass().getMethod(beanDefinition.getInitMethodName());
            initMethod.invoke(instance);
        }
    }

    // 使用非静态工厂方法创建实例
    private Object createInstanceByFactoryBean(BeanDefinition beanDefinition) throws Exception {
        Object beanFactory=doGetBean(beanDefinition.getFactoryBeanName());
        Object[] args = getConstructorArgumentValues(beanDefinition);
        Method factoryMethod =determineFactoryMethod(beanDefinition,args,beanFactory.getClass());
        return factoryMethod.invoke(beanFactory,args);
    }

    // 使用静态工厂方法创建实例
    private Object createInstanceByStaticFactoryMethod(BeanDefinition beanDefinition)
            throws Exception {
        Class<?> beanClass = beanDefinition.getBeanClass();
        Object[] args = getConstructorArgumentValues(beanDefinition);
        Method staticFactoryMethod = determineFactoryMethod(beanDefinition, args, beanClass);
        return staticFactoryMethod.invoke(beanClass,args);
    }

    // 使用构造方法来构造对象
    private Object createInstanceByConstructor(BeanDefinition beanDefinition)
            throws Exception {
        try {
            Object[] args = getConstructorArgumentValues(beanDefinition);
            return determineConstructor(beanDefinition, args).newInstance(args);
        } catch (SecurityException e1) {
            log.error("创建bean的实例异常,beanDefinition：" + beanDefinition, e1);
            throw e1;
        }
    }

    // 设置属性依赖值
    private void setPropertyDIValues(BeanDefinition diValues,Object instance) throws Exception {
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


    //获取构造器的执行参数
    private Object[] getConstructorArgumentValues(BeanDefinition beanDefinition) throws Exception {
        return getRealValues(beanDefinition.getConstructorArgumentValues());
    }

    //获取构造器参数的真实值，将引用值替换为真实值
    private Object[] getRealValues(List<?> constructorArgumentValues) throws Exception {
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
    private Object getRealValue(Object ref) throws Exception {
        if(ref==null){
            return null;
        }else if(ref instanceof BeanReference){
            return doGetBean(((BeanReference)ref).getBeanName());
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


    @Override
    public void close() throws IOException {
        for (Map.Entry<String,BeanDefinition> entry:beanDefinitionMap.entrySet()){
            String beanName = entry.getKey();
            BeanDefinition bd = entry.getValue();

            if(bd.isSingleton() && !Assert.isBlankString(bd.getDestroyMethodName())){
                Object instance = this.singletonObjects.get(beanName);
                try {
                    Method destroyMethod = instance.getClass().getMethod(bd.getDestroyMethodName());
                    destroyMethod.invoke(instance);
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e1) {
                    log.error("执行bean[" + beanName + "] " + bd + " 的销毁方法执行异常！", e1);
                }
            }
        }
    }
}
