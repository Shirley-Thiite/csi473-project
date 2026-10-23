

package com.team12.lostandfound.interfaces;

import com.team12.lostandfound.domain.ChainOfCustodyEntry;
import java.util.List;
import java.util.UUID;

/**
 * Append-only persistence port for the Chain of Custody event log.
 *
 * IMPORTANT: This interface intentionally has NO update and NO delete method.
 * The append-only guarantee (BR-07, QS-07) is a structural property of the code.
 * A developer cannot modify or remove a custody entry through this interface,
 * even accidentally.
 *
 * The concrete adapter (EventStoreCustodyRepository) uses a database role with
 * INSERT-only privileges, so the guarantee is enforced at both the code level
 * and the database level.
 *
 * See docs/pattern-decisions.md Section 3.1 and decisions/ADR-002.md.
 */
public interface CustodyRepository {

    /**
     * Append a new custody entry. There is no return value because the entry
     * cannot fail in a way that the caller can recover from; if the append fails,
     * the surrounding transaction rolls back.
     */
    void append(ChainOfCustodyEntry entry);

    /**
     * Retrieve the full custody history for an item, ordered by timestamp ascending.
     * Read-only. The returned list is immutable; callers cannot modify it to
     * alter the audit trail.
     */
    List<ChainOfCustodyEntry> findByItemId(UUID itemId);
}