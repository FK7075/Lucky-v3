package org.luckyframework.aop.advisor;

import org.luckyframework.aop.advice.Advice;

/**
 * 切面
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 14:37
 */
public interface Advisor {

    String getAdvisorBeanName();

    Advice getAdvisor();

    String getExpression();

}
