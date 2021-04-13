package org.jacklamb.lucky.aop.aspectj;

import org.luckyframework.aop.pointcut.Pointcut;

import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/13 0013 11:54
 */
public interface GlobalPointcutManagement {

    void addPointcut(Pointcut pointcut);

    List<Pointcut> getAllPointcut();
}
