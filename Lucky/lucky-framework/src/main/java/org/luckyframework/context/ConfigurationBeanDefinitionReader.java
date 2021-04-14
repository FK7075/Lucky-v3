package org.luckyframework.context;

import com.lucky.utils.base.Assert;
import com.lucky.utils.proxy.ASMUtil;
import com.lucky.utils.reflect.ClassUtils;
import com.lucky.utils.type.AnnotatedElementUtils;
import com.lucky.utils.type.AnnotationUtils;
import com.lucky.utils.type.ResolvableType;
import com.lucky.utils.type.StandardMethodMetadata;
import org.luckyframework.beans.BeanDefinitionRegistry;
import org.luckyframework.beans.BeanReference;
import org.luckyframework.beans.ConstructorValue;
import org.luckyframework.beans.GenericBeanDefinition;
import org.luckyframework.context.annotation.*;
import org.luckyframework.environment.Environment;
import org.luckyframework.exception.LuckyIOException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/24 0024 11:22
 */
public class ConfigurationBeanDefinitionReader extends ComponentBeanDefinitionReader {

    public ConfigurationBeanDefinitionReader(ApplicationContext context, Environment environment, BeanDefinitionRegistry registry, Class<?> configurationBeanClass){
        super(context,environment,registry,configurationBeanClass);
    }

    public List<BeanDefinitionPojo> getBeanDefinitions(){
        List<BeanDefinitionPojo> beanDefinitions = super.getBeanDefinitions();
        List<Method> beanMethods = ClassUtils.getMethodByAnnotation(componentClass, Bean.class);
        for (Method method : beanMethods) {
            if (!conditionJudgeByMethod(method)){
                continue;
            }
            Bean beanAnn = AnnotationUtils.findAnnotation(method,Bean.class);
            String beanName = Assert.isBlankString(beanAnn.name())?method.getName():beanAnn.name();
            GenericBeanDefinition bd = new GenericBeanDefinition();
            bd.setFactoryBeanName(getThisBeanName());
            bd.setFactoryMethodName(method.getName());
            bd.setConstructorValues(getConstructorValues(method));
            bd.setPrimary(method.isAnnotationPresent(Primary.class));
            if(!Assert.isBlankString(beanAnn.initMethod())){
                bd.setInitMethodName(beanAnn.initMethod());
            }
            if(!Assert.isBlankString(beanAnn.destroyMethod())){
                bd.setDestroyMethodName(beanAnn.destroyMethod());
            }

            Order order = method.getAnnotation(Order.class);
            if(order != null){
                bd.setPriority(order.value());
            }

            DependsOn dependsOn = method.getAnnotation(DependsOn.class);
            if(dependsOn != null){
                bd.setDependsOn(dependsOn.value());
            }
            Lazy lazy = method.getAnnotation(Lazy.class);
            if(lazy != null){
                bd.setLazyInit(true);
            }
            Scope scope = method.getAnnotation(Scope.class);
            if(scope != null){
                bd.setBeanScope(scope.value());
            }
            beanDefinitions.add(new BeanDefinitionPojo(beanName,bd));
        }
        return beanDefinitions;
    }

    private ConstructorValue[] getConstructorValues(Method method){
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0){
            return null;
        }
        List<String> paramNames;
        try {
            paramNames = ASMUtil.getClassOrInterfaceMethodParamNames(method);
        } catch (IOException e) {
           throw new LuckyIOException(e);
        }

        Type[] genericParameterTypes = method.getGenericParameterTypes();
        ConstructorValue[] constructorValues = new ConstructorValue[parameters.length];
        BeanReference beanReference;
        for (int i = 0,j=parameters.length ; i < j; i++) {
            String parameterName = paramNames.get(i);
            Class<?> paramType = parameters[i].getType();
            Qualifier qualifier = AnnotatedElementUtils.findMergedAnnotation(parameters[i], Qualifier.class);
            Autowired autowired = AnnotatedElementUtils.findMergedAnnotation(parameters[i], Autowired.class);
            Value value = AnnotatedElementUtils.findMergedAnnotation(parameters[i], Value.class);
            if(qualifier != null){
                String beanName = Assert.isBlankString(qualifier.value())?parameterName:qualifier.value();
                beanReference = new BeanReference(beanName);
                boolean required = autowired == null || autowired.required();
                beanReference.setRequired(required);
                constructorValues[i] = new ConstructorValue(beanReference);
                continue;
            }

            if(autowired != null){
                beanReference = new BeanReference(parameterName,paramType);
                beanReference.setRequired(autowired.required());
                constructorValues[i] = new ConstructorValue(beanReference);
                continue;
            }

            if(value != null){
                constructorValues[i] = new ConstructorValue(paramType,getRealValue(ResolvableType.forType(genericParameterTypes[i]),value));
                continue;
            }

            if(!ClassUtils.isJdkType(paramType)){
                constructorValues[i] = new ConstructorValue(new BeanReference(parameterName,paramType));
                continue;
            }
            constructorValues[i] = new ConstructorValue(paramType,null);
        }
        return constructorValues;
    }

    public boolean conditionJudgeByMethod(Method beanMethod){
        Conditional conditional = AnnotatedElementUtils.findMergedAnnotation(beanMethod, Conditional.class);
        if(conditional == null){
            return true;
        }

        Class<? extends Condition>[] conditionClasses = conditional.value();
        for (Class<? extends Condition> conditionClass : conditionClasses) {
            Condition condition = ClassUtils.newObject(conditionClass);
            boolean matches = condition.matches(getConditionContext(), new StandardMethodMetadata(beanMethod));
            if(!matches){
                return false;
            }
        }
        return true;
    }
}
