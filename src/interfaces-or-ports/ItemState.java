

package com.team12.lostandfound.interfaces;

import com.team12.lostandfound.domain.AuthenticatedUser;
import com.team12.lostandfound.domain.ChainOfCustodyEntry;
import com.team12.lostandfound.domain.Item;
import com.team12.lostandfound.domain.ItemStatus;

/**
 * State port for the Item lifecycle. Each status (LOST, FOUND, RESOLVED) is
 * implemented by a state class. The Item delegates transition requests to
 * its current state.
 *
 * The transitionTo method returns a ChainOfCustodyEntry. This is the mechanism
 * that guarantees every status change produces an audit entry (BR-07). A
 * transition that does not produce an entry cannot be expressed through this
 * interface.
 *
 * See docs/pattern-decisions.md Section 3.2 (Adapter is for external systems;
 * the State pattern is documented in ADR-002 Section 2).
 */
public interface ItemState {

    boolean canTransitionTo(ItemStatus newStatus);

    ChainOfCustodyEntry transitionTo(
            Item item,
            ItemStatus newStatus,
            AuthenticatedUser actor,
            String reason
    );
}