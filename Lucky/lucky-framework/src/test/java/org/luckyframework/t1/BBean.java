package org.luckyframework.t1;

import org.luckyframework.beans.BeanScope;
import org.luckyframework.context.annotation.Controller;
import org.luckyframework.context.annotation.Scope;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/23 0023 14:08
 */
@Controller
@Scope(BeanScope.PROTOTYPE)
public class BBean {

    private Integer id;
    private ABean a;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ABean getA() {
        return a;
    }

    public void setA(ABean a) {
        this.a = a;
    }
}
