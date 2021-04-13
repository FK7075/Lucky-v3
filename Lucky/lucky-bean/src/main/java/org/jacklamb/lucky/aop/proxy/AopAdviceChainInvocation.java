package org.jacklamb.lucky.aop.proxy;

import com.lucky.utils.reflect.MethodUtils;
import org.aspectj.lang.JoinPoint;
import org.jacklamb.lucky.aop.advice.*;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 将增强组成执行链
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 15:30
 */
public class  AopAdviceChainInvocation {

    private static Method invokeMethod = MethodUtils.getMethod(AopAdviceChainInvocation.class,"invoke");
    private Object proxy;
    private final Object target;
    private final Method method;
    private Object[] args;
    private final List<Object> advices;
    private int index = 0;
    private final JoinPoint joinPoint;

    public void setArgument(Object[] args){
        this.args=args;
    }

    public Object getProxy() {
        return proxy;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    public AopAdviceChainInvocation(Object proxy, Object target, Method method, Object[] args, List<Object> advices) {
        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.args = args;
        this.advices = advices;
        joinPoint = new MethodInterceptorJoinPoint(this);
    }

    public Object invoke(){
        if(index<this.advices.size()){
            Object advice = advices.get(index++);
            //前置增加
            if(advice instanceof MethodBeforeAdvice){
                ((MethodBeforeAdvice)advice).before();
            }
            //正常执行的后置增强
            else if(advice instanceof AfterReturningAdvice){
                Object result = this.invoke();
                ((AfterReturningAdvice)advice).afterReturning(result);
                return result;
            }
            //执行异常的后置增强
            else if(advice instanceof AfterThrowingAdvice){
                try {
                    return this.invoke();
                }catch (Throwable e){
                    ((AfterThrowingAdvice)advice).afterThrowing(e);
                }
            }

            //后置增强
            else if(advice instanceof MethodAfterAdvice){
                try {
                    return this.invoke();
                }finally {
                    ((MethodAfterAdvice)advice).after();
                }
            }
            //环绕增强
            else if(advice instanceof MethodInterceptor){
                return ((MethodInterceptor)advice).invoke(this,invokeMethod,null);
            }
            return this.invoke();
        }else{
            return MethodUtils.invoke(target,method,args);
        }
    }
}
