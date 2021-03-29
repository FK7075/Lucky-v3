package org.luckyframework.context;

import org.luckyframework.beans.BeanDefinition;

import java.util.List;

/**
 * BeanDefinition的读取器
 * @author fk
 * @version 1.0
 * @date 2021/3/24 0024 11:19
 */
public interface BeanDefinitionReader {

    /**
     * 获取解析得到的BeanDefinition
     */
    List<BeanDefinitionPojo> getBeanDefinitions();




    class BeanDefinitionPojo{
        private String beanName;
        private BeanDefinition definition;

        public BeanDefinitionPojo(String beanName, BeanDefinition definition) {
            this.beanName = beanName;
            this.definition = definition;
        }

        public String getBeanName() {
            return beanName;
        }

        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }

        public BeanDefinition getDefinition() {
            return definition;
        }

        public void setDefinition(BeanDefinition definition) {
            this.definition = definition;
        }
    }
}
