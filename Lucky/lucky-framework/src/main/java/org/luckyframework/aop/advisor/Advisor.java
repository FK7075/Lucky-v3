package org.luckyframework.aop.advisor;

import org.luckyframework.aop.advice.Advice;
import org.luckyframework.aop.pointcut.Pointcut;
import org.luckyframework.beans.Ordered;
import org.luckyframework.beans.PriorityOrdered;
import org.luckyframework.context.annotation.Order;

/**
 * 切面，用于组织Advice和Pointcut
 * @author fk
 * @version 1.0
 * @date 2021/4/7 0007 11:36
 */
public interface Advisor {

    Advice getAdvice();

    void setAdvice(Advice advice);

    Pointcut getPointcut();

    void setPointcut(Pointcut pointcut);

    int priority();

}
