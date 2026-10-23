

package com.team12.lostandfound.interfaces;

import com.team12.lostandfound.domain.HandoverRecord;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for HandoverRecords.
 *
 * See docs/pattern-decisions.md Section 3.1.
 */
public interface HandoverRepository {

    void save(HandoverRecord record);

    Optional<HandoverRecord> findByClaimId(UUID claimId);
}