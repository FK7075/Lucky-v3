package org.jacklamb.lucky.bean.test1;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/13 上午2:48
 */
public class ABeanFactory {

    public ABean getABean(){
        return new ABean();
    }

    public static ABean getABeanByStatic(){
        return new ABean();
    }
}
