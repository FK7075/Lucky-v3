package org.luckyframework.datasource;

import org.luckyframework.beans.factory.InitializingBean;
import org.luckyframework.context.annotation.DependsOn;

import javax.sql.DataSource;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/16 0016 14:22
 */
@DependsOn("")
public class DefaultDataSourceManager extends AbstractDataSourceManager implements InitializingBean {


    @Override
    public void afterPropertiesSet() throws Exception {
        String[] dataSourceNames = applicationContext.getBeanNamesForType(DataSource.class);
        for (String dbname : dataSourceNames) {
            addDataSource(dbname,applicationContext.getBean(dbname,DataSource.class));
        }
    }
}
