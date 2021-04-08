package org.luckyframework.aop.proxy;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/7 0007 11:54
 */
public interface AopProxy {

    Object getProxy();

    Object getProxy(ClassLoader classLoader);

}
