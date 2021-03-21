package org.jacklamb.lucky.exception;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/22 上午12:49
 */
public class LuckyIOException extends RuntimeException {

    public LuckyIOException(String message,Throwable e){
        super(message,e);
    }

    public LuckyIOException(Throwable e){
        super(e);
    }

    public LuckyIOException(String message){
        super(message);
    }
}
