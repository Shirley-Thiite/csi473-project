

package com.team12.lostandfound.interfaces;

import com.team12.lostandfound.application.ReportResult;
import java.util.Optional;
import java.util.UUID;

/**
 * Store for idempotency keys used by the Report Found Item operation.
 * If the same key is seen twice within 24 hours for the same actor, the
 * original result is returned rather than creating a duplicate.
 *
 * See docs/api-contracts/core-operation.md Section 8.
 */
public interface IdempotencyStore {

    Optional<ReportResult> find(String key, UUID actorId);

    void save(String key, UUID actorId, ReportResult result);
}