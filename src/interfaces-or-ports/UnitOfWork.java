

package com.team12.lostandfound.interfaces;

/**
 * Transaction boundary. The application service opens a unit of work,
 * performs its writes, and commits or rolls back. The Item write and the
 * custody append must happen inside the same unit of work so that a
 * failure in either rolls back both (BR-07, QS-07).
 *
 * See models/failure-recovery.md.
 */
public interface UnitOfWork {

    void begin();

    void commit();

    void rollback();
}