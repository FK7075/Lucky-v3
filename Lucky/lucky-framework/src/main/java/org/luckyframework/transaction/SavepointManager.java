package org.luckyframework.transaction;

/**
 * @author fk
 * @version 1.0
 * @date 2021/4/15 0015 14:43
 */
public interface SavepointManager {


    Object createSavepoint() throws TransactionException;


    void rollbackToSavepoint(Object savepoint) throws TransactionException;


    void releaseSavepoint(Object savepoint) throws TransactionException;
}
