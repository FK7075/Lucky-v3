package org.jacklamb.lucky.context;

import com.lucky.utils.fileload.Resource;

/**
 * bean定义读取器接口
 * @author fk
 * @version 1.0
 * @date 2021/3/19 0019 15:21
 */
public interface BeanDefinitionReader {

    //加载单个bean定义
    void loadBeanDefinition(Resource resource) throws Exception;

    //加载多个bean定义
    void loadBeanDefinitions(Resource... resource) throws Exception;
}
