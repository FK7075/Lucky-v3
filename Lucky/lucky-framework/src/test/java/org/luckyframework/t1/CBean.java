package org.luckyframework.t1;

import org.luckyframework.context.annotation.Autowired;
import org.luckyframework.context.annotation.Component;
import org.luckyframework.context.annotation.Configuration;
import org.luckyframework.context.annotation.Service;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/25 上午12:48
 */
@Component("c")
public class CBean {

    @Autowired(required = false)
    private ABean a;

    @Autowired(required = false)
    private BBean b;

    public static ABean getABean(){
        return new ABean();
    }

    public BBean getBBean(){
        return new BBean();
    }
}
