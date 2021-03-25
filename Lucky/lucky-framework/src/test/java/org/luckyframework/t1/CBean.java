package org.luckyframework.t1;

import org.luckyframework.beans.BeanScope;
import org.luckyframework.context.ApplicationContext;
import org.luckyframework.context.annotation.*;
import org.luckyframework.environment.Environment;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/25 上午12:48
 */
@Configuration("c")
@PropertySource({"classpath:**/*.properties","classpath:**/*.yml"})
public class CBean {

    @Bean(name = "o1",initMethod = "initMethod",destroyMethod = "destroyMethod")
    public Test01 test01(ABean a, ApplicationContext context){
        System.out.println("A -> "+a);
        System.out.println("ApplicationContext -> "+context);
        return new Test01();
    }

    @Autowired
    private Environment environment;

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

    public void print(){
        System.out.println(environment.getProperty("info"));
    }
}
