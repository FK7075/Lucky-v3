package org.luckyframework.aop.proxy;

/**
 * Aop代理
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 14:47
 */
public interface AopProxy {

    Object getProxy();

    Object getProxy(ClassLoader classLoader);

}
