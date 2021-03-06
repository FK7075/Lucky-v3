package org.jacklamb.lucky.beans.factory;

import com.lucky.utils.base.Assert;
import com.lucky.utils.reflect.FieldUtils;
import org.jacklamb.lucky.beans.*;
import org.jacklamb.lucky.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

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
            //???????????????
            if(Assert.isBlankString(beanDefinition.getFactoryMethodName())){
                if(isAbstract){
                    throw new BeansException("Specified class '" + name + "' is an abstract class or interface");
                }
                instance = createInstanceByConstructor(name,beanDefinition);
            }
            //????????????????????????
            else{
                instance = createInstanceByStaticFactoryMethod(name,beanDefinition);
            }
        }
        //???????????????bean????????????
        else{
            instance = createInstanceByFactoryBean(name,beanDefinition);
        }
        return instance;
    }

    // ?????????
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

    // ???????????????????????????????????????
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

    // ????????????????????????????????????
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

    // ?????????????????????????????????
    private Object createInstanceByConstructor(String name,BeanDefinition beanDefinition) {
        try {
            Object[] args = getConstructorArgumentValues(beanDefinition);

            beanDefinition.setConstructorArgumentRealValues(args);
            return determineConstructor(beanDefinition, args).newInstance(args);
        } catch (Exception e) {
            throw new BeanCreationException("An exception occurred while creating Bean '"+name+"'. ["+beanDefinition+"]",e);
        }
    }

    // ?????????????????????
    private void populateBean(BeanDefinition diValues,Object instance) {
        List<PropertyValue> propertyValues = diValues.getPropertyValues();
        // ??????????????????????????????
        if(Assert.isEmptyCollection(propertyValues)){
            return;
        }

        Class<?> beanClass = instance.getClass();
        for (PropertyValue ref : propertyValues) {
            Field field = FieldUtils.getDeclaredField(beanClass, ref.getName());
            FieldUtils.setValue(instance,field,getRealValue(ref.getValue()));
        }
    }


    // ??????????????????????????????????????????(type ??????????????????????????????type???null?????????BeanDefinition????????????beanCLass)
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
            throw new Exception("???????????????????????????" + definition);
        }

    }

    // ???????????????????????????????????????
    private Constructor<?> determineConstructor(BeanDefinition definition,Object[] args)
            throws Exception {

        Constructor<?> ct = definition.getConstructor();
        if(ct != null){
            return ct;
        }

        Class<?> beanClass=definition.getBeanClass();
        //?????????null????????????????????????
        if(args == null){
            ct= beanClass.getConstructor();
        }
        //????????????null?????????????????????????????????????????????
        else {
            Class<?>[] paramTypes=new Class<?>[args.length];
            for (int i = 0 , j=args.length; i < j; i++) {
                paramTypes[i]=args[i].getClass();
            }
            //???????????????????????????????????????
            try {
                ct=beanClass.getConstructor(paramTypes);
            }catch (Exception ignored){
                //???????????????????????????
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

        //???????????????????????????????????????????????????????????????
        if (ct != null) {
            // ????????????bean,????????????????????????????????????????????????????????????????????????BeanDefinition?????????????????????????????????????????????
            // ????????????????????????beanDefinition?????????????????????
            if (definition.isPrototype()) {
                definition.setConstructor(ct);
            }
            return ct;
        } else {
            throw new Exception("?????????????????????????????????" + definition);
        }

    }

    @Override
    public void close() throws IOException {
        super.close();
        for (String singletonName : getSingletonNames()) {
            BeanDefinition bd = getBeanDefinition(singletonName);
            if(Assert.isBlankString(bd.getDestroyMethodName())){
                try {
                    Object instance = getSingleton(singletonName);
                    Method destroyMethod = instance.getClass().getMethod(bd.getDestroyMethodName());
                    destroyMethod.invoke(instance);
                } catch (Exception e) {
                    log.error("??????bean[" + singletonName + "] " + bd + " ??????????????????????????????", e);
                }
            }
        }
        clearSingletonCache();
    }
}
