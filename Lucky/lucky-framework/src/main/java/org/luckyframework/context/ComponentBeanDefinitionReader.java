package org.luckyframework.context;

import com.lucky.utils.base.Assert;
import com.lucky.utils.base.BaseUtils;
import com.lucky.utils.conversion.JavaConversion;
import com.lucky.utils.reflect.ClassUtils;
import com.lucky.utils.type.AnnotatedElementUtils;
import com.lucky.utils.type.AnnotationMetadata;
import com.lucky.utils.type.ResolvableType;
import com.lucky.utils.type.StandardAnnotationMetadata;
import org.luckyframework.beans.*;
import org.luckyframework.context.annotation.*;
import org.luckyframework.environment.Environment;
import org.luckyframework.environment.EnvironmentCapable;
import org.luckyframework.exception.BeanCreationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/24 0024 11:43
 */
public class ComponentBeanDefinitionReader implements BeanDefinitionReader, EnvironmentCapable {


    protected final Class<?> componentClass;
    protected final Component component;
    protected final Environment environment;
    protected final ApplicationContext applicationContext;

    public ComponentBeanDefinitionReader(ApplicationContext context,Environment environment,Class<?> componentClass){
        Assert.notNull(componentClass,"class is null");
        Component component = AnnotatedElementUtils.findMergedAnnotation(componentClass, Component.class);
        Assert.notNull(component,"'"+componentClass+"' type is illegal, legal type should be marked by '@org.luckyframework.context.annotation.Component' annotation");
        this.component=component;
        this.componentClass=componentClass;
        this.environment=environment;
        this.applicationContext = context;
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
        Type[] genericParameterTypes = constructor.getGenericParameterTypes();
        //无参构造
        if(parameters.length == 0){
            return null;
        }
        ConstructorValue[] values = new ConstructorValue[parameters.length];
        int i=0;
        BeanReference beanReference;

        for (Parameter parameter : parameters) {
            Class<?> parameterType = parameter.getType();
            String parameterName = parameter.getName();
            Qualifier qualifier = AnnotatedElementUtils.findMergedAnnotation(parameter, Qualifier.class);
            Autowired autowired = AnnotatedElementUtils.findMergedAnnotation(parameter, Autowired.class);
            Value value = AnnotatedElementUtils.findMergedAnnotation(parameter, Value.class);
            if(qualifier != null){
                String beanName = Assert.isBlankString(qualifier.value())?parameterName:qualifier.value();
                beanReference = new BeanReference(beanName);
                boolean required = autowired == null || autowired.required();
                beanReference.setRequired(required);
                values[i] = new ConstructorValue(beanReference);
                i++;
                continue;
            }

            if(autowired != null){
                beanReference = new BeanReference(parameterName,parameterType);
                beanReference.setRequired(autowired.required());
                values[i] = new ConstructorValue(beanReference);
                i++;
                continue;
            }

            if(value != null){
                values[i] = new ConstructorValue(parameterType,getRealValue(ResolvableType.forType(genericParameterTypes[i]),value));
                i++;
                continue;
            }

            if(!ClassUtils.isJdkType(parameterType)){
                values[i] = new ConstructorValue(new BeanReference(parameterName,parameterType));
                i++;
                continue;
            }
            values[i] = new ConstructorValue(parameterType,null);
            i++;
        }
        return values;
    }

