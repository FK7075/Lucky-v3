package org.luckyframework.beans.factory;

import com.lucky.utils.base.ArrayUtils;
import com.lucky.utils.base.Assert;
import com.lucky.utils.reflect.FieldUtils;
import com.lucky.utils.reflect.MethodUtils;
import com.lucky.utils.type.ResolvableType;
import org.luckyframework.beans.*;
import org.luckyframework.exception.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/23 0023 9:59
 */
public abstract class StandardBeanFactory extends DefaultBeanDefinitionRegistry implements ListableBeanFactory, BeanPostProcessorRegistry {

    private final List<BeanPostProcessor> beanPostProcessors =new ArrayList<>(20);
    //单例池
    private final Map<String,Object> singletonObjects = new ConcurrentHashMap<>(256);
    //实例化但未初始化的早期对象
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);
    //正在创建的对象
    private final Set<String> inCreationCheckExclusions = Collections.newSetFromMap(new ConcurrentHashMap<>(16));
    //缓存bean的类型信息
    private final Map<String,Class<?>> beanTypes = new ConcurrentHashMap<>(256);


    /**
     * 获取所有单例bean的名称
     * @return 所有单例bean的名称
     */
    public String[] getSingletonObjectNames(){
        return singletonObjects.keySet().toArray(EMPTY_STRING_ARRAY);
    }

    //获取bean的实例
    protected Object doGetBean(String name) {
        Object bean = singletonObjects.get(name);
        if(bean == null){
            BeanDefinition definition = getBeanDefinition(name);
            Assert.notNull(definition,"can not find the definition of bean '" + name +"'");
            bean = doCreateBean(name,definition);
        }
        if(NULL_OBJECT.equals(bean)){
            return null;
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
        // 创建bean的实例
        instance = createBeanInstance(name,definition);
        //如果是FactoryBean则调用getObject方法得到真正的组件
        instance = factoryBeanProcess(name,definition,instance);
        // 设置Aware
        setAware(instance);
        // 设置属性
        populateBean(name,instance);
        instance=applyPostProcessBeforeInitialization(name,instance);
        doInit(name,instance);
        instance=applyPostProcessAfterInitialization(name,instance);
        inCreationCheckExclusions.remove(name);
        if(definition.isSingleton()){
            addSingletonObject(name,instance);
        }
        return instance;
    }


    //最后的处理
    private Object factoryBeanProcess(String name, BeanDefinition definition, Object instance) {
        if(instance instanceof FactoryBean){
            try {
                FactoryBean<?> factoryBean = (FactoryBean<?>) instance;
                setAware(factoryBean);
                Object factoryCreateBean = factoryBean.getObject();
                if(!factoryBean.isSingleton()){
                    definition.setScope(BeanScope.PROTOTYPE);
                    removerSingletonObject(name);
                }
                return factoryCreateBean;
            }catch (Exception e){
                throw new BeanCreationException(name,"The bean ('"+name+"') being created is a FactoryBean type, but an exception occurs when using this FactoryBean to create an object",e);
            }
        }
        return instance;
    }

    // 添加单例到单例池
    public void addSingletonObject(String name,Object bean){
        bean = getNotNullInstance(bean);
        singletonObjects.put(name,bean);
        earlySingletonObjects.remove(name);
    }

    public void removerSingletonObject(String name){
        singletonObjects.remove(name);
        earlySingletonObjects.remove(name);
    }

    // 设置属性依赖值
    private void populateBean(String beanName,Object instance) {
        BeanDefinition definition = getBeanDefinition(beanName);
        PropertyValue[] propertyValues = definition.getPropertyValues();
        // 没有属性依赖直接返回
        if(Assert.isEmptyArray(propertyValues)){
            return;
        }
        Class<?> beanClass = instance.getClass();
        for (PropertyValue ref : propertyValues) {
            Field field = FieldUtils.getDeclaredField(beanClass, ref.getName());
            FieldUtils.setValue(instance,field,getRealValue(beanName,ref.getValue()));
        }
    }

    // 初始化
    private void doInit(String beanName, Object instance) {
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        if(instance instanceof InitializingBean){
            try {
                ((InitializingBean)instance).afterPropertiesSet();
            } catch (Exception e) {
                throw new BeanCreationException("An exception occurred when using the 'InitializingBean#afterPropertiesSet()' method to initialize the bean named '"+beanName+"'",e);
            }
        }

        if(!Assert.isBlankString(beanDefinition.getInitMethodName())){
            try {
                Method initMethod = instance.getClass().getMethod(beanDefinition.getInitMethodName());
                initMethod.invoke(instance);
            }catch (Exception e){
                throw new BeanCreationException("An exception occurs when the bean named '"+beanName+"' is initialized using the initialization method the beanDefinition.  ["+beanDefinition+"]",e);
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
            instance = getNotNullInstance(instance);
            earlySingletonObjects.put(name,instance);
        }
        return instance;
    }

    private Object getNotNullInstance(Object instance){
        return instance==null?NULL_OBJECT:instance;
    }

    // 使用非静态工厂方法创建实例
    private Object createInstanceByFactoryBean(String name,BeanDefinition beanDefinition){
        try {
            Object beanFactory=doGetBean(beanDefinition.getFactoryBeanName());
            ConstructorValue[] constructorValues = beanDefinition.getConstructorValues();
            Object[] args = getConstructorArgumentRealValues(name,constructorValueToObject(constructorValues));
            Method factoryMethod =determineFactoryMethod(name,constructorValueToClasses(constructorValues),beanFactory.getClass());
            return factoryMethod.invoke(beanFactory,args);
        }catch (Exception e){
            throw new BeanCreationException("An exception occurred while creating Bean '"+name+"'. ["+beanDefinition+"]",e);
        }
    }

    // 使用静态工厂方法创建实例
    private Object createInstanceByStaticFactoryMethod(String name,BeanDefinition beanDefinition) {
        try {
            Class<?> beanClass = beanDefinition.getBeanClass();
            ConstructorValue[] constructorValues = beanDefinition.getConstructorValues();
            Object[] args = getConstructorArgumentRealValues(name,constructorValueToObject(constructorValues));
            Method staticFactoryMethod = determineFactoryMethod(name, constructorValueToClasses(constructorValues), beanClass);
            return staticFactoryMethod.invoke(beanClass,args);
        }catch (Exception e){
            throw new BeanCreationException("An exception occurred while creating Bean '"+name+"'. ["+beanDefinition+"]",e);
        }

    }

    // 使用构造方法来构造对象
    private Object createInstanceByConstructor(String name,BeanDefinition beanDefinition) {
        try {
            ConstructorValue[] constructorValues = beanDefinition.getConstructorValues();
            Object[] args = getConstructorArgumentRealValues(name,constructorValueToObject(constructorValues));
            if(beanDefinition instanceof GenericBeanDefinition){
                ((GenericBeanDefinition)beanDefinition).setCacheConstructorArgumentRealValues(args);
            }
            return determineConstructor(beanDefinition, constructorValueToClasses(constructorValues)).newInstance(args);
        } catch (Exception e) {
            throw new BeanCreationException("An exception occurred while creating Bean '"+name+"'. ["+beanDefinition+"]",e);
        }
    }


    // 根据参数值确定使用的构造器
    private Constructor<?> determineConstructor(BeanDefinition definition, Class<?>[] constructorClasses)
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
        if(constructorClasses == null){
            ct= beanClass.getConstructor();
        }
        //参数不为null，需要匹配参数类型来获取构造器
        else {
            //使用参数的类型进行精确查找
            try {
                ct=beanClass.getConstructor(constructorClasses);
            }catch (Exception ignored){
                //精确查找找不到结果
            }

            if(ct == null){

                Constructor<?>[] constructors = beanClass.getConstructors();
                out:for (Constructor<?> constructor : constructors) {
                    Class<?>[] parameterTypes = constructor.getParameterTypes();
                    if(constructorClasses.length==parameterTypes.length){
                        for (int i = 0,j= parameterTypes.length; i < j; i++) {
                            if(!parameterTypes[i].isAssignableFrom(constructorClasses[i])){
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
    private Method determineFactoryMethod(String beanName,Class<?>[] argsClasses,Class<?> type)
            throws Exception {
        BeanDefinition definition = getBeanDefinition(beanName);
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
        if(argsClasses == null){
            method=type.getDeclaredMethod(factoryMethodName);
        } else {
            method = getMethodByParamTypes(type, factoryMethodName, argsClasses);
        }
        if(method != null){
            if(definition.isPrototype() && isGbd){
                ((GenericBeanDefinition) definition).setCacheFactoryMethod(method);
            }
            return method;
        }else{
            throw new BeanCreationException(beanName,"No factory method named '"+factoryMethodName+"' was found" + definition);
        }

    }

    private Object[] constructorValueToObject(ConstructorValue[] constructorValues){
        if(constructorValues == null){
            return null;
        }
        Object[] refValues = new Object[constructorValues.length];
        int i = 0;
        for (ConstructorValue constructorValue : constructorValues) {
            refValues[i++] = constructorValue.getValue();
        }
        return refValues;
    }

    private Class<?>[] constructorValueToClasses(ConstructorValue[] constructorValues){
        if(constructorValues == null){
            return null;
        }
        Class<?>[] constructorClasses = new Class<?>[constructorValues.length];
        int i = 0;
        for (ConstructorValue constructorValue : constructorValues) {
            constructorClasses[i++] = constructorValue.getType(this);
        }
        return constructorClasses;
    }

    private Method getMethodByParamTypes(Class<?> type,String methodName,Class<?>[] paramTypes){
        try {
            return type.getMethod(methodName,paramTypes);
        }catch (Exception ignored){

        }
        Method[] methods = type.getMethods();
        out:for (Method m : methods) {
            if(!m.getName().equals(methodName)){
                continue ;
            }
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
    protected Object[] getConstructorArgumentRealValues(String beanName,Object[] refValues) throws BeansException {
        return getRealValues(beanName,refValues);
    }

    //获取构造器参数的真实值，将引用值替换为真实值
    protected Object[] getRealValues(String beanName,Object[] refArgumentValues) throws BeansException {
        //空值
        if(Assert.isEmptyArray(refArgumentValues)){
            return null;
        }
        Object[] values=new Object[refArgumentValues.length];
        int index=0;
        for (Object ref : refArgumentValues) {
            values[index++]=getRealValue(beanName,ref);
        }
        return values;
    }

    //将引用值转化为真实值
    protected Object getRealValue(String beanName,Object ref) throws BeansException {
        if(ref==null){
            return null;
        }else if(ref instanceof BeanReference){
            BeanReference  beanReference = (BeanReference) ref;
            if(beanReference.getAutowire() == Autowire.BY_NAME){
                try {
                    return this.getBean(beanReference.getBeanName());
                }catch (IllegalArgumentException e){
                    if(beanReference.isRequired()){
                        NoSuchBeanDefinitionException ex = new NoSuchBeanDefinitionException(beanReference.getBeanName());
                        throw new BeanCreationException("An exception occurred while creating a bean named '"+beanName+"'",ex);
                    }
                    return null;
                }

            }
            if(beanReference.getAutowire() == Autowire.BY_TYPE){
                String[] forType = getBeanNamesForType(beanReference.getReferenceType(this));
                if(forType.length == 1){
                    return getBean(forType[0]);
                }

                if(forType.length == 0){
                    if(beanReference.isRequired()){
                        NoSuchBeanDefinitionException ex = new NoSuchBeanDefinitionException(beanReference.getReferenceType(this));
                        throw new BeanCreationException("An exception occurred while creating a bean named '"+beanName+"'",ex);
                    }
                    return null;
                }
                if(ArrayUtils.containStr(forType,beanReference.getBeanName())){
                    return this.getBean(beanReference.getBeanName());
                }
                NoUniqueBeanDefinitionException ex = new NoUniqueBeanDefinitionException(ResolvableType.forType(beanReference.getReferenceType(this)), forType);
                throw new BeanCreationException("An exception occurred while creating a bean named '"+beanName+"'",ex);
            }
            return this.getBean(((BeanReference)ref).getBeanName());
        }else if (ref instanceof Object[]) {
            Object[] refArray= (Object[]) ref;
            Object[] targetArray=new Object[refArray.length];
            int i=0;
            for (Object element : refArray) {
                targetArray[i++]=getRealValue(beanName,element);
            }
            return targetArray;

        } else if (ref instanceof Collection) {
            Collection<?> refCollection = (Collection<?>) ref;
            for (Object element : refCollection) {
                getRealValue(beanName,element);
            }
            return refCollection;
        } else if (ref instanceof Properties) {
            // TODO
            return ref;
        } else if (ref instanceof Map) {
            // TODO
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
        Class<?> beanType = beanTypes.get(name);
        if(beanType == null){
            BeanDefinition definition = getBeanDefinition(name);
            Assert.notNull(definition,"Cannot find the bean definition information with the name '"+name+"'");
            Class<?> beanClass = definition.getBeanClass();
            String factoryBeanName = definition.getFactoryBeanName();
            String factoryMethodName = definition.getFactoryMethodName();
            if(beanClass != null){

                //构造器
                if(Assert.isBlankString(factoryBeanName) && Assert.isBlankString(factoryMethodName)){
                    beanType = beanClass;
                }
                //静态工厂方法
                else if(!Assert.isBlankString(factoryMethodName)){
                    Method method = getMethod(beanClass, factoryMethodName, definition.getConstructorValues());
                    //"There is no corresponding method"
                    Assert.notNull(method,"No static factory method matching the name '"+factoryMethodName+"' was found "+definition);
                    beanType = method.getReturnType();
                }
            }
            //工厂方法
            else{
                Class<?> factoryBeanClass = getType(factoryBeanName);
                Method method = getMethod(factoryBeanClass, factoryMethodName, definition.getConstructorValues());
                Assert.notNull(method, "No factory method matching the name '"+factoryMethodName+"' was found "+definition);
                beanType = method.getReturnType();
            }

            //是一个工厂bean
            if(FactoryBean.class.isAssignableFrom(beanType)){
                for (Type genericInterface : beanType.getGenericInterfaces()) {
                    ResolvableType forType = ResolvableType.forType(genericInterface);
                    Class<?> rawClass = forType.getRawClass();
                    if(FactoryBean.class.equals(rawClass)){
                       beanType = forType.resolveGeneric(0);
                        break;
                    }
                }
            }
            beanTypes.put(name,beanType);
        }
        return beanType;
    }

    private Method getMethod(Class<?> aClass,String factoryMethodName,ConstructorValue[] constructorValues){
        if(Assert.isEmptyArray(constructorValues)){
            return MethodUtils.getMethod(aClass,factoryMethodName);
        }
        Class<?>[] types = constructorValueToClasses(constructorValues);
        return getMethodByParamTypes(aClass,factoryMethodName,types);
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
        List<String> equalsNames = new ArrayList<>();
        String[] names = getBeanNamesForType(requiredType);
        for (String name : names) {
            Class<?> beanClass = getType(name);
            if(beanClass.equals(requiredType)){
                equalsNames.add(name);
                matchNames.add(name);
                continue;
            }
            if(requiredType.isAssignableFrom(beanClass)){
                matchNames.add(name);
            }
        }
        if(equalsNames.size() == 1){
            return (T) getBean(equalsNames.get(0));
        }

        if(matchNames.size() == 1){
            return (T) getBean(matchNames.get(0));
        }
        if(equalsNames.size()==0 && matchNames.size()==0){
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
        ConstructorValue[] constructorValues = copy.getConstructorValues();
        if(constructorValues.length != args.length){
            throw new BeansException("An exception occurred when creating the bean using the constructor because the number of parameters you provided did not match the number of parameters required by the constructor.Expected :"+constructorValues.length+" Actual: "+args.length+"");
        }
        int i=0;
        for (ConstructorValue constructorValue : constructorValues) {
            constructorValue.setValue(args[i++]);
        }
        return doCreateBean(name,copy);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        List<String> matchNames = new ArrayList<>();
        List<String> equalsNames = new ArrayList<>();
        String[] names = getBeanNamesForType(requiredType);
        for (String name : names) {
            Class<?> beanClass = getType(name);
            if(beanClass.equals(requiredType)){
                equalsNames.add(name);
                matchNames.add(name);
                continue;
            }
            if(requiredType.isAssignableFrom(beanClass)){
                matchNames.add(name);
            }
        }
        if(equalsNames.size() == 1){
            return (T) getBean(equalsNames.get(0),args);
        }

        if(matchNames.size() == 1){
            return (T) getBean(matchNames.get(0),args);
        }
        if(equalsNames.size()==0 && matchNames.size()==0){
            throw new NoSuchBeanDefinitionException(requiredType);
        }
        throw new NoUniqueBeanDefinitionException(ResolvableType.forType(requiredType),matchNames);
    }

    @Override
    public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        return typeToMatch.isAssignableFrom(getType(name));
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

    public void addInternalComponent(Object internalComponent){
        Class<?> internalComponentClass = internalComponent.getClass();
        String name = internalComponentClass.getName();
        registerBeanDefinition(name,new GenericBeanDefinition(internalComponentClass));
        singletonObjects.put(name,internalComponent);
    }

    protected void clear(){
        beanTypes.clear();
        earlySingletonObjects.clear();
        singletonObjects.clear();
        beanPostProcessors.clear();
    }


}
