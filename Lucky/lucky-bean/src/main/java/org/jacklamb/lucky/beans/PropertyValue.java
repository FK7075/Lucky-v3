package org.jacklamb.lucky.beans;

/**
 * 属性值依赖实体
 * @author fk
 * @version 1.0
 * @date 2021/3/12 0012 16:59
 */
public class PropertyValue {

    private String name;
    private Object value;

    public PropertyValue(){}

    public PropertyValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
