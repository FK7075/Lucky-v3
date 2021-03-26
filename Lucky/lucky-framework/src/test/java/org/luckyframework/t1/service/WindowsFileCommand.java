package org.luckyframework.t1.service;

import org.luckyframework.context.annotation.Conditional;
import org.luckyframework.context.annotation.Service;
import org.luckyframework.t1.condition.MacCondition;
import org.luckyframework.t1.condition.WindowsCondition;

/**
 * @author fk
 * @version 1.0
 * @date 2021/3/26 0026 15:37
 */
public class WindowsFileCommand implements FileCommand{
    @Override
    public String getCommand() {
        return "dir";
    }
}
