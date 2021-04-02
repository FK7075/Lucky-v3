package org.luckyframework.context;

import com.lucky.utils.fileload.ResourceLoader;
import org.luckyframework.beans.BeanDefinitionRegistry;
import org.luckyframework.beans.factory.ListableBeanFactory;
import org.luckyframework.environment.EnvironmentCapable;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/24 0024 10:42
 */
public interface ApplicationContext extends ListableBeanFactory , ResourceLoader, BeanDefinitionRegistry, EnvironmentCapable {

    String[] getSingletonObjectNames();

    BasedPackageClassResourceLoader getClassResourceLoader();
}
