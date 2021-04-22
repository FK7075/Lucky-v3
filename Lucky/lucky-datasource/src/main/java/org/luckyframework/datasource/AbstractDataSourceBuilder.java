package org.luckyframework.datasource;

import com.lucky.utils.annotation.Nullable;
import com.lucky.utils.conversion.JavaConversion;
import com.lucky.utils.reflect.ClassUtils;
import org.luckyframework.environment.Environment;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        Object confValue = ENVIRONMENT.getProperty(realKey);
        if(ClassUtils.isSimple(aClass) || ClassUtils.isPrimitive(aClass)){
            return (T) JavaConversion.strToBasic(confValue.toString(),aClass);
        }
        return (T)ClassUtils.newObject(confValue.toString());
    }

    @SuppressWarnings("unchecked")
    protected List<String> getConfCollectionValue(String key){
        String realKey = PREFIX+"."+key;
        if(!ENVIRONMENT.contains(realKey)){
            return null;
        }
        Object confValue = ENVIRONMENT.getProperty(realKey);
        if((confValue instanceof Collection)){
            throw new RuntimeException("The value corresponding to the '"+realKey+"' configuration is not a collection type");
        }
        List<Object> list = (List<Object>) confValue;
        List<String> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            result.add(ENVIRONMENT.getProperty(realKey+".get("+i+")").toString());
        }
        return result;
    }

    public abstract DataSource createDataSource() throws Exception;

}
