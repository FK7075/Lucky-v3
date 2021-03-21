package org.jacklamb.lucky.context.scanner;

import com.lucky.utils.fileload.Resource;
import com.lucky.utils.reflect.ClassUtils;
import org.jacklamb.lucky.exception.LuckyIOException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 获取classpath下指定包下的所有class资源
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/22 上午12:32
 */
public class PathClassFileScanner {

    private static final String CLASS_FILE_TEMP = "classpath:%s/**/*.class";
    private static final int START_INDEX = PathClassFileScanner.class.getResource("/").toString().length();
    private final Resource[] resources;

    public PathClassFileScanner(String packageName){
        resources=getResourcesByPackage(packageName);
    }

    public PathClassFileScanner(Class<?> rootClass){
        resources=getResourcesByClass(rootClass);
    }

    public Set<Class<?>> getClasses() throws IOException {
        Set<Class<?>> classes = new HashSet<>();
        String fullClass;
        for (Resource resource : resources) {
            fullClass = resource.getURL().toString();
            fullClass = fullClass.substring(START_INDEX,fullClass.lastIndexOf("."))
                    .replaceAll("/",".");
            classes.add(ClassUtils.getClass(fullClass));
        }
        return classes;
    }

    public Resource[] getResources(){
        return this.resources;
    }

    public Resource[] getResourcesByPackage(String packageName) {
        String location = String.format(CLASS_FILE_TEMP,packageName.replaceAll("\\.","/"));
        return FileScanner.getResources(location);
    }

    public Resource[] getResourcesByClass(Class<?> rootClass) {
        return getResourcesByPackage(rootClass.getPackage().getName());
    }




}
