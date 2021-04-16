package org.luckyframework.aop.advisor;

import org.luckyframework.aop.pointcut.Pointcut;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 14:39
 */
public interface PointcutAdvisor extends Advisor {

    Pointcut getPointcut();



}
