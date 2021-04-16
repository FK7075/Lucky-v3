package org.luckyframework.datasource;

import com.lucky.utils.base.Assert;
import org.luckyframework.beans.aware.ApplicationContextAware;
import org.luckyframework.beans.aware.BeanFactoryAware;
import org.luckyframework.beans.factory.BeanFactory;
import org.luckyframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/16 0016 11:37
 */
public abstract class AbstractDataSourceManager implements DataSourceManager, ApplicationContextAware {

    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();
    protected ApplicationContext applicationContext;

    @Override
    public DataSource getDataSource(String dbname) {
        return dataSourceMap.get(dbname);
    }

    @Override
    public List<DataSource> getDataSources() {
        return new ArrayList<>(dataSourceMap.values());
    }

    @Override
    public void addDataSource(String dbname, DataSource dataSource) {
        if(Assert.isBlankString(dbname)){
            throw new DataSourceRegistrationException("Failed to register data source, invalid dbname:'"+dbname+"'");
        }
        if(dataSourceMap.containsKey(dbname)){
            throw new DataSourceRegistrationException("Failed to register the data source, the data source named '"+dbname+"' already exists");
        }
        dataSourceMap.put(dbname, dataSource);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
