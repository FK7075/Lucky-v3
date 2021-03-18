package org.jacklamb.lucky.aop.advisor;

import java.util.List;

/**
 * 切面的注册表
 * @author fk
 * @version 1.0
 * @date 2021/3/18 0018 14:43
 */
public interface AdvisorRegistry {

    /**
     * 注册一个Advisor
     * @param advisor Advisor
     */
    void registryAdvisor(Advisor advisor);

    /**
     * 获取所有的Advisor
     * @return 所有的Advisor
     */
    List<Advisor> getAdvisors();
}
