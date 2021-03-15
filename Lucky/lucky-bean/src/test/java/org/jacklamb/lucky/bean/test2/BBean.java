package org.jacklamb.lucky.bean.test2;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/13 下午6:51
 */
public class BBean {

    private CBean cBean;
    String name;

    public BBean(){}

    public BBean(CBean cBean){
        this.cBean=cBean;
    }

    public BBean(CBean cBean,String name){
        this.cBean=cBean;
        this.name=name;
    }

    public String print() {
        final StringBuilder sb = new StringBuilder("BBean{");
        sb.append("cBean=").append(cBean);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
