package org.luckyframework.transaction;

import com.lucky.utils.annotation.Nullable;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/15 0015 14:35
 */
public interface TransactionDefinition {


    /** 如果父方法没有事务，子方法会新建事务，如果父方法有事务，则子方法沿用父方法的事务*/
    int PROPAGATION_REQUIRED = 0;

    int PROPAGATION_SUPPORTS = 1;

    /** 子方法新建事务，如果父方法没有事务会抛出异常，*/
    int PROPAGATION_MANDATORY = 2;

    int PROPAGATION_REQUIRES_NEW = 3;

    int PROPAGATION_NOT_SUPPORTED = 4;

    /** 子方法始终不会有事务，如果父方法有事务直接抛出异常，父方法没有事务则正常执行 */
    int PROPAGATION_NEVER = 5;

    /** 子方法新建事务，如果父方法有事务则将子方法嵌套在父方法的事务之中
     *  嵌套：即父方法的事务将会影响子方法事务
     *  （子方法事务执行成功，父方法事务执行失败时，子方法的事务也会回滚）
     */
    int PROPAGATION_NESTED = 6;

    int ISOLATION_DEFAULT = -1;

    int ISOLATION_READ_UNCOMMITTED = 1;  // same as java.sql.Connection.TRANSACTION_READ_UNCOMMITTED;

    int ISOLATION_READ_COMMITTED = 2;  // same as java.sql.Connection.TRANSACTION_READ_COMMITTED;

    int ISOLATION_REPEATABLE_READ = 4;  // same as java.sql.Connection.TRANSACTION_REPEATABLE_READ;

    int ISOLATION_SERIALIZABLE = 8;  // same as java.sql.Connection.TRANSACTION_SERIALIZABLE;

    int TIMEOUT_DEFAULT = -1;

    /**
     * 返回事务的传播行为
     */
    default int getPropagationBehavior() {
        return PROPAGATION_REQUIRED;
    }

    /**
     * 返回事务的隔离级别
     */
    default int getIsolationLevel() {
        return ISOLATION_DEFAULT;
    }

    /**
     * 返回事务超时时间,以秒为单位,同样只有在创建新事务时才有效
     */
    default int getTimeout() {
        return TIMEOUT_DEFAULT;
    }

    /**
     * 是否优化为只读事务，支持这项属性的事务管理器会将事务标记为只读，
     * 只读事务不允许有写操作，不支持只读属性的事务管理器需要忽略这项设置，
     * 这一点跟其他事务属性定义不同，针对其他不支持的属性设置，事务管理器应该抛出异常。
     */
    default boolean isReadOnly() {
        return false;
    }

    /**
     * 返回事务名称，声明式事务中默认值为"类的完全限定名.方法名"
     */
    @Nullable
    default String getName() {
        return null;
    }

    // Static builder methods
    static TransactionDefinition withDefaults() {
        return StaticTransactionDefinition.INSTANCE;
    }
}
