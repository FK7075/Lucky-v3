package org.luckyframework.t1;

import org.luckyframework.beans.BeanScope;
import org.luckyframework.beans.factory.DisposableBean;
import org.luckyframework.beans.factory.InitializingBean;
import org.luckyframework.context.annotation.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/23 0023 14:08
 */

@Service
@Lazy
public class ABean implements InitializingBean, DisposableBean {

    private Integer id;
    private BBean b;

    public ABean(){}

    private String name;

    @Autowired
    public ABean(BBean b,@Value("${os}") String name) {
        this.b = b;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BBean getB() {
        return b;
    }

    public void setB(BBean b) {
        this.b = b;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ABean{");
        sb.append("id=").append(id);
        sb.append(", b=").append(b);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static void main(String[] args) {
        Constructor<?>[] constructors = ABean.class.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Parameter[] parameters = constructor.getParameters();
            for (Parameter parameter : parameters) {
                System.out.println(parameter.getName());
            }

        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("ABean#afterPropertiesSet() "+name);
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("ABean#destroy() "+name);
    }
}
