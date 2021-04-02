package org.jacklamb.luckydata.Integration;

import com.lucky.datasource.sql.HikariCPDataSource;
import com.lucky.datasource.sql.LuckyDataSource;
import com.lucky.datasource.sql.LuckyDataSourceManage;
import com.lucky.jacklamb.jdbc.core.abstcore.SqlCoreFactory;
import com.lucky.utils.base.Assert;
import com.lucky.utils.base.BaseUtils;
import com.lucky.utils.type.AnnotationMetadata;
import org.jacklamb.luckydata.annotation.Mapper;
import org.luckyframework.beans.BeanDefinitionRegistry;
import org.luckyframework.beans.ConstructorValue;
import org.luckyframework.beans.GenericBeanDefinition;
import org.luckyframework.beans.aware.ApplicationContextAware;
import org.luckyframework.context.ApplicationContext;
import org.luckyframework.context.annotation.ImportBeanDefinitionRegistrar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/2 0002 9:14
 */
public class JackLambAutoImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private static final String FACTORY_METHOD = "createSqlCore";
    private static final String CREATE_MAPPER_METHOD = "getMapper";
    private static final String DESTROY_METHOD = "close";

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext=applicationContext;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata cm, BeanDefinitionRegistry registry) {
        //注册默认的连接池类型
        LuckyDataSourceManage.registerPool(HikariCPDataSource.class);

        //将IOC容器中的数据源加入到数据源管理器
        String[] luckyDatasourceBeanNames = applicationContext.getBeanNamesForType(LuckyDataSource.class);
        for (String datasourceBeanName : luckyDatasourceBeanNames) {
            LuckyDataSourceManage.addLuckyDataSource(applicationContext.getBean(datasourceBeanName,LuckyDataSource.class));
        }

        Map<String, List<Class<?>>> dbnameMapperClassMap = new HashMap<>(20);
        Set<Class<?>> mapperClassSet = applicationContext.getClassResourceLoader().getClassesByAnnotation(Mapper.class);
        if(!Assert.isEmptyCollection(mapperClassSet)){
            dbnameMapperClassMap = mapperClassSet.stream().collect(Collectors.groupingBy(c->c.getAnnotation(Mapper.class).dbname()));
        }

        List<LuckyDataSource> allDataSource = LuckyDataSourceManage.getAllDataSource();
        for (LuckyDataSource dataSource : allDataSource) {
            String dbname = dataSource.getDbname();
            String beanName = dbname+"SqlCore";
            GenericBeanDefinition definition = new GenericBeanDefinition(SqlCoreFactory.class);
            definition.setFactoryMethodName(FACTORY_METHOD);
            definition.setDestroyMethodName(DESTROY_METHOD);
            ConstructorValue[] constructorValues = new ConstructorValue[]{
                    new ConstructorValue(dbname)
            };
            definition.setConstructorValues(constructorValues);
            registry.registerBeanDefinition(beanName,definition);
            List<Class<?>> mapperClasses = dbnameMapperClassMap.get(dbname);
            if(!Assert.isEmptyCollection(mapperClasses)){
                for (Class<?> mapperClass : mapperClasses) {
                    GenericBeanDefinition mapper =new GenericBeanDefinition();
                    mapper.setFactoryBeanName(beanName);
                    mapper.setFactoryMethodName(CREATE_MAPPER_METHOD);
                    ConstructorValue[] createMapperArgs = new ConstructorValue[]{
                            new ConstructorValue(mapperClass)
                    };
                    mapper.setConstructorValues(createMapperArgs);
                    mapper.setFinallyClass(mapperClass);
                    registry.registerBeanDefinition(getBeanName(mapperClass),mapper);
                }
            }
        }

    }

    private String getBeanName(Class<?> mapperClass){
        Mapper mapper = mapperClass.getAnnotation(Mapper.class);
        String value = mapper.value();
        return Assert.isBlankString(value)? BaseUtils.lowercaseFirstLetter(mapperClass.getSimpleName()):value;
    }
}
