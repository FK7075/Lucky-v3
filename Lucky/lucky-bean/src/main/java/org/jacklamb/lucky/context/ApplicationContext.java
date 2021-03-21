package org.jacklamb.lucky.context;

import com.lucky.utils.fileload.Resource;
import com.lucky.utils.fileload.ResourceLoader;
import org.jacklamb.lucky.beans.factory.BeanFactory;
import org.jacklamb.lucky.context.scanner.FileScanner;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/19 0019 15:08
 */
public interface ApplicationContext extends BeanFactory , ResourceLoader {

    @Override
    default Resource getResource(String location) {
        return FileScanner.getResource(location);
    }

    default Resource[] getResources(String location){
        return FileScanner.getResources(location);
    }
}
