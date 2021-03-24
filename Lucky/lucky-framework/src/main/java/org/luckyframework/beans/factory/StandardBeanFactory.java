package org.luckyframework.beans.factory;

import com.lucky.utils.base.ArrayUtils;
import com.lucky.utils.base.Assert;
import com.lucky.utils.reflect.ClassUtils;
import com.lucky.utils.reflect.FieldUtils;
import com.lucky.utils.reflect.MethodUtils;
import com.lucky.utils.type.ResolvableType;
import org.luckyframework.beans.*;
import org.luckyframework.exception.*;

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
public abstract class StandardBeanFactory extends DefaultBeanDefinitionRegister implements ListableBeanFactory, BeanPostProcessorRegistry {

    private final List<BeanPostProcessor> beanPostProcessors =new ArrayList<>(20);
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
            if(instance instanceof FactoryBean){
                try {
                    return ((FactoryBean<?>)instance).getObject();
                } catch (Exception e) {
                    throw new BeanCreationException(name,"An exception occurred when creating an object through the factory bean",e);
                }
            }
            return instance;
        }
        instance = createBeanInstance(name,definition);
        populateBean(definition,instance);
        instance=applyPostProcessBeforeInitialization(name,instance);
        doInit(definition,instance);
        instance=applyPostProcessAfterInitialization(name,instance);
        inCreationCheckExclusions.remove(name);
        finalTreatment(name,definition,instance);
        return instance;
    }

    //最后的处理
    private void finalTreatment(String name, BeanDefinition definition, Object instance) {
        if(instance instanceof FactoryBean){
            FactoryBean<?> factoryBean = (FactoryBean<?>) instance;
            if(factoryBean.isSingleton()){
                singletonObjects.put(name,instance);
                earlySingletonObjects.remove(name);
            }
            return;
        }
        if(definition.isSingleton()){
            singletonObjects.put(name,instance);
            earlySingletonObjects.remove(name);
        }
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

    //创建bean的实例
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
        else {
            Class<?>[] paramTypes = new Class[args.length];
            for (int i = 0, j = args.length; i < j; i++) {
                paramTypes[i] = args[i].getClass();
            }
            method = getMethodByParamTypes(type, factoryMethodName, paramTypes);
        }
//
//            try {
//                method=type.getMethod(factoryMethodName,paramTypes);
//            }catch (Exception ignored){
//
//            }
//            if(method == null){
//                Method[] methods = type.getMethods();
//                out:for (Method m : methods) {
//                    Class<?>[] parameterTypes = m.getParameterTypes();
//                    if(parameterTypes.length == paramTypes.length){
//                        for (int i = 0 ,j= parameterTypes.length; i < j; i++) {
//                            if(!parameterTypes[i].isAssignableFrom(paramTypes[i])){
//                                continue out;
//                            }
//                        }
//                        method=m;
//                        break;
//                    }
//                }
//            }
//        }
        if(method != null){
            if(definition.isPrototype() && isGbd){
                ((GenericBeanDefinition) definition).setCacheFactoryMethod(method);
            }
            return method;
        }else{
            throw new Exception("There is no corresponding method" + definition);
        }

    }

    private Method getMethodByParamTypes(Class<?> type,String methodName,Class<?>[] paramTypes){
        try {
            return type.getMethod(methodName,paramTypes);
        }catch (Exception ignored){

        }
        Method[] methods = type.getMethods();
        out:for (Method m : methods) {
            Class<?>[] parameterTypes = m.getParameterTypes();
            if(parameterTypes.length == paramTypes.length){
                for (int i = 0 ,j= parameterTypes.length; i < j; i++) {
                    if(!parameterTypes[i].isAssignableFrom(paramTypes[i])){
                        continue out;
                    }
                }
                return m;
            }
        }
        return null;
    }

    //获取构造器的执行参数
    protected Object[] getConstructorArgumentRealValues(BeanDefinition beanDefinition) throws BeansException {
        return getRealValues(beanDefinition.getConstructorArgumentValues());
    }

    //获取构造器参数的真实值，将引用值替换为真实值
    protected Object[] getRealValues(Object[] refArgumentValues) throws BeansException {
        //空值
        if(Assert.isEmptyArray(refArgumentValues)){
            return null;
        }
        Object[] values=new Object[refArgumentValues.length];
        int index=0;
        for (Object ref : refArgumentValues) {
            values[index++]=getRealValue(ref);
        }
        return values;
    }

    //将引用值转化为真实值
    protected Object getRealValue(Object ref) throws BeansException {
        if(ref==null){
            return null;
        }else if(ref instanceof BeanReference){
            BeanReference  beanReference = (BeanReference) ref;
            if(beanReference.getAutowire() == Autowire.BY_NAME){
                return this.getBean(beanReference.getBeanName());
            }
            if(beanReference.getAutowire() == Autowire.BY_TYPE){
                String[] forType = getBeanNamesForType(beanReference.getType());
                if(forType.length == 1){
                    return getBean(forType[0]);
                }

                if(forType.length == 0){
                    throw new NoSuchBeanDefinitionException(beanReference.getType());
                }
                if(ArrayUtils.containStr(forType,beanReference.getBeanName())){
                    return this.getBean(beanReference.getBeanName());
                }
                throw new NoUniqueBeanDefinitionException(ResolvableType.forType(beanReference.getType()),forType);
            }
            return this.getBean(((BeanReference)ref).getBeanName());
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
            //TODO
            return ref;
        } else if (ref instanceof Map) {
            //TODO
            return ref;
        } else {
            return ref;
        }
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
        BeanDefinition definition = getBeanDefinition(name);
        Class<?> beanClass = definition.getBeanClass();
        String factoryBeanName = definition.getFactoryBeanName();
        String factoryMethodName = definition.getFactoryMethodName();
        if(beanClass != null){
            if(Assert.isBlankString(factoryBeanName) && Assert.isBlankString(factoryMethodName)){
                return beanClass;
            }

            if(!Assert.isBlankString(factoryMethodName)){

            }
        }
        return getBean(name).getClass();
    }

    private Method getMethod(Class<?> aClass,String factoryMethodName,Object[] argumentValues){
        if(Assert.isEmptyArray(argumentValues)){
            return MethodUtils.getMethod(aClass,factoryMethodName);
        }
        Class<?>[] types = new Class<?>[argumentValues.length];
        int i=0;
        for (Object value : argumentValues) {
            if(value instanceof BeanReference){
                types[i++] = ((BeanReference)value).getReferenceType(this);
            }else{
                types[i++] = value.getClass();
            }
        }
        return null;
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
        throw new NoUniqueBeanDefinitionException(ResolvableType.forType(requiredType),matchNames);
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        Object bean = singletonObjects.get(name);
        if(bean != null){
            return bean;
        }
        BeanDefinition definition = getBeanDefinition(name);
        if(definition == null){
            throw new NoSuchBeanDefinitionException("No definition information found for bean name '"+name+"'");
        }
        BeanDefinition copy = definition.copy();
        if(copy instanceof GenericBeanDefinition){
            ((GenericBeanDefinition)copy).setConstructorArgumentValues(args);
        }
        return doCreateBean(name,copy);
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



    //---------------------------------------------------------------------
    // BeanPostProcessorRegistry methods
    //---------------------------------------------------------------------

    @Override
    public void registerBeanPostProcessor(BeanPostProcessor processor) {
        beanPostProcessors.add(processor);
    }

    @Override
    public List<BeanPostProcessor> getBeanPostProcessors() {
        return beanPostProcessors;
    }

    private Object applyPostProcessBeforeInitialization(String beanName,Object bean){
        for (BeanPostProcessor processor : getBeanPostProcessors()) {
            bean = processor.postProcessBeforeInitialization(bean, beanName);
            if(bean == null){
                return null;
            }
        }
        return bean;
    }

    private Object applyPostProcessAfterInitialization(String beanName,Object bean){
        for (BeanPostProcessor processor : getBeanPostProcessors()) {
            bean = processor.postProcessAfterInitialization(bean, beanName);
            if(bean == null){
                return null;
            }
        }
        return bean;
    }


}
