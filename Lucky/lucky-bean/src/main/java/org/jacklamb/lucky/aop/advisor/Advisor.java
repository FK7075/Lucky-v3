package org.jacklamb.lucky.aop.advisor;

import org.jacklamb.lucky.aop.advice.Advice;

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
