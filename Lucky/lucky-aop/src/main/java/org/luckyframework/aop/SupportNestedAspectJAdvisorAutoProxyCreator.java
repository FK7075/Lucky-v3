package org.luckyframework.aop;

import org.luckyframework.aop.proxy.ProxyFactory;

/**
 * 基于AspectJ并支持嵌套代理自动代理创建器
 * @author fk
 * @version 1.0
 * @date 2021/4/15 0015 11:55
 */
public class SupportNestedAspectJAdvisorAutoProxyCreator extends AbstractAspectJAdvisorAutoProxyCreator{
    @Override
    public ProxyFactory getProxyFactory(Object target) {
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.setSupportNestedProxy(true);
        return proxyFactory;
    }
}
