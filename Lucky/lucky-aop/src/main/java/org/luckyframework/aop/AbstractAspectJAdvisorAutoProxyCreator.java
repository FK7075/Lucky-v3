package org.luckyframework.aop;

import com.lucky.utils.base.Assert;
import com.lucky.utils.proxy.ASMUtil;
import com.lucky.utils.reflect.ClassUtils;
import com.lucky.utils.reflect.MethodUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.luckyframework.aop.aspectj.AspectJExpressionGlobalPointcutManagement;
import org.luckyframework.aop.aspectj.ExpressionGlobalPointcutManagement;
import org.luckyframework.aop.aspectj.advisor.DefaultAdvisor;
import org.luckyframework.aop.exception.PositionExpressionConfigException;
import org.luckyframework.aop.advice.*;
import org.luckyframework.aop.advisor.Advisor;
import org.luckyframework.aop.exception.AopParamsConfigurationException;
import org.luckyframework.aop.proxy.ProxyFactory;
import org.luckyframework.beans.BeanPostProcessor;
import org.luckyframework.beans.Ordered;
import org.luckyframework.beans.aware.ApplicationContextAware;
import org.luckyframework.context.ApplicationContext;
import org.luckyframework.context.annotation.Order;
import org.luckyframework.exception.LuckyIOException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/15 0015 11:57
 */
public abstract class AbstractAspectJAdvisorAutoProxyCreator implements BeanPostProcessor, ApplicationContextAware {

    private final static Class<?> JOIN_POINT_TYPE = JoinPoint.class;
    private final ExpressionGlobalPointcutManagement pointcutManagement;
    private final List<Advisor> advisors = new ArrayList<>(32);
    private ApplicationContext applicationContext;

    public AbstractAspectJAdvisorAutoProxyCreator(){
        pointcutManagement = new AspectJExpressionGlobalPointcutManagement();
    }

