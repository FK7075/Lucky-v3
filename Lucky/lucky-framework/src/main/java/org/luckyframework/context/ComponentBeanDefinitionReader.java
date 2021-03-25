package org.luckyframework.context;

import com.lucky.utils.base.Assert;
import com.lucky.utils.base.BaseUtils;
import com.lucky.utils.reflect.ClassUtils;
import com.lucky.utils.type.AnnotatedElementUtils;
import org.luckyframework.beans.*;
import org.luckyframework.context.annotation.*;
import org.luckyframework.exception.BeanCreationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;

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
        beanDefinition.setConstructorValues(getConstructorValues());
        return beanDefinition;
    }

    protected ConstructorValue[] getConstructorValues(){
        Constructor<?>[] constructors = componentClass.getConstructors();
        Constructor<?> constructor;
        //只有一个构造器是默认使用这个唯一的构造器
        if(constructors.length == 1){
            constructor=constructors[0];
        } else{
            List<Constructor<?>> autowiredConstructors = new ArrayList<>();
            for (Constructor<?> cons : constructors) {
                if(cons.isAnnotationPresent(Autowired.class)){
                    autowiredConstructors.add(cons);
                }
            }

            if(autowiredConstructors.isEmpty()){
                throw new BeanCreationException(getThisBeanName(),"In the process of creating the bean, multiple constructors were found, but no constructor annotated by '@Autowired' was found, so Lucky doesn't know which one to use?");
            }

            if(autowiredConstructors.size()!=1){
                throw new BeanCreationException(getThisBeanName(),"Multiple constructors annotated by '@Autowired' were found during the bean creation process, so Lucky didn't know which one to use?");
            }
            constructor = autowiredConstructors.get(0);
        }

        return getValuesByConstructor(constructor);
    }

    private ConstructorValue[] getValuesByConstructor(Constructor<?> constructor){
        Parameter[] parameters = constructor.getParameters();
        //无参构造
        if(parameters.length == 0){
            return null;
        }
        ConstructorValue[] values = new ConstructorValue[parameters.length];
        int i=0;
        BeanReference beanReference;
        for (Parameter parameter : parameters) {
            Qualifier qualifier = AnnotatedElementUtils.findMergedAnnotation(parameter, Qualifier.class);
            Autowired autowired = AnnotatedElementUtils.findMergedAnnotation(parameter, Autowired.class);
            if(qualifier != null){
                String beanName = Assert.isBlankString(qualifier.value())?parameter.getName():qualifier.value();
                beanReference = new BeanReference(beanName);
                boolean required = autowired == null || autowired.required();
                beanReference.setRequired(required);
                values[i++] = new ConstructorValue(beanReference);
                continue;
            }

            if(autowired != null){
                beanReference = new BeanReference(parameter.getName(),parameter.getType());
                beanReference.setRequired(autowired.required());
                values[i++] = new ConstructorValue(beanReference);
                continue;
            }
            values[i++] = new ConstructorValue(parameter.getType(),null);
        }
        return values;
    }

    protected PropertyValue[] getPropertyValues(){
        List<PropertyValue> propertyValues = new ArrayList<>();
        List<Field> autowiredFields = ClassUtils.getFieldByStrengthenAnnotation(componentClass, Autowired.class);
        List<Field> qualifierFields = ClassUtils.getFieldByStrengthenAnnotation(componentClass, Qualifier.class);
        autowiredFields.removeAll(qualifierFields);
        propertyValues.addAll(getPropertyValuesByQualifierField(qualifierFields));
        propertyValues.addAll(getPropertyValuesByAutowiredField(autowiredFields));
        return propertyValues.toArray(new PropertyValue[]{});
    }

    private List<PropertyValue> getPropertyValuesByAutowiredField(Collection<Field> autowiredFields){
        List<PropertyValue> autowiredValues = new ArrayList<>();
        for (Field autowiredField : autowiredFields) {
            String fileName = autowiredField.getName();
            BeanReference br = new BeanReference(fileName,autowiredField.getType());
            br.setRequired(AnnotatedElementUtils.findMergedAnnotation(autowiredField,Autowired.class).required());
            PropertyValue pv = new PropertyValue(fileName,br);
            autowiredValues.add(pv);
        }
        return autowiredValues;
    }

    private List<PropertyValue> getPropertyValuesByQualifierField(Collection<Field> qualifierFields){
        List<PropertyValue> qualifierValues = new ArrayList<>();
        for (Field qualifierField : qualifierFields) {
            String fileName = qualifierField.getName();
            Qualifier qualifier = AnnotatedElementUtils.findMergedAnnotation(qualifierField, Qualifier.class);
            Autowired autowired = AnnotatedElementUtils.findMergedAnnotation(qualifierField, Autowired.class);
            String beanName = Assert.isBlankString(qualifier.value())?qualifierField.getName():qualifier.value();
            boolean required = autowired == null || autowired.required();
            BeanReference br = new BeanReference(beanName);
            br.setRequired(required);
            PropertyValue pv = new PropertyValue(fileName,br);
            qualifierValues.add(pv);
        }
        return qualifierValues;
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
