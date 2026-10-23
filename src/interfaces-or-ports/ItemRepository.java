

package com.team12.lostandfound.interfaces;

import com.team12.lostandfound.domain.Item;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for Items. The application layer depends on this interface,
 * not on a concrete database client. The adapter implementation
 * (PostgresItemRepository) lives in src/adapters/persistence/.
 *
 * See docs/pattern-decisions.md Section 3.1 and decisions/ADR-002.md.
 */
public interface ItemRepository {

    /**
     * Persist the item. If the item already exists (same itemId), it is updated;
     * otherwise it is inserted. The primary database is mutable, so update is legal here.
     */
    void save(Item item);

    /**
     * Retrieve an item by its identifier.
     */
    Optional<Item> findById(UUID itemId);

    /**
     * Find an item created by the given reporter with matching category, description,
     * and location, created at or after the given instant. Used for duplicate detection
     * within the 24-hour window (BR-01).
     */
    Optional<Item> findDuplicate(
            UUID reporterId,
            String category,
            String description,
            String location,
            Instant since
    );
}