package org.luckyframework.context.annotation;

import com.lucky.utils.annotation.Nullable;
import com.lucky.utils.type.AnnotationMetadata;

import java.util.function.Predicate;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/29 0029 9:43
 */
public interface ImportSelector {

    String[] selectImports(AnnotationMetadata importingClassMetadata);

    @Nullable
    default Predicate<String> getExclusionFilter() {
        return null;
    }

}
