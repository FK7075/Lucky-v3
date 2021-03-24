package org.luckyframework.context;

import com.lucky.utils.base.Assert;
import com.lucky.utils.base.BaseUtils;
import com.lucky.utils.reflect.ClassUtils;
import com.lucky.utils.type.AnnotatedElementUtils;
import org.luckyframework.beans.*;
import org.luckyframework.context.BeanDefinitionReader;
import org.luckyframework.context.annotation.Autowired;
import org.luckyframework.context.annotation.Component;
import org.luckyframework.context.annotation.Lazy;
import org.luckyframework.context.annotation.Scope;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/24 0024 11:43
 */
public class ComponentBeanDefinitionReader implements BeanDefinitionReader {


    protected final Class<?> componentClass;
    protected final Component component;

    public ComponentBeanDefinitionReader(Class<?> componentClass){
        Assert.notNull(componentClass,"class is null");
        Component component = AnnotatedElementUtils.findMergedAnnotation(componentClass, Component.class);
        Assert.notNull(component,"'"+componentClass+"' type is illegal, legal type should be marked by '@org.luckyframework.context.annotation.Component' annotation");
        this.component=component;
        this.componentClass=componentClass;
    }


    protected String getThisBeanName(){
        return Assert.isBlankString(component.value())
                ? BaseUtils.lowercaseFirstLetter(componentClass.getSimpleName())
                : component.value();
    }

    protected BeanDefinition getThisBeanDefinition(){
        GenericBeanDefinition beanDefinition =new GenericBeanDefinition(componentClass);
        beanDefinition.setPropertyValues(getPropertyValues());
        beanDefinition.setBeanScope(getScope());
        beanDefinition.setLazyInit(isLazyInit());
        beanDefinition.setConstructorArgumentValues(getConstructorArgumentValues());
        return beanDefinition;
    }

    protected Object[] getConstructorArgumentValues(){
        Constructor<?>[] constructors = componentClass.getConstructors();
        return null;

    }

    private Object[]getArgumentValuesByConstructor(Constructor<?> constructor){
        Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
        if(Assert.isEmptyArray(constructorParameterTypes)){
            return null;
        }
        Object[] values = new Object[constructorParameterTypes.length];
        for (Class<?> type : constructorParameterTypes) {

        }
        return values;
    }

    protected PropertyValue[] getPropertyValues(){
        List<PropertyValue> propertyValues = new ArrayList<>();
        List<Field> autowiredFiles = ClassUtils.getFieldByStrengthenAnnotation(componentClass, Autowired.class);
        for (Field autowiredFile : autowiredFiles) {
            String fileName = autowiredFile.getName();
            BeanReference br = new BeanReference(fileName,autowiredFile.getType());
            br.setRequired(AnnotatedElementUtils.findMergedAnnotation(autowiredFile,Autowired.class).required());
            PropertyValue pv = new PropertyValue(fileName,br);
            propertyValues.add(pv);
        }
        return propertyValues.toArray(new PropertyValue[]{});
    }

    protected boolean isLazyInit(){
        Lazy lazy = componentClass.getAnnotation(Lazy.class);
        return lazy != null;
    }

    protected BeanScope getScope(){
        Scope scope = componentClass.getAnnotation(Scope.class);
        return scope == null ? BeanScope.SINGLETON : scope.value();
    }

    @Override
    public BeanDefinitionPojo getBeanDefinition() {
        return new BeanDefinitionPojo(getThisBeanName(),getThisBeanDefinition());
    }
}
