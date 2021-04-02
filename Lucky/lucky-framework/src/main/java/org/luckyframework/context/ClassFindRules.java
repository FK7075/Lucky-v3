package org.luckyframework.context;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/2 0002 10:33
 */
@FunctionalInterface
public interface ClassFindRules {

    boolean isMatch(Class<?> matchedClass);

}
