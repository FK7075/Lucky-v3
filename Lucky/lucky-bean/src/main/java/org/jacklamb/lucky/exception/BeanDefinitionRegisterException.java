package org.jacklamb.lucky.exception;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/12 0012 18:29
 */
public class BeanDefinitionRegisterException extends Exception{

    public BeanDefinitionRegisterException(String mess) {
        super(mess);
    }

    public BeanDefinitionRegisterException(String mess, Throwable e) {
        super(mess, e);
    }
}
