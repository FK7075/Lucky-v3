package org.luckyframework.aop.pointcut;

import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.ShadowMatch;

import java.lang.reflect.Method;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 14:33
 */
public class AspectJExpressionPointcut extends AbstractExpressionPointcut {

    //获得切点解析器
    private static final PointcutParser pp = PointcutParser
            .getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution();

    //AspectJ的Pointcut表达式校验
    private final PointcutExpression pe;

    public AspectJExpressionPointcut(String expression) {
        super(expression);
        pe=pp.parsePointcutExpression(expression);
    }

    @Override
    public boolean matchClass(Class<?> targetClass) {
        return pe.couldMatchJoinPointsInType(targetClass);
    }

    @Override
    public boolean matchMethod(Class<?> targetClass, Method method) {
        ShadowMatch sm = pe.matchesAdviceExecution(method);
        return sm.alwaysMatches();
    }
}
