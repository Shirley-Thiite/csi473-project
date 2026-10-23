

package com.team12.lostandfound.interfaces;

import com.team12.lostandfound.domain.AuthenticatedUser;
import com.team12.lostandfound.domain.EvidenceInput;
import java.util.UUID;

/**
 * Persistence port for private evidence. The adapter stores encrypted blobs
 * in the encrypted blob store, separate from the primary database (BR-04, FR-27).
 * RBAC is enforced inside the adapter; callers cannot bypass it.
 *
 * See docs/pattern-decisions.md Section 3.1.
 */
public interface EvidenceRepository {

    /**
     * Store evidence for an item, associated with the given claim or reporter.
     * The actor is required because RBAC is enforced at this boundary; only
     * authorized roles may store evidence.
     *
     * @return the opaque blob identifier assigned to the stored evidence
     */
    UUID store(UUID itemId, EvidenceInput evidence, AuthenticatedUser actor);
}