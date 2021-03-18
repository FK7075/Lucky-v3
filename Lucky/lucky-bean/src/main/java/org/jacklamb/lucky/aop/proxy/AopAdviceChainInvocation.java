package org.jacklamb.lucky.aop.proxy;

import com.lucky.utils.reflect.MethodUtils;
import org.jacklamb.lucky.aop.advice.AfterReturningAdvice;
import org.jacklamb.lucky.aop.advice.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 将增强组成执行链
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 15:30
 */
public class AopAdviceChainInvocation {

    private static Method invokeMethod;
    private Object proxy;
    private Object target;
    private Method method;
    private Object[] args;
    private List<Object> advices;
    private int index = 0;

    static {
        try {
            invokeMethod = AopAdviceChainInvocation.class.getMethod("invoke", null);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
    }

    public AopAdviceChainInvocation(Object proxy, Object target, Method method, Object[] args, List<Object> advices) {
        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.args = args;
        this.advices = advices;
    }

    public Object invoke(){
        if(index<this.advices.size()){
            Object advice = advices.get(index++);
            if(advice instanceof MethodBeforeAdvice){
                ((MethodBeforeAdvice)advice).before(target,method,args);
            }else if(advice instanceof AfterReturningAdvice){

            }
        }else{
            return MethodUtils.invoke(target,method,args);
        }
        return null;
    }



}
