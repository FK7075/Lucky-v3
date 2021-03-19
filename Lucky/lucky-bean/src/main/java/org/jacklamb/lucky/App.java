package org.jacklamb.lucky;

import com.lucky.utils.fileload.Resource;
import com.lucky.utils.fileload.resourceimpl.PathMatchingResourcePatternResolver;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        PathMatchingResourcePatternResolver patternResolver =new PathMatchingResourcePatternResolver();
        Resource[] resources = patternResolver.getResources("classpath:**/*.class");
        for (Resource resource : resources) {
//            System.out.println(resource.getDescription());
            System.out.println(resource.getDescription());
        }

    }
}
