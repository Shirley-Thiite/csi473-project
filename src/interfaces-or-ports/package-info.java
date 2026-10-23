

/**
 * Interfaces (ports) that the core application logic depends on.
 *
 * The dependency direction is:
 *   application -> interfaces <- adapters
 *
 * Adapters live in src/adapters/ and are injected at startup.
 * Domain entities live in src/domain/ and depend on nothing external.
 *
 * The most important interface is CustodyRepository, which enforces the
 * append-only guarantee by having no update or delete method.
 *
 * See docs/pattern-decisions.md and decisions/ADR-002.md for the reasoning.
 */
package com.team12.lostandfound.interfaces;