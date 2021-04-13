package org.jacklamb.lucky.aop;

import com.lucky.utils.base.Assert;
import com.lucky.utils.proxy.ASMUtil;
import com.lucky.utils.reflect.AnnotationUtils;
import com.lucky.utils.reflect.MethodUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.jacklamb.lucky.aop.aspectj.BasedExpressionGlobalPointcutManagement;
import org.jacklamb.lucky.aop.aspectj.advisor.DefaultAdvisor;
import org.jacklamb.lucky.aop.exception.PositionExpressionConfigException;
import org.luckyframework.aop.advice.*;
import org.luckyframework.aop.advisor.Advisor;
import org.luckyframework.aop.advisor.AdvisorRegistry;
import org.luckyframework.aop.advisor.ProgrammaticAdvisor;
import org.luckyframework.aop.exception.AopParamsConfigurationException;
import org.luckyframework.aop.pointcut.Pointcut;
import org.luckyframework.beans.BeanPostProcessor;
import org.luckyframework.beans.aware.ApplicationContextAware;
import org.luckyframework.context.ApplicationContext;
import org.luckyframework.context.annotation.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import static org.jacklamb.lucky.aop.aspectj.BasedExpressionGlobalPointcutManagement.CONNECTOR;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/13 0013 11:13
 */
public class AspectjAdvisorAutoProxyCreator implements AdvisorRegistry, BeanPostProcessor, ApplicationContextAware {

    private final static Class<?> JOIN_POINT_TYPE = JoinPoint.class;
    private BasedExpressionGlobalPointcutManagement pointcutManagement;
    private final List<Advisor> advisors = new ArrayList<>(32);
    private ApplicationContext applicationContext;

    @Override
    public void registryAdvisor(Advisor advisor) {
        this.advisors.add(advisor);
    }

    @Override
    public List<Advisor> getAdvisors() {
        return this.advisors;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return null;
    }

    private void registryAdvisor(){
        registryProgrammaticAdvisor();


    }

    private void registryMethodAdvisor(){
        String[] aspectBeanNames = applicationContext.getBeanNamesForAnnotation(Aspect.class);
    }

    // 注册编程式增强 ProgrammaticAdvisor
    private void registryProgrammaticAdvisor(){
        String[] programmaticAdvisorNames = applicationContext.getBeanNamesForType(ProgrammaticAdvisor.class);
        for (String programmaticAdvisorName : programmaticAdvisorNames) {
            registryAdvisor(applicationContext.getBean(programmaticAdvisorName,ProgrammaticAdvisor.class));
        }
    }

    // 注册前置增强 @Before
    private void registryBeforeAdvisor(String aspectBeanName, Method beforeMethod){
        Parameter[] parameters = beforeMethod.getParameters();
        if(parameters.length !=1 || !parameters[0].getType().equals(JOIN_POINT_TYPE)){
            throw new AopParamsConfigurationException("The after-advice parameter configuration is incorrect. The method can have no parameters or one parameter, and the type of the parameter must be 'org.aspectj.lang.JoinPoint'. Error location:'"+beforeMethod+"'");
        }
        final Object[] aspectMethodRunningArgs = new Object[parameters.length];
        Before before = beforeMethod.getAnnotation(Before.class);
        Object aspectBean = applicationContext.getBean(aspectBeanName);
        String prefix = aspectBean.getClass().getName()+CONNECTOR+beforeMethod.getName();
        String expression = before.value();
        Pointcut pointcut = pointcutManagement.getPointcutByExpression(prefix,expression);
        BeforeAdvice advice = (joinPoint)->{
            if(!Assert.isEmptyArray(aspectMethodRunningArgs)){
                aspectMethodRunningArgs[0] = joinPoint;
            }
            MethodUtils.invoke(aspectBean,beforeMethod,aspectMethodRunningArgs);
        };
        registryAdvisor(new DefaultAdvisor(advice,pointcut));
    }

    // 注册后置增强 @After
    private void registryAfterAdvisor(String aspectBeanName, Method afterMethod){
        Parameter[] parameters = afterMethod.getParameters();
        if(parameters.length !=1 || !parameters[0].getType().equals(JOIN_POINT_TYPE)){
            throw new AopParamsConfigurationException("The after-advice parameter configuration is incorrect. The method can have no parameters or one parameter, and the type of the parameter must be 'org.aspectj.lang.JoinPoint'. Error location:'"+afterMethod+"'");
        }
        final Object[] aspectMethodRunningArgs = new Object[parameters.length];
        After after = afterMethod.getAnnotation(After.class);
        Object aspectBean = applicationContext.getBean(aspectBeanName);
        String prefix = aspectBean.getClass().getName()+CONNECTOR+afterMethod.getName();
        String expression = after.value();
        Pointcut pointcut = pointcutManagement.getPointcutByExpression(prefix,expression);
        AfterAdvice advice = (joinPoint)->{
            if(!Assert.isEmptyArray(aspectMethodRunningArgs)){
                aspectMethodRunningArgs[0] = joinPoint;
            }
            MethodUtils.invoke(aspectBean,afterMethod,aspectMethodRunningArgs);
        };
        registryAdvisor(new DefaultAdvisor(advice,pointcut));
    }

