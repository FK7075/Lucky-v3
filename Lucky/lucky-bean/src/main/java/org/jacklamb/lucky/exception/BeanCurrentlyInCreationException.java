package org.jacklamb.lucky.exception;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/14 上午1:26
 */
public class BeanCurrentlyInCreationException extends Exception {

    public BeanCurrentlyInCreationException(String message){
        super(message);
    }


}
