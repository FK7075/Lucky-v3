package org.luckyframework.datasource;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.ExceptionSorter;
import com.lucky.utils.base.Assert;
import com.lucky.utils.reflect.ClassUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/22 0022 9:17
 */
public class DruidDataSourceBuilder extends AbstractDataSourceBuilder {


    private final static String INITIAL_SIZE                        = "initial-size";
    private final static String MAX_ACTIVE                          = "max_active";
    private final static String MAX_IDLE                            = "max_idle";
    private final static String MIN_IDLE                            = "min_idle";
    private final static String MAX_WAIT                            = "max_wait";
    private final static String POOL_PREPARED_STATEMENTS            = "pool-prepared-statements";
    private final static String MAX_OPEN_PREPARED_STATEMENTS        = "max-open-prepared-statements";
    private final static String VALIDATION_QUERY                    = "validation-query";
    private final static String TEST_ON_BORROW                      = "test-on-borrow";
    private final static String TEST_ON_RETURN                      = "test-on-return";
    private final static String TEST_WHILE_IDLE                     = "test-while-idle";
    private final static String TIME_BETWEEN_EVICTION_RUNS_MILLIS   = "time-between-eviction-runs-millis";
    private final static String NUM_TESTS_PER_EVICTION_RUN          = "num-tests-per-eviction-run";
    private final static String MIN_EVICTABLE_IDLE_TIME_MILLIS      = "min-evictable-idle-time-millis";
    private final static String CONNECTION_INIT_SQLS                = "connection-init-sqls";
    private final static String EXCEPTION_SORTER                    = "exception-sorter";
    private final static String FILTERS                             = "filters";
    private final static String PROXY_FILTERS                       = "proxy-filters";


    @Override
    public DataSource createDataSource() throws Exception {
        DruidDataSource dataSource = new DruidDataSource();
        String poolName = getConfValue(POOL_NAME, String.class);
        if(poolName != null) dataSource.setName(poolName);
        dataSource.setUrl(getConfValue(JDBC_URL,String.class));
        dataSource.setDriverClassName(getConfValue(DRIVER_CLASS_NAME,String.class));
        dataSource.setUsername(getConfValue(USER_NAME,String.class));
        dataSource.setPassword(getConfValue(PASSWORD,String.class));
        dataSource.setInitialSize(getConfValue(INITIAL_SIZE,int.class,0));
        dataSource.setMaxActive(getConfValue(MAX_ACTIVE,int.class,8));
        Integer maxIdle = getConfValue(MAX_IDLE, int.class);
        if(maxIdle != null)dataSource.setMaxIdle(maxIdle);
        Integer minIdle = getConfValue(MIN_IDLE, int.class);
        if(minIdle != null) dataSource.setMinIdle(minIdle);
        Integer maxWait = getConfValue(MAX_WAIT, int.class);
        if(maxWait != null) dataSource.setMaxWait(maxWait);
        dataSource.setPoolPreparedStatements(getConfValue(POOL_PREPARED_STATEMENTS,boolean.class,false));
        dataSource.setMaxOpenPreparedStatements(getConfValue(MAX_OPEN_PREPARED_STATEMENTS,int.class,-1));
        dataSource.setValidationQuery(getConfValue(VALIDATION_QUERY,String.class,"SELECT 1"));
        dataSource.setTestOnBorrow(getConfValue(TEST_ON_BORROW,boolean.class,true));
        dataSource.setTestOnReturn(getConfValue(TEST_ON_RETURN,boolean.class,false));
        dataSource.setTestWhileIdle(getConfValue(TEST_WHILE_IDLE,boolean.class,false));
        Long timeBetweenEvictionRunsMillis = getConfValue(TIME_BETWEEN_EVICTION_RUNS_MILLIS, long.class);
        if(timeBetweenEvictionRunsMillis != null) dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        Integer numTestsPerEvictionRun = getConfValue(NUM_TESTS_PER_EVICTION_RUN, int.class);
        if(numTestsPerEvictionRun != null) dataSource.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        Long minEvictableIdleTimeMillis = getConfValue(MIN_EVICTABLE_IDLE_TIME_MILLIS, long.class);
        if(minEvictableIdleTimeMillis != null)dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        List<String> list = getConfCollectionValue(CONNECTION_INIT_SQLS);
        if(!Assert.isEmptyCollection(list)) dataSource.setConnectionInitSqls(list);
        ExceptionSorter exceptionSorter = getConfValue(EXCEPTION_SORTER, ExceptionSorter.class);
        if(exceptionSorter != null)dataSource.setExceptionSorter(exceptionSorter);
        String filters = getConfValue(FILTERS, String.class);
        if(filters != null) dataSource.setFilters(filters);
        List<String> proxyFiltersStr = getConfCollectionValue(PROXY_FILTERS);
        if(!Assert.isEmptyCollection(proxyFiltersStr)){
            List<Filter> filterList = proxyFiltersStr.stream()
                        .map((str)->(Filter) ClassUtils.newObject(str))
                        .collect(Collectors.toList());
            dataSource.setProxyFilters(filterList);
        }
        return dataSource;
    }

    @Override
    public String getPoolType() {
        return "Druid";
    }
}