    // 注册后置增强 @AfterReturning
    private void registryAfterReturningAdvisor(String aspectBeanName, Method afterReturningMethod) throws IOException {
        AfterReturning afterReturning = afterReturningMethod.getAnnotation(AfterReturning.class);
        String resultName = afterReturning.returning();
        returningAndThrowingParamCheck("AfterReturning",resultName,afterReturningMethod);
        List<String> paramNames = ASMUtil.getClassOrInterfaceMethodParamNames(afterReturningMethod);
        Parameter[] parameters = afterReturningMethod.getParameters();
        final Object[] aspectMethodRunningArgs = new Object[parameters.length];
        Object aspectBean = applicationContext.getBean(aspectBeanName);
        String prefix = aspectBean.getClass().getName()+CONNECTOR+afterReturningMethod.getName();
        String expression = getNotEmptyString(afterReturning.pointcut(),afterReturning.value());
        if(expression == null) throw new PositionExpressionConfigException("afterReturning",afterReturningMethod);
        Pointcut pointcut = pointcutManagement.getPointcutByExpression(prefix,expression);
        AfterReturningAdvice advice = (joinPoint, returning) -> {
            for (int i = 0,j =parameters.length ; i < j; i++) {
                if(JOIN_POINT_TYPE.equals(parameters[i].getType())){
                    aspectMethodRunningArgs[i] = joinPoint;
                    continue;
                }
                if(resultName.equals(paramNames.get(i))){
                    aspectMethodRunningArgs[i] = returning;
                    continue;
                }
                aspectMethodRunningArgs[i] = null;
            }
            MethodUtils.invoke(aspectBean,afterReturningMethod,aspectMethodRunningArgs);
        };
        registryAdvisor(new DefaultAdvisor(advice,pointcut));
    }

    // 注册后置增强 @AfterThrowing
    private void registryAfterThrowingAdvisor(String aspectBeanName, Method afterThrowingMethod) throws IOException {
        AfterThrowing afterThrowing = afterThrowingMethod.getAnnotation(AfterThrowing.class);
        String resultName = afterThrowing.throwing();
        returningAndThrowingParamCheck("AfterThrowing",resultName,afterThrowingMethod);
        List<String> paramNames = ASMUtil.getClassOrInterfaceMethodParamNames(afterThrowingMethod);
        Parameter[] parameters = afterThrowingMethod.getParameters();
        final Object[] aspectMethodRunningArgs = new Object[parameters.length];
        Object aspectBean = applicationContext.getBean(aspectBeanName);
        String prefix = aspectBean.getClass().getName()+CONNECTOR+afterThrowingMethod.getName();
        String expression = getNotEmptyString(afterThrowing.pointcut(),afterThrowing.value());
        if(expression == null) throw new PositionExpressionConfigException("afterReturning",afterThrowingMethod);
        Pointcut pointcut = pointcutManagement.getPointcutByExpression(prefix,expression);
        AfterThrowingAdvice advice = (joinPoint, e) -> {
            for (int i = 0,j =parameters.length ; i < j; i++) {
                if(JOIN_POINT_TYPE.equals(parameters[i].getType())){
                    aspectMethodRunningArgs[i] = joinPoint;
                    continue;
                }
                if(resultName.equals(paramNames.get(i))){
                    aspectMethodRunningArgs[i] = e;
                    continue;
                }
                aspectMethodRunningArgs[i] = null;
            }
            MethodUtils.invoke(aspectBean,afterThrowingMethod,aspectMethodRunningArgs);
        };
        registryAdvisor(new DefaultAdvisor(advice,pointcut));
    }

    // 注册后置增强 @Around
    private void registryAroundAdvisor(String aspectBeanName, Method aroundMethod){
        Parameter[] parameters = aroundMethod.getParameters();
        if(parameters.length !=1 || !JOIN_POINT_TYPE.isAssignableFrom(parameters[0].getType())){
            throw new AopParamsConfigurationException("The around-advice parameter configuration is incorrect. The method can have no parameters or one parameter, and the type of the parameter must be 'org.aspectj.lang.JoinPoint'. Error location:'"+aroundMethod+"'");
        }
        if(aroundMethod.getReturnType() == void.class){
            throw new AopParamsConfigurationException("The around-advice return value was misconfigured. The method must have a return value. Error location:'"+aroundMethod+"'");
        }
        final Object[] aspectMethodRunningArgs = new Object[parameters.length];
        Around before = aroundMethod.getAnnotation(Around.class);
        Object aspectBean = applicationContext.getBean(aspectBeanName);
        String prefix = aspectBean.getClass().getName()+CONNECTOR+aroundMethod.getName();
        String expression = before.value();
        Pointcut pointcut = pointcutManagement.getPointcutByExpression(prefix,expression);
        MethodInterceptor advice = (joinPoint)->{
            if(!Assert.isEmptyArray(aspectMethodRunningArgs)){
                aspectMethodRunningArgs[0] = joinPoint;
            }
            return MethodUtils.invoke(aspectBean,aroundMethod,aspectMethodRunningArgs);
        };
        registryAdvisor(new DefaultAdvisor(advice,pointcut));
    }

    private String getNotEmptyString(String str1,String str2){
        if(!Assert.isBlankString(str1)){
            return str1;
        }
        if(!Assert.isBlankString(str2)){
            return str2;
        }
        return null;
    }

    private void returningAndThrowingParamCheck(String type,String rt,Method method){
        Parameter[] parameters = method.getParameters();
        if("AfterReturning".equals(type)){

        }

        // type == AfterThrowing
        else{

        }
    }

    private boolean lengthCheck(String rt,int paramLength){
        if(!Assert.isBlankString(rt)){
            return paramLength == 0 || paramLength == 1 || paramLength ==2;
        }
        return paramLength == 0 || paramLength == 1;
    }


}
