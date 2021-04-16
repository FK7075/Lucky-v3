package org.luckyframework.datasource;

import javax.sql.DataSource;
import java.util.List;

/**
 * 数据源管理器
 * @author fk
 * @version 1.0
 * @date 2021/4/16 0016 11:33
 */
public interface DataSourceManager {

    /** 根据dbname获取一个数据源*/
    DataSource getDataSource(String dbname);

    /** 获取所有已经注册的数据源*/
    List<DataSource> getDataSources();

    /** 添加一个数据源 */
    void addDataSource(String dbname,DataSource dataSource);

}