    protected PropertyValue[] getPropertyValues(){
        List<PropertyValue> propertyValues = new ArrayList<>();
        List<Field> autowiredFields = ClassUtils.getFieldByStrengthenAnnotation(componentClass, Autowired.class);
        List<Field> qualifierFields = ClassUtils.getFieldByStrengthenAnnotation(componentClass, Qualifier.class);
        List<Field> valueFields = ClassUtils.getFieldByStrengthenAnnotation(componentClass, Value.class);
        valueFields.remove(autowiredFields);
        valueFields.remove(qualifierFields);
        autowiredFields.removeAll(qualifierFields);
        propertyValues.addAll(getPropertyValuesByQualifierField(qualifierFields));
        propertyValues.addAll(getPropertyValuesByAutowiredField(autowiredFields));
        propertyValues.addAll(getPropertyValuesByValueField(valueFields));
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

    private List<PropertyValue> getPropertyValuesByValueField(Collection<Field> valueFields){
        List<PropertyValue> values = new ArrayList<>();
        for (Field valueField : valueFields) {
            String fileName = valueField.getName();
            Value value = AnnotatedElementUtils.findMergedAnnotation(valueField, Value.class);
            Object fileValue = getRealValue(ResolvableType.forField(valueField),value);
            values.add(new PropertyValue(fileName,fileValue));
        }
        return values;
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

    @SuppressWarnings("all")
    public Object getRealValue(ResolvableType rtype,Value value){
        Class<?> type = rtype.getRawClass();
        String exp = value.value();
        Object confValue = environment.parsing(exp);
        if(confValue==null){
            return null;
        }
        //Class类型
        if(Class.class==type){
            return ClassUtils.getClass(confValue.toString());
        }
        //基本类型以及基本类型的包装类型
        if(ClassUtils.isPrimitive(type)||ClassUtils.isSimple(type)){
            return JavaConversion.strToBasic(confValue.toString(), type);
        }

        //基本类型以及其包装类型的数组
        if(ClassUtils.isSimpleArray(type)){
            List<String> confList= (List<String>) confValue;
            return JavaConversion.strArrToBasicArr(listToArrayByStr(confList), type);
        }

        //非JDK类型
        if(!ClassUtils.isJdkType(type)){
//            if(JexlEngineUtil.isExpression(exp)){
//                setField(bean,valueField,getFieldObject(yaml,valueField,fieldType,exp,delimiter));
//            }
            return null;
        }


        //集合类型
        if(Collection.class.isAssignableFrom(type)){
            Class<?> genericType = rtype.resolveGeneric(0);

            //泛型为基本类型
            if(ClassUtils.isSimple(genericType)){
                List<String> confList= (List<String>) confValue;
                String[] confArr=listToArrayByStr(confList);
                if(List.class.isAssignableFrom(type)){
                    return Stream.of(JavaConversion.strArrToBasicArr(confArr,genericType)).collect(Collectors.toList());
                }
                if(Set.class.isAssignableFrom(type)){
                    return Stream.of(JavaConversion.strArrToBasicArr(confArr,genericType)).collect(Collectors.toSet());
                }
                return null;
            }

            //泛型为非JDK类型
            if(!ClassUtils.isJdkType(genericType)){
//                if(!JexlEngineUtil.isExpression(exp)){
//                    return null;
//                }
//                List<Object> confList= (List<Object>) confValue;
//                List<Object> confValueList=new ArrayList<>(confList.size());
//                String listExp=exp.substring(2,exp.length()-1).trim();
//                for(int i=0,j=confList.size();i<j;i++){
//                    String pre=listExp+".get("+i+")";
//                    confValueList.add(getFieldObject(yaml,valueField,genericType,pre,delimiter));
//                }
//                setField(bean,valueField,confValueList);
                return null;
            }

            //泛型为Class
            if(Class.class==genericType){
                List<String> confList= (List<String>) confValue;
                Class<?>[] classes=new Class[confList.size()];
                for (int i = 0,j=classes.length; i < j; i++) {
                    classes[i]=ClassUtils.getClass(environment.parsing(confList.get(i)).toString());
                }
                if(List.class.isAssignableFrom(type)){
                    return  Stream.of(classes).collect(Collectors.toList());
                }
                if(Set.class.isAssignableFrom(type)){
                    return Stream.of(classes).collect(Collectors.toSet());
                }
                return null;
            }
        }

        if(Map.class.isAssignableFrom(type)){
            Map<String,Object> $confMap= (Map<String, Object>) confValue;
            Map<String,Object> confMap=new HashMap<>();
            for(Map.Entry<String,Object> entry:$confMap.entrySet()){
                confMap.put(entry.getKey(),environment.parsing(entry.getValue()));
            }
            return confMap;
        }
        return ClassUtils.newObject(environment.parsing(confValue).toString());
    }

    private String[] listToArrayByStr(List<?> list){
        List<String> strList = list.stream().map(Object::toString).collect(Collectors.toList());
        return strList.toArray(new String[]{});
    }

    @Override
    public Environment getEnvironment() {
        return this.environment;
    }

    public boolean conditionJudgeByClass(){
        Conditional conditional = AnnotatedElementUtils.findMergedAnnotation(componentClass, Conditional.class);
        if(conditional == null){
            return true;
        }
        Class<? extends Condition>[] conditionClasses = conditional.value();
        for (Class<? extends Condition> conditionClass : conditionClasses) {
            Condition condition = ClassUtils.newObject(conditionClass);
            boolean matches = condition.matches(getConditionContext(), AnnotationMetadata.introspect(componentClass));
            if(!matches){
                return false;
            }
        }
        return true;
    }

    protected ConditionContext getConditionContext(){
        return new ConditionContextImpl(environment,applicationContext);
    }
}
