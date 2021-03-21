package org.jacklamb.lucky.context;

import com.lucky.utils.base.Assert;
import com.lucky.utils.base.BaseUtils;
import com.lucky.utils.base.StringUtils;
import com.lucky.utils.fileload.Resource;
import com.lucky.utils.reflect.ClassUtils;
import org.jacklamb.lucky.beans.BeanDefinitionRegister;
import org.jacklamb.lucky.beans.GenericBeanDefinition;
import org.jacklamb.lucky.context.annotation.Component;
import org.jacklamb.lucky.exception.BeanDefinitionRegisterException;

import java.io.File;
import java.io.IOException;

/**
 * 注解bean定义读取器,从Resource里面加载bean定义、创建bean定义、注册bean定义到bean工厂
 * @author fk
 * @version 1.0
 * @date 2021/3/19 0019 15:24
 */
public class AnnotationBeanDefinitionReader extends AbstractBeanDefinitionReader {

    public AnnotationBeanDefinitionReader(BeanDefinitionRegister registry) {
        super(registry);
    }

    @Override
    public void loadBeanDefinition(Resource resource) throws Exception {
        this.loadBeanDefinitions(resource);
    }

    @Override
    public void loadBeanDefinitions(Resource... resources) throws Exception {
        if(!Assert.isEmptyArray(resources)){
            for (Resource resource : resources) {
                searchAndRegistryBeanDefinition(resource);
            }
        }
    }

    private void searchAndRegistryBeanDefinition(Resource resource) throws IOException, BeanDefinitionRegisterException {
        if(resource != null && resource.getFile() !=null){
            String className = getClassNameFromFile(resource.getFile());
            Class<?> clazz = ClassUtils.getClass(className);
            Component component = clazz.getAnnotation(Component.class);
            if(component != null){
                GenericBeanDefinition bd = new GenericBeanDefinition();
                bd.setBeanClass(clazz);
                String beanName= Assert.isBlankString(component.value())
                        ? BaseUtils.lowercaseFirstLetter(clazz.getSimpleName())
                        : component.value();
                this.registry.registerBeanDefinition(beanName, bd);
            }
        }
    }

    private final int classPathAbsLength = AnnotationBeanDefinitionReader.class.getResource("/").toString().length();

    private String getClassNameFromFile(File file) {
        String absPath = file.getAbsolutePath();
        String name = absPath.substring(classPathAbsLength + 1, absPath.indexOf('.'));
        return StringUtils.replace(name, File.separator, ".");
    }
}
