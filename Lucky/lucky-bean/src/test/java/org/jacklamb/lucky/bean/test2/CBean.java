package org.jacklamb.lucky.bean.test2;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/13 下午6:52
 */
public class CBean {

    private Integer id;
    private String name;
    private BBean bBean;

    public CBean(){}

    public CBean(BBean bBean){
        this.bBean=bBean;
    }

    public CBean(Integer id,String name){
        this.id=id;
        this.name=name;
    }

    public String print() {
        final StringBuilder sb = new StringBuilder("CBean{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
