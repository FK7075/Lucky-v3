package org.luckyframework.transaction;

import java.io.Flushable;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/15 0015 14:42
 */
public interface TransactionStatus extends TransactionExecution, SavepointManager, Flushable {

    boolean hasSavepoint();

    @Override
    void flush();
}
