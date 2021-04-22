package org.luckyframework.datasource;

import com.lucky.utils.base.Assert;
import org.luckyframework.beans.aware.ApplicationContextAware;
import org.luckyframework.beans.factory.DisposableBean;
import org.luckyframework.beans.factory.InitializingBean;
import org.luckyframework.context.ApplicationContext;
import org.luckyframework.environment.Environment;

import javax.sql.DataSource;
import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.luckyframework.datasource.DataSourceBuilder.DATA_SOURCE_PREFIX;
import static org.luckyframework.datasource.DataSourceBuilder.POOL_TYPE;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/21 0021 9:36
 */
public class LuckyDataSourceManager implements DataSourceManager, ApplicationContextAware, InitializingBean , DisposableBean {

    //利用DataSourceFactory达到懒加载的效果
    private final static Map<String,DataSourceFactory> configDataSourceFactoryMap = new ConcurrentHashMap<>();

    private static DataSourceBuilder defaultDataSourceBuilder;
    private ApplicationContext applicationContext;

    static {
        setDefaultDataSourceBuilder(new HikariDataSourceBuilder());
    }

    public static Map<String,DataSource> getAllDataSources() throws Exception {
        if(!configDataSourceFactoryMap.isEmpty()){
            Set<String> factoryName = configDataSourceFactoryMap.keySet();
            for (String dbname : factoryName) {
                dataSourceMap.put(dbname,configDataSourceFactoryMap.get(dbname).getDataSource());
            }
        }
        return dataSourceMap;
    }

    public static DataSource getDefaultDataSource() throws Exception {
        return getSDataSource(DEFAULT_DBNAME);
    }

    public static DataSource getSDataSource(String dbname) throws Exception {
        if(dataSourceMap.containsKey(dbname)){
            return dataSourceMap.get(dbname);
        }
        if(configDataSourceFactoryMap.containsKey(dbname)){
            DataSourceFactory dataSourceFactory = configDataSourceFactoryMap.get(dbname);
            DataSource dataSource = dataSourceFactory.getDataSource();
            dataSourceMap.put(dbname,dataSource);
            return dataSource;
        }
        return null;
    }

    public static DataSourceBuilder getDefaultDataSourceBuilder() {
        return defaultDataSourceBuilder;
    }

    public static void setDefaultDataSourceBuilder(DataSourceBuilder defaultDataSourceBuilder) {
        LuckyDataSourceManager.defaultDataSourceBuilder = defaultDataSourceBuilder;
        if(!dataSourceBuilderMap.containsKey(defaultDataSourceBuilder.getPoolType())){
            dataSourceBuilderMap.put(defaultDataSourceBuilder.getPoolType().toUpperCase(),defaultDataSourceBuilder);
        }
    }

    @Override
    public DataSource getDataSource(String dbname) throws Exception {
        return getSDataSource(dbname);
    }

    @Override
    public Collection<DataSource> getDataSources() throws Exception {
        return getAllDataSources().values();
    }

    @Override
    public void addDataSource(String dbname, DataSource dataSource) {
        Assert.notNull(dbname,"Data source registration failed! dbname is null");
        Assert.notNull(dataSource,"Data source registration failed! data source is null");
        if (containsDataSource(dbname)){
            throw new DataSourceRegistrationException("Data source registration failed! The data source named '"+dbname+"' has been registered");
        }
        dataSourceMap.put(dbname, dataSource);
    }

    public void addDataSourceFactory(String dbname, DataSourceFactory dataSourceFactory){
        Assert.notNull(dbname,"Data source registration failed! dbname is null");
        Assert.notNull(dataSourceFactory,"Data source factory registration failed! data source factory is null");
        if (containsDataSource(dbname)){
            throw new DataSourceRegistrationException("Data source registration failed! The data source named '"+dbname+"' has been registered");
        }
        configDataSourceFactoryMap.put(dbname, dataSourceFactory);
    }

    @Override
    public void removeDataSource(String dbname) {
        dataSourceMap.remove(dbname);
        configDataSourceFactoryMap.remove(dbname);
    }

    @Override
    public boolean containsDataSource(String dbname) {
        return dataSourceMap.containsKey(dbname) || configDataSourceFactoryMap.containsKey(dbname);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() throws Exception {
        //注册IOC容器中的DataSource
        String[] dbNames = applicationContext.getBeanNamesForType(DataSource.class);
        for (String dbName : dbNames) {
            addDataSourceFactory(dbName,()->applicationContext.getBean(dbName,DataSource.class));
        }
        Environment environment = applicationContext.getEnvironment();
        if(!environment.contains(DATA_SOURCE_PREFIX)){
            return;
        }
        Object dataConfig = environment.getProperty(DATA_SOURCE_PREFIX);

        //注册配置文件中的DataSource
        if(!(dataConfig instanceof Map)){
           throw new DataSourceParsingException("An exception occurred while parsing the data source configuration in the default configuration file, please check the '"+DATA_SOURCE_PREFIX+"' configuration");
        }else{
            Map<String,Object> dataMap = (Map<String, Object>) dataConfig;
            for(Map.Entry<String,Object> data : dataMap.entrySet()){
                Object dataValue = data.getValue();

                //单一数据源，使用省略"dbname"方式配置
                if(!(dataValue instanceof Map)){
                    Object poolTypeObj = dataMap.get(POOL_TYPE);
                    DataSourceBuilder builder = poolTypeObj == null
                            ? defaultDataSourceBuilder
                            : DataSourceManager.getDataSourceBuilder(poolTypeObj.toString());
                    if(builder == null){
                        throw new DataSourceParsingException("An exception occurred while parsing the data source configuration in the default configuration file,There is no DataSourceBuilder of type '"+poolTypeObj+"'");
                    }
                    addDataSourceFactory(DEFAULT_DBNAME, ()->builder.builder(environment,DATA_SOURCE_PREFIX));
                    break;
                }
                Map<String,Object> dataSourceInfo = (Map<String, Object>) dataValue;
                Object poolTypeObj = dataSourceInfo.get(POOL_TYPE);
                DataSourceBuilder builder = poolTypeObj == null
                            ? defaultDataSourceBuilder
                            : DataSourceManager.getDataSourceBuilder(poolTypeObj.toString());
                if(builder == null){
                    throw new DataSourceParsingException("An exception occurred while parsing the data source configuration in the default configuration file,There is no DataSourceBuilder of type '"+poolTypeObj+"'");
                }
                String dbname= data.getKey();
                addDataSourceFactory(dbname,()->builder.builder(environment,DATA_SOURCE_PREFIX+"."+dbname));
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        for(Map.Entry<String,DataSource> dataSourceEntry : dataSourceMap.entrySet()){
            DataSource dataSource = dataSourceEntry.getValue();
            if(dataSource instanceof Closeable){
                ((Closeable)dataSource).close();
            }
        }
    }

    @FunctionalInterface
    interface DataSourceFactory{
        DataSource getDataSource() throws Exception;
    }
}
