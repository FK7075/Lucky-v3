package org.jacklamb.lucky.beans;

import org.jacklamb.lucky.exception.BeansException;

/**
 * Bean工厂
 * @author fk
 * @version 1.0
 * @date 2021/3/12 0012 16:34
 */
public interface BeanFactory {

    /**
     * 获取一个bean的实例
     * @param name bean的唯一ID
     * @return bean实例
     * @throws BeansException bean异常
     */
    Object getBean(String name) throws Exception;
}
