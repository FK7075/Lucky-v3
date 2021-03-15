package org.jacklamb.lucky.bean.test1;

import java.time.LocalDateTime;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/13 上午2:43
 */
public class ABean {

    public void doSomthing(){
        System.out.println(LocalDateTime.now()+"==>"+this);
    }

    public void init(){
        System.out.println("ABean 的初始化方法执行...");
    }

    public void destroy(){
        System.out.println("ABean 的销毁方法执行...");
    }
}
