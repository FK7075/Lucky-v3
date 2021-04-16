package org.luckyframework.aop;

import org.luckyframework.aop.proxy.ProxyFactory;

/**
 * 基于AspectJ的自动代理创建器
 * @author fk
 * @version 1.0
 * @date 2021/4/13 0013 11:13
 */
public class AspectJAdvisorAutoProxyCreator extends AbstractAspectJAdvisorAutoProxyCreator {


    @Override
    public ProxyFactory getProxyFactory(Object target) {
        return new ProxyFactory(target);
    }
}
