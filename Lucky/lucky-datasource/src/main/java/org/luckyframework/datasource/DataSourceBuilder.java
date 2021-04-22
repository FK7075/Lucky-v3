package org.luckyframework.datasource;

import org.luckyframework.environment.Environment;

import javax.sql.DataSource;

/**
 * 数据源的自动创建器
 * @author fk
 * @version 1.0
 * @date 2021/4/16 0016 11:52
 */
public interface DataSourceBuilder {

    String DATA_SOURCE_PREFIX   = "lucky.datasource";
    String DRIVER_CLASS_NAME    = "driver-class-name";
    String JDBC_URL             = "jdbc-url";
    String USER_NAME            = "username";
    String PASSWORD             = "password";
    String POOL_TYPE            = "pool-type";
    String POOL_NAME            = "pool-name";

    DataSource builder(Environment environment,String dataSourceConfigPrefix) throws Exception;

    String getPoolType();


}
