package org.luckyframework.datasource;

import javax.sql.DataSource;

/**
 * 数据源的自动创建器
 * @author fk
 * @version 1.0
 * @date 2021/4/16 0016 11:52
 */
public interface DataSourceBuilder {

    DataSource builder();

}
