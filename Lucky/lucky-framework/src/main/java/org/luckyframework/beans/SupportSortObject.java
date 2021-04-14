package org.luckyframework.beans;

import java.util.Comparator;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/14 0014 17:33
 */
public class SupportSortObject<T> {

    private int priority;
    private T obj;

    public SupportSortObject(int priority, T obj) {
        this.priority = priority;
        this.obj = obj;
    }

    public T getObject() {
        return obj;
    }

    public int getPriority() {
        return priority;
    }
}
