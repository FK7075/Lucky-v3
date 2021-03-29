package org.luckyframework.t1;

import org.luckyframework.context.annotation.Bean;
import org.luckyframework.context.annotation.Conditional;
import org.luckyframework.context.annotation.Configuration;
import org.luckyframework.context.annotation.Import;
import org.luckyframework.impotrBeanDefinitionRegistrar.MyAutoBeanDefinitionRegistrar;
import org.luckyframework.t1.condition.MacCondition;
import org.luckyframework.t1.condition.WindowsCondition;
import org.luckyframework.t1.service.FileCommand;
import org.luckyframework.t1.service.MacFileCommand;
import org.luckyframework.t1.service.WindowsFileCommand;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/26 0026 15:31
 */
@Configuration
@Import(MyAutoBeanDefinitionRegistrar.class)
public class ConfigBean {

    @Bean
    @Conditional(MacCondition.class)
    public FileCommand mac(){
        return new MacFileCommand();
    }

    @Bean
//    @Conditional(WindowsCondition.class)
    public FileCommand windows(){
        return new WindowsFileCommand();
    }

    @Bean
    public void tt2(){
        System.out.println("54545");
    }

    @Bean
    public void tt(){
        System.out.println("54545");
    }
}
