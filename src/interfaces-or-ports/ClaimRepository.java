

package com.team12.lostandfound.interfaces;

import com.team12.lostandfound.domain.Claim;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for Claims.
 *
 * See docs/pattern-decisions.md Section 3.1.
 */
public interface ClaimRepository {

    void save(Claim claim);

    Optional<Claim> findById(UUID claimId);

    /**
     * Find the active (PENDING) claim on a given item by a given claimant.
     * Used to enforce BR-08 (one active claim per item per user).
     */
    Optional<Claim> findActive(UUID itemId, UUID claimantId);
}