package org.jacklamb.lucky.bean.test2;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/13 下午8:09
 */
public class MyBeanFactory {

    public BBean getBBean(CBean bean,String name){
        return new BBean(bean,name);
    }

    public static CBean getCBean(Integer id,String name){
        return new CBean(id, name);
    }
}
