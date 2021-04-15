package org.luckyframework.transaction;

/**
 * 静态的不可修改的事务定义。
 * @author fk
 * @version 1.0
 * @date 2021/4/15 0015 14:37
 */
final class StaticTransactionDefinition implements TransactionDefinition {

    static final StaticTransactionDefinition INSTANCE = new StaticTransactionDefinition();

    private StaticTransactionDefinition() {
    }

}