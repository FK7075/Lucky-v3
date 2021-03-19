package org.jacklamb.lucky.context;

import com.lucky.utils.io.utils.AntPathMatcher;
import com.lucky.utils.io.utils.PathMatcher;
import org.jacklamb.lucky.beans.BeanDefinitionRegister;
import org.jacklamb.lucky.beans.postprocessor.BeanPostProcessorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * classpath下的BeanDefinition扫描器
 * @author fk
 * @version 1.0
 * @date 2021/3/19 0019 15:19
 */
public class ClassPathBeanDefinitionScanner {

    private static final Logger log = LoggerFactory.getLogger(ClassPathBeanDefinitionScanner.class);
    /** bean定义注册器*/
    private BeanDefinitionRegister registry;
    /** bean读取器*/
    private BeanDefinitionReader reader;
    /** classpath文件扫描器*/
    private PathMatcher pathMatcher = new AntPathMatcher();
    /** 资源模式*/
    private String resourcePatter = "**/*.class";

    public ClassPathBeanDefinitionScanner(BeanDefinitionRegister registry) {
        super();
        this.registry = registry;
        this.reader = new AnnotationBeanDefinitionReader(this.registry);
    }

}
