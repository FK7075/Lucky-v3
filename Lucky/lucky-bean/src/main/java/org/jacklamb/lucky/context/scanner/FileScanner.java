package org.jacklamb.lucky.context.scanner;

import com.lucky.utils.fileload.Resource;
import com.lucky.utils.fileload.resourceimpl.PathMatchingResourcePatternResolver;
import org.jacklamb.lucky.exception.LuckyIOException;

import java.io.IOException;

/**
 * 文件扫描基本接口
 * @author fk7075
 * @version 1.0.0
 * @date 2021/3/22 上午12:29
 */
public interface FileScanner {

    PathMatchingResourcePatternResolver pm = new PathMatchingResourcePatternResolver();

    static Resource[] getResources(String location){
        try {
            return pm.getResources(location);
        } catch (IOException e) {
            throw new LuckyIOException(e);
        }
    }

    static Resource getResource(String location){
        return pm.getResource(location);
    }
}
