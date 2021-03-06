package org.luckyframework.beans;

import org.luckyframework.context.annotation.Order;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/14 0014 15:50
 */
public interface Ordered {


    /**
     * Useful constant for the highest precedence value.
     * @see java.lang.Integer#MIN_VALUE
     */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    /**
     * Useful constant for the lowest precedence value.
     * @see java.lang.Integer#MAX_VALUE
     */
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;


    /**
     * Get the order value of this object.
     * <p>Higher values are interpreted as lower priority. As a consequence,
     * the object with the lowest value has the highest priority (somewhat
     * analogous to Servlet {@code load-on-startup} values).
     * <p>Same order values will result in arbitrary sort positions for the
     * affected objects.
     * @return the order value
     * @see #HIGHEST_PRECEDENCE
     * @see #LOWEST_PRECEDENCE
     */
    int getOrder();

    static int getPriority(Object bean){
        if(bean instanceof PriorityOrdered){
            return ((PriorityOrdered) bean).getOrder();
        }
        if(bean instanceof Ordered){
            return ((Ordered)bean).getOrder();
        }
        Order order = bean.getClass().getAnnotation(Order.class);
        return order == null ? Ordered.LOWEST_PRECEDENCE : order.value();
    }
}
