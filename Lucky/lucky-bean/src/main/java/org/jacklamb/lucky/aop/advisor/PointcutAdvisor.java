package org.jacklamb.lucky.aop.advisor;

import org.jacklamb.lucky.aop.pointcut.Pointcut;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 14:39
 */
public interface PointcutAdvisor extends Advisor {

    Pointcut getPointcut();



}
