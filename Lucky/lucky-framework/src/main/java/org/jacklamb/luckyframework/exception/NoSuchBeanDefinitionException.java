package org.jacklamb.luckyframework.exception;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/16 0016 10:21
 */
public class NoSuchBeanDefinitionException extends RuntimeException {

    public NoSuchBeanDefinitionException(Class<?> type){
        super("No bean named '" + type + "' available");
    }

}
