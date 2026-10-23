

package com.team12.lostandfound.interfaces;

import com.team12.lostandfound.domain.AuthenticatedUser;
import com.team12.lostandfound.domain.SessionToken;
import java.util.Optional;

/**
 * Port for the external identity provider. The application logic depends on
 * this interface, not on the OIDC client library. If the university changes
 * its OIDC claim structure or the library is replaced, only the adapter
 * (UniversityOidcAdapter) changes.
 *
 * See docs/pattern-decisions.md Section 3.2 and docs/framework-decision.md Section 2.4.
 */
public interface IdentityProvider {

    /**
     * Verify the bearer token and return the corresponding authenticated user.
     * Returns empty if the token is missing, expired, or invalid.
     */
    Optional<AuthenticatedUser> verifyToken(String token);

    /**
     * Issue a session token for the given authenticated user after login.
     */
    SessionToken issueSession(AuthenticatedUser user);
}