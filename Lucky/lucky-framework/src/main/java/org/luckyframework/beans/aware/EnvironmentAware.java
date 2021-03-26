package org.luckyframework.beans.aware;

import org.luckyframework.environment.Environment;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/26 0026 11:56
 */
public interface EnvironmentAware extends Aware {

    void setEnvironment(Environment environment);
}
