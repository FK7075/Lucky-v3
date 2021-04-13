package org.luckyframework.beans;

import com.lucky.utils.base.Assert;
import com.lucky.utils.base.BaseUtils;
import com.lucky.utils.type.AnnotatedElementUtils;
import org.luckyframework.context.annotation.Component;

/**
 * 起名器
 * @author fk
 * @version 1.0
 * @date 2021/4/13 0013 16:02
 */
public interface Namer {

    static String getBeanName(Class<?> componentClass){
        Component component = AnnotatedElementUtils.findMergedAnnotation(componentClass, Component.class);
        //如果
        if(component != null){
            String confName = component.value();
            if(!Assert.isBlankString(confName)){
                return confName;
            }
        }
        String simpleName = componentClass.getSimpleName();
        if("".equals(simpleName)){
            String classStr = componentClass.toString();
            simpleName=classStr.substring(classStr.lastIndexOf(".")+1);
        }
        return BaseUtils.lowercaseFirstLetter(simpleName);
    }
}
