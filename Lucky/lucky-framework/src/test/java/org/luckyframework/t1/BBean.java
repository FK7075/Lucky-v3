package org.luckyframework.t1;

import org.luckyframework.beans.BeanScope;
import org.luckyframework.beans.aware.EnvironmentAware;
import org.luckyframework.context.annotation.Controller;
import org.luckyframework.context.annotation.Scope;
import org.luckyframework.context.annotation.Value;
import org.luckyframework.environment.Environment;

import java.util.List;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/23 0023 14:08
 */
@Controller
public class BBean implements EnvironmentAware {

    @Value("${user.dir}")
    private String USER_DIR;

    @Value("${int}")
    private String my;

    @Value("${list}")
    private List<Integer> list;

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

    @Override
    public void setEnvironment(Environment environment) {
        System.out.println("vdfv");
    }
}
