package org.luckyframework.aop.proxy;

import com.lucky.utils.base.Assert;
import com.lucky.utils.reflect.AnnotationUtils;
import com.lucky.utils.reflect.ClassUtils;
import com.lucky.utils.reflect.FieldUtils;
import com.lucky.utils.reflect.MethodUtils;
import net.sf.cglib.proxy.MethodProxy;
import org.luckyframework.aop.advice.Advice;
import org.luckyframework.aop.advisor.Advisor;
import org.luckyframework.context.annotation.SupportNestedProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/7 0007 14:17
 */
public class AopProxyUtils {

    public static Object applyAdvices(Object proxy, Object target, Method method,Object[] args,
                                      List<Advisor> matchAdvisor) throws Throwable{
        List<Advice> advices = getShouldApplyAdvices(target.getClass(),method,args,matchAdvisor);
        if(Assert.isEmptyCollection(advices)){
            return MethodUtils.invoke(target,method,args);
        }else{
            AopAdviceChainInvocation chain = new AopAdviceChainInvocation(proxy,target,method,args,advices);
            return chain.invoke();
        }
    }

    // 生产支持嵌套代理的代理对象
    public static Object applySupportNestingAdvices(Object proxy, Object target, Method method,
                                                    MethodProxy methodProxy,Object[] args, List<Advisor> matchAdvisor) throws Throwable {
        List<Advice> advices = getShouldApplyAdvices(target.getClass(),method,args,matchAdvisor);
        if(Assert.isEmptyCollection(advices)){
            return methodProxy.invokeSuper(proxy,args);
        }else{
            AopAdviceChainInvocation chain = new AopAdviceChainInvocation(proxy,target,method,methodProxy,args,advices);
            return chain.invoke();
        }
    }

    private static List<Advice> getShouldApplyAdvices(Class<?> targetClass, Method method,Object[] args,
                                                     List<Advisor> matchAdvisors){
        if (Assert.isEmptyCollection(matchAdvisors)) {
            return null;
        }
        List<Advice> advices = new ArrayList<>();
        for (Advisor ad : matchAdvisors) {
            if(ad.getPointcut().matchMethod(targetClass,method,args)){
                advices.add(ad.getAdvice());
            }
        }
        return advices;
    }

    public static void fieldCopy(Object proxy,Object target){
        Field[] allFields = ClassUtils.getAllFields(target.getClass());
        for (Field field : allFields) {
            FieldUtils.setValue(proxy,field,FieldUtils.getValue(target,field));
        }
    }

    public static boolean supportNestedProxy(Object target){
        return AnnotationUtils.strengthenIsExist(target.getClass(), SupportNestedProxy.class);
    }

}
