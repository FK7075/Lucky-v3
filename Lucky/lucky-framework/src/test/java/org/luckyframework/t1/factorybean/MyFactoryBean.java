package org.luckyframework.t1.factorybean;

import org.luckyframework.beans.aware.BeanFactoryAware;
import org.luckyframework.beans.factory.BeanFactory;
import org.luckyframework.beans.factory.FactoryBean;
import org.luckyframework.context.annotation.Component;
import org.luckyframework.t1.ABean;

/**
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/27 上午1:29
 */
@Component
public class MyFactoryBean implements FactoryBean<ABean> , BeanFactoryAware {

    @Override
    public ABean getObject() throws Exception {
        ABean a =new ABean();
        a.setId(3306);
        a.setName("FactoryCreate->ABean");
        return a;
    }

    @Override
    public Class<?> getObjectType() {
        return ABean.class;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        System.out.println(beanFactory);
    }

}
