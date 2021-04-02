package org.luckyframework.context;

import com.lucky.utils.base.Assert;
import com.lucky.utils.fileload.Resource;
import com.lucky.utils.fileload.resourceimpl.PathMatchingResourcePatternResolver;
import com.lucky.utils.reflect.AnnotationUtils;
import com.lucky.utils.reflect.ClassUtils;
import org.luckyframework.context.annotation.Component;
import org.luckyframework.exception.LuckyIOException;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于包类的资源加载器
 * @author fk
 * @version 1.0
 * @date 2021/4/2 0002 10:05
 */
public class BasedPackageClassResourceLoader {

    private final static String CLASS_CONF_TEMP = "classpath:%s/**/*.class";
    private final static int basePackageIndex = RootBasedAnnotationApplicationContext.class.getResource("/").toString().length();
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final String[] basePackages;
    private final Set<Class<?>> allClassResources = new HashSet<>(225);

    public BasedPackageClassResourceLoader(String...basePackage){
        Assert.notNull(basePackage,"basePackage is null");
        this.basePackages = basePackage;
        scanner();
    }

    public BasedPackageClassResourceLoader(Class<?>... baseClass){
        String[] basePackages = new String[baseClass.length];
        int i = 0;
        for (Class<?> aClass : baseClass) {
            basePackages[i++] = aClass.getPackage().getName();
        }
        this.basePackages = basePackages;
        scanner();
    }

    public ClassLoader getClassLoader(){
        return resolver.getClassLoader();
    }

    public Resource getResource(String location){
        return resolver.getResource(location);
    }

    //循环扫描
    private void scanner(){
        for (String basePackage : basePackages) {
            doScanner(basePackage);
        }
    }

    //处理一个包扫描的逻辑
    private void doScanner(String basePackage){
        String root = basePackage.replaceAll("\\.", "/");
        String scanRule=String.format(CLASS_CONF_TEMP,root);
        try {
            Resource[] classResources = resolver.getResources(scanRule);
            for (Resource resource : classResources) {
                String fullClass = resource.getURL().toString();
                fullClass = fullClass.substring(basePackageIndex,fullClass.lastIndexOf("."))
                        .replaceAll("/",".");
                Class<?> aClass = ClassUtils.getClass(fullClass);
                allClassResources.add(aClass);
            }
        } catch (IOException e) {
            throw new LuckyIOException(e);
        }
    }

    /**
     * 获取所有扫描得到的类
     * @return 所有扫描得到的类
     */
    public Set<Class<?>> getAllClasses(){
        return allClassResources;
    }

    public Set<Class<?>> getClassesByRules(ClassFindRules rules){
        return allClassResources.stream().filter(rules::isMatch).collect(Collectors.toSet());
    }

    public Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotationClass){
        return getClassesByRules(c->AnnotationUtils.isExist(c,annotationClass));
    }

    public Set<Class<?>> getClassesByAnnotationStrengthen(Class<? extends Annotation> annotationClass){
        return getClassesByRules(c->AnnotationUtils.strengthenIsExist(c,annotationClass));
    }

    public Set<Class<?>> getClassesByType(Class<?> tClass){
        return getClassesByRules(tClass::isAssignableFrom);
    }

    @SafeVarargs
    public final Set<Class<?>> getClassesByAnnotationArrayOR(Class<? extends Annotation>... annotationClass){
        return getClassesByRules((c)->{
            for (Class<? extends Annotation> aClass : annotationClass) {
                if(AnnotationUtils.isExist(c,aClass)){
                    return true;
                }
            }
            return false;
        });
    }

    @SafeVarargs
    public final Set<Class<?>> getClassesByAnnotationArrayORStrengthen(Class<? extends Annotation>... annotationClass){
        return getClassesByRules((c)->{
            for (Class<? extends Annotation> aClass : annotationClass) {
                if(AnnotationUtils.strengthenIsExist(c,aClass)){
                    return true;
                }
            }
            return false;
        });
    }

    public Set<Class<?>> getClassByTypeArrayOR(Class<?>...tClass){
        return getClassesByRules((c)->{
            for (Class<?> aClass : tClass) {
                if(aClass.isAssignableFrom(c)){
                    return true;
                }
            }
            return false;
        });
    }
}
