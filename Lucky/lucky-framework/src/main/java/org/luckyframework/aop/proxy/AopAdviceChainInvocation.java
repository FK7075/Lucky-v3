package org.luckyframework.aop.proxy;

import com.lucky.utils.reflect.MethodUtils;
import org.aspectj.lang.JoinPoint;
import org.luckyframework.aop.MethodInterceptorJoinPoint;
import org.luckyframework.aop.advice.*;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/7 0007 14:06
 */
public class AopAdviceChainInvocation {

    private final MethodInterceptorJoinPoint joinPoint;
    private final Object target;
    private final Object proxy;
    private Object[] args;
    private final Method method;
    private final List<Advice> advices;
    private int index = 0;

    public AopAdviceChainInvocation(Object proxy,Object target, Method method, Object[] args, List<Advice> advices) {
        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.advices = advices;
        this.args = args;
        this.joinPoint = new MethodInterceptorJoinPoint(this);
    }

    public Object getProxy() {
        return proxy;
    }

    public Object[] getArgs() {
        return args;
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public List<Advice> getAdvices() {
        return advices;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Object invoke() throws Throwable{
        if(index<this.advices.size()){
            Object advice = advices.get(index++);
            //前置增加
            if(advice instanceof BeforeAdvice){
                ((BeforeAdvice)advice).before(joinPoint);
            }
            //正常执行的后置增强
            else if(advice instanceof AfterReturningAdvice){
                Object result = this.invoke();
                ((AfterReturningAdvice)advice).afterReturning(joinPoint,result);
                return result;
            }
            //执行异常的后置增强
            else if(advice instanceof AfterThrowingAdvice){
                try {
                    return this.invoke();
                }catch (Throwable e){
                    ((AfterThrowingAdvice)advice).afterThrowing(joinPoint,e);
                }
            }

            //后置增强
            else if(advice instanceof AfterAdvice){
                try {
                    return this.invoke();
                }finally {
                    ((AfterAdvice)advice).after(joinPoint);
                }
            }
            //环绕增强
            else if(advice instanceof MethodInterceptor){
                return ((MethodInterceptor)advice).invoke(joinPoint);
            }
            return this.invoke();
        }else{
            return MethodUtils.invoke(target,method,args);
        }
    }
}
