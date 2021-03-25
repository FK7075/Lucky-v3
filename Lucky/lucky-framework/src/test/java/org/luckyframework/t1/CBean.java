package org.luckyframework.t1;

import org.luckyframework.context.annotation.*;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/25 上午12:48
 */
@Configuration("c")
public class CBean {

    @Autowired
    private ABean a;

    @Autowired
    private BBean b;

    public static ABean getABean(){
        return new ABean();
    }

    public BBean getBBean(){
        return new BBean();
    }
}
