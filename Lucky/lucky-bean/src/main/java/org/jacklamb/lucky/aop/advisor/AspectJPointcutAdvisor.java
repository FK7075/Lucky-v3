package org.jacklamb.lucky.aop.advisor;

import org.jacklamb.lucky.aop.advice.Advice;
import org.jacklamb.lucky.aop.pointcut.AspectJExpressionPointcut;
import org.jacklamb.lucky.aop.pointcut.Pointcut;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 14:40
 */
public class AspectJPointcutAdvisor implements PointcutAdvisor{

    //用户配置的advice的bean的名字
    private String adviceBeanName;
    private Advice advice;
    //切入点表达式
    private final String expression;
    //AspectJ表达式切入点对象
    private final AspectJExpressionPointcut pointcut;

    public AspectJPointcutAdvisor(String adviceBeanName, String expression) {
        this.adviceBeanName = adviceBeanName;
        this.expression = expression;
        this.pointcut=new AspectJExpressionPointcut(expression);
    }

    public AspectJPointcutAdvisor(Advice advice, String expression) {
        this.advice = advice;
        this.expression = expression;
        this.pointcut=new AspectJExpressionPointcut(expression);
    }

    @Override
    public String getAdvisorBeanName() {
        return adviceBeanName;
    }

    @Override
    public Advice getAdvisor() {
        return advice;
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }
}
