package org.luckyframework.datasource;

import com.lucky.utils.annotation.Nullable;
import com.lucky.utils.conversion.JavaConversion;
import com.lucky.utils.reflect.ClassUtils;
import org.luckyframework.environment.Environment;

import javax.sql.DataSource;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/21 0021 16:58
 */
public abstract class AbstractDataSourceBuilder implements DataSourceBuilder{

    private String PREFIX;
    private Environment ENVIRONMENT;


    @Override
    public final DataSource builder(Environment environment, String dataSourceConfigPrefix) throws Exception {
        PREFIX = dataSourceConfigPrefix;
        ENVIRONMENT = environment;
        return createDataSource();

    }

    protected <T> T getConfValue(String key,Class<T> aClass,@Nullable T defaultValue){
        T value = getConfValue(key, aClass);
        return value == null ? defaultValue : value;
    }

    @SuppressWarnings("unchecked")
    protected  <T> T getConfValue(String key,Class<T> aClass){
        String realKey = PREFIX+"."+key;
        if(!ENVIRONMENT.contains(realKey)){
            return null;
        }
        String strValue = ENVIRONMENT.getProperty(realKey).toString();
        if(ClassUtils.isSimple(aClass) || ClassUtils.isPrimitive(aClass)){
            return (T) JavaConversion.strToBasic(strValue,aClass);
        }
        return (T)ClassUtils.newObject(strValue);
    }

    public abstract DataSource createDataSource() throws Exception;

}
