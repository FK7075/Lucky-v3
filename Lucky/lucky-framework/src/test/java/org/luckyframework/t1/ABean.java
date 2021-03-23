package org.luckyframework.t1;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/23 0023 14:08
 */
public class ABean {

    private Integer id;
    private BBean b;

    private String name;

    public ABean(String name) {
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
}
