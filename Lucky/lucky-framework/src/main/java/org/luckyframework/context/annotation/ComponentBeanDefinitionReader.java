package org.luckyframework.context.annotation;

import com.lucky.utils.base.Assert;
import com.lucky.utils.base.BaseUtils;
import com.lucky.utils.type.AnnotatedElementUtils;
import org.luckyframework.beans.BeanDefinition;
import org.luckyframework.beans.BeanScope;
import org.luckyframework.beans.GenericBeanDefinition;
import org.luckyframework.beans.PropertyValue;
import org.luckyframework.context.BeanDefinitionReader;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/24 0024 11:43
 */
public class ComponentBeanDefinitionReader implements BeanDefinitionReader {


    private final Class<?> componentClass;
    private final Component component;

    public ComponentBeanDefinitionReader(Class<?> componentClass){
        Assert.notNull(componentClass,"class is null");
        Component component = AnnotatedElementUtils.findMergedAnnotation(componentClass, Component.class);
        Assert.notNull(component,"'"+componentClass+"' type is illegal, legal type should be marked by '@org.luckyframework.context.annotation.Component' annotation");
        this.component=component;
        this.componentClass=componentClass;
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitions() {
        Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>(1);
        beanDefinitionMap.put(getThisBeanName(),getThisBeanDefinition());
        return beanDefinitionMap;
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
        return null;
    }

    protected boolean isLazyInit(){
        Lazy lazy = componentClass.getAnnotation(Lazy.class);
        return lazy != null;
    }

    protected BeanScope getScope(){
        Scope scope = componentClass.getAnnotation(Scope.class);
        return scope == null ? BeanScope.SINGLETON : scope.value();
    }

}