    public void registryAdvisor(Advisor advisor) {
        this.advisors.add(advisor);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        try {
            registryAdvisor();
        } catch (IOException e) {
            throw new LuckyIOException("An exception occurred while registering the Advisor",e);
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        //?????????????????????
        if(bean instanceof Advisor){
            return bean;
        }

        Class<?> beanClass = bean.getClass();
        //???JDK?????????bean???????????????
        if(Proxy.isProxyClass(beanClass)){
            return bean;
        }
        //???Cglib?????????bean???????????????
        if(net.sf.cglib.proxy.Proxy.isProxyClass(beanClass)){
            return bean;
        }
        ProxyFactory proxyFactory = getProxyFactory(bean);
        for (Advisor advisor : advisors) {
            if(advisor.getPointcut().matchClass(beanClass)){
                proxyFactory.registryAdvisor(advisor);
            }
        }
        if(Assert.isEmptyCollection(proxyFactory.getAdvisors())){
            return bean;
        }
        return proxyFactory.getProxy();
    }

    public abstract ProxyFactory getProxyFactory(Object target);

    private void registryAdvisor() throws IOException {
        registryUserAdvisor();
        pointcutManagementSetting();
        registryMethodAdvisor();
    }

    // ??????@Aspect????????????@Pointcut?????????????????????
    private void pointcutManagementSetting() {
        String[] aspectBeanNames = applicationContext.getBeanNamesForAnnotation(Aspect.class);
        for (String aspectBeanName : aspectBeanNames) {
            Class<?> aspectClass = applicationContext.getType(aspectBeanName);
            String aspectClassName = aspectClass.getName();
            List<Method> pointcutMethods = ClassUtils.getMethodByAnnotation(aspectClass, org.aspectj.lang.annotation.Pointcut.class);
            for (Method pointcutMethod : pointcutMethods) {
                org.aspectj.lang.annotation.Pointcut pointcut = pointcutMethod.getAnnotation( org.aspectj.lang.annotation.Pointcut.class);
                String prefix = aspectClassName+ ExpressionGlobalPointcutManagement.CONNECTOR+pointcutMethod.getName()+ ExpressionGlobalPointcutManagement.PARENTHESES;
                pointcutManagement.addExpressionPointcut(prefix,pointcut.value());
            }
        }
    }

    // ???????????????@Aspect????????????Advisor
    private void registryMethodAdvisor() throws IOException {
        String[] aspectBeanNames = applicationContext.getBeanNamesForAnnotation(Aspect.class);
        for (String aspectBeanName : aspectBeanNames) {
            Class<?> aspectClass = applicationContext.getType(aspectBeanName);
            Object aspectBean = applicationContext.getBean(aspectBeanName);
            int aspectBeanPriority = Ordered.getPriority(aspectBean);
            List<Method> allMethods = ClassUtils.getAllMethodForClass(aspectClass);
            for (Method method : allMethods) {
                if(method.isAnnotationPresent(Around.class)){
                    registryAroundAdvisor(aspectBeanName,method,aspectBeanPriority);
                    continue;
                }
                if(method.isAnnotationPresent(After.class)){
                    registryAfterAdvisor(aspectBeanName,method,aspectBeanPriority);
                    continue;
                }
                if(method.isAnnotationPresent(AfterThrowing.class)){
                    registryAfterThrowingAdvisor(aspectBeanName,method,aspectBeanPriority);
                    continue;
                }
                if(method.isAnnotationPresent(AfterReturning.class)){
                    registryAfterReturningAdvisor(aspectBeanName,method,aspectBeanPriority);
                    continue;
                }
                if(method.isAnnotationPresent(Before.class)){
                    registryBeforeAdvisor(aspectBeanName,method,aspectBeanPriority);
                }
            }
        }
    }

    // ????????????????????????Advisor
    private void registryUserAdvisor(){
        String[] advisorNames = applicationContext.getBeanNamesForType(Advisor.class);
        for (String advisorName : advisorNames) {
            registryAdvisor(applicationContext.getBean(advisorName,Advisor.class));
        }
    }

    // ?????????????????? @Before
    private void registryBeforeAdvisor(String aspectBeanName, Method beforeMethod,int aspectClassPriority){
        Parameter[] parameters = beforeMethod.getParameters();
        if(parameters.length == 1 && !parameters[0].getType().equals(JOIN_POINT_TYPE)){
            throw new AopParamsConfigurationException("The after-advice parameter configuration is incorrect. The method can have no parameters or one parameter, and the type of the parameter must be 'org.aspectj.lang.JoinPoint'. Error location:'"+beforeMethod+"'");
        }
        final Object[] aspectMethodRunningArgs = new Object[parameters.length];
        Before before = beforeMethod.getAnnotation(Before.class);
        Object aspectBean = applicationContext.getBean(aspectBeanName);
        String standbyExpression = before.value();
        String prefix = aspectBean.getClass().getName()+ ExpressionGlobalPointcutManagement.CONNECTOR+standbyExpression;
        org.luckyframework.aop.pointcut.Pointcut pointcut = pointcutManagement.getPointcutByExpression(prefix,standbyExpression);
        BeforeAdvice advice = (joinPoint)->{
            if(!Assert.isEmptyArray(aspectMethodRunningArgs)){
                aspectMethodRunningArgs[0] = joinPoint;
            }
            MethodUtils.invoke(aspectBean,beforeMethod,aspectMethodRunningArgs);
        };
        registryAdvisor(new DefaultAdvisor(advice,pointcut,getPriority(aspectClassPriority,beforeMethod)));
    }

    // ?????????????????? @After
    private void registryAfterAdvisor(String aspectBeanName, Method afterMethod,int aspectClassPriority){
        Parameter[] parameters = afterMethod.getParameters();
        if(parameters.length ==1 && !parameters[0].getType().equals(JOIN_POINT_TYPE)){
            throw new AopParamsConfigurationException("The after-advice parameter configuration is incorrect. The method can have no parameters or one parameter, and the type of the parameter must be 'org.aspectj.lang.JoinPoint'. Error location:'"+afterMethod+"'");
        }
        final Object[] aspectMethodRunningArgs = new Object[parameters.length];
        After after = afterMethod.getAnnotation(After.class);
        Object aspectBean = applicationContext.getBean(aspectBeanName);
        String standbyExpression = after.value();
        String prefix = aspectBean.getClass().getName()+ ExpressionGlobalPointcutManagement.CONNECTOR+standbyExpression;
        org.luckyframework.aop.pointcut.Pointcut pointcut = pointcutManagement.getPointcutByExpression(prefix,standbyExpression);
        AfterAdvice advice = (joinPoint)->{
            if(!Assert.isEmptyArray(aspectMethodRunningArgs)){
                aspectMethodRunningArgs[0] = joinPoint;
            }
            MethodUtils.invoke(aspectBean,afterMethod,aspectMethodRunningArgs);
        };
        registryAdvisor(new DefaultAdvisor(advice,pointcut,getPriority(aspectClassPriority,afterMethod)));
    }

    // ?????????????????? @AfterReturning
    private void registryAfterReturningAdvisor(String aspectBeanName, Method afterReturningMethod,int aspectClassPriority) throws IOException {
        AfterReturning afterReturning = afterReturningMethod.getAnnotation(AfterReturning.class);
        String resultName = afterReturning.returning();
        returningAndThrowingParamCheck("AfterReturning",resultName,afterReturningMethod);
        List<String> paramNames = ASMUtil.getClassOrInterfaceMethodParamNames(afterReturningMethod);
        Parameter[] parameters = afterReturningMethod.getParameters();
        final Object[] aspectMethodRunningArgs = new Object[parameters.length];
        Object aspectBean = applicationContext.getBean(aspectBeanName);
        String standbyExpression = getNotEmptyString(afterReturning.pointcut(),afterReturning.value());
        if(standbyExpression == null) throw new PositionExpressionConfigException("afterReturning",afterReturningMethod);
        String prefix = aspectBean.getClass().getName()+ ExpressionGlobalPointcutManagement.CONNECTOR+standbyExpression;
        org.luckyframework.aop.pointcut.Pointcut pointcut = pointcutManagement.getPointcutByExpression(prefix,standbyExpression);
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
        registryAdvisor(new DefaultAdvisor(advice,pointcut,getPriority(aspectClassPriority,afterReturningMethod)));
    }

    // ?????????????????? @AfterThrowing
    private void registryAfterThrowingAdvisor(String aspectBeanName, Method afterThrowingMethod,int aspectClassPriority) throws IOException {
        AfterThrowing afterThrowing = afterThrowingMethod.getAnnotation(AfterThrowing.class);
        String resultName = afterThrowing.throwing();
        returningAndThrowingParamCheck("AfterThrowing",resultName,afterThrowingMethod);
        List<String> paramNames = ASMUtil.getClassOrInterfaceMethodParamNames(afterThrowingMethod);
        Parameter[] parameters = afterThrowingMethod.getParameters();
        final Object[] aspectMethodRunningArgs = new Object[parameters.length];
        Object aspectBean = applicationContext.getBean(aspectBeanName);
        String standbyExpression = getNotEmptyString(afterThrowing.pointcut(),afterThrowing.value());
        if(standbyExpression == null) throw new PositionExpressionConfigException("afterReturning",afterThrowingMethod);
        String prefix = aspectBean.getClass().getName()+ ExpressionGlobalPointcutManagement.CONNECTOR+standbyExpression;
        org.luckyframework.aop.pointcut.Pointcut pointcut = pointcutManagement.getPointcutByExpression(prefix,standbyExpression);
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
        registryAdvisor(new DefaultAdvisor(advice,pointcut,getPriority(aspectClassPriority,afterThrowingMethod)));
    }

    // ?????????????????? @Around
    private void registryAroundAdvisor(String aspectBeanName, Method aroundMethod,int aspectClassPriority){
        Parameter[] parameters = aroundMethod.getParameters();
        if(parameters.length == 1 && !JOIN_POINT_TYPE.isAssignableFrom(parameters[0].getType())){
            throw new AopParamsConfigurationException("The around-advice parameter configuration is incorrect. The method can have no parameters or one parameter, and the type of the parameter must be 'org.aspectj.lang.JoinPoint'. Error location:'"+aroundMethod+"'");
        }
        if(aroundMethod.getReturnType() == void.class){
            throw new AopParamsConfigurationException("The around-advice return value was misconfigured. The method must have a return value. Error location:'"+aroundMethod+"'");
        }
        final Object[] aspectMethodRunningArgs = new Object[parameters.length];
        Around before = aroundMethod.getAnnotation(Around.class);
        Object aspectBean = applicationContext.getBean(aspectBeanName);
        String standbyExpression = before.value();
        String prefix = aspectBean.getClass().getName()+ ExpressionGlobalPointcutManagement.CONNECTOR+standbyExpression;
        org.luckyframework.aop.pointcut.Pointcut pointcut = pointcutManagement.getPointcutByExpression(prefix,standbyExpression);
        MethodInterceptor advice = (joinPoint)->{
            if(!Assert.isEmptyArray(aspectMethodRunningArgs)){
                aspectMethodRunningArgs[0] = joinPoint;
            }
            return MethodUtils.invoke(aspectBean,aroundMethod,aspectMethodRunningArgs);
        };
        registryAdvisor(new DefaultAdvisor(advice,pointcut,getPriority(aspectClassPriority,aroundMethod)));
    }

    /*
        ???????????????????????????????????????
        1.str1?????????????????????str1
        2.str2???????????????str2
        3.str1???str2?????????????????????null
    */
    private String getNotEmptyString(String str1,String str2){
        if(!Assert.isBlankString(str1)){
            return str1;
        }
        if(!Assert.isBlankString(str2)){
            return str2;
        }
        return null;
    }

    //todo ????????????????????????
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



    private int getPriority(int classPriority,Method aspectMethod){
        Order order = aspectMethod.getAnnotation(Order.class);
        return order == null ? classPriority : order.value();
    }
}
