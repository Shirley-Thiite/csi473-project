Pattern Decisions — Campus Lost and Found Management System

Project: Campus Lost and Found Management System
Team: CSI473 Team 12
Date: 9 October 2026
Branch: lab-09


1. Purpose

This document records the design patterns and techniques we selected for the Campus Lost and Found Management System, and the patterns and technologies we rejected. It starts with the project-specific design problems, not with the patterns. A pattern is only recorded here if it solves a named problem in this project.

The document is written to be consistent with the Lab 08 artefacts (models/logical-data-model.md, docs/api-contracts/core-operation.md, models/deployment.md, models/failure-recovery.md, docs/data-integrity.md) and with the Lab 07 architecture decision (decisions/ADR-001-architecture.md). Where a pattern decision refines or extends the architecture, the link is stated.


2. Design Problems

We identified five design problems in the Campus Lost and Found Management System. Each one is a concrete problem that appears in the code we are about to write, not a generic concern.

2.1 Problem 1: Persistence Coupling

Where it appears. The Report module needs to write an Item to the Primary DB, append a Chain of Custody entry to the Event Store, and optionally store an evidence blob in the Encrypted Blob Store. If the Report module calls PostgreSQL, the event store client, and the blob store SDK directly, it becomes coupled to three storage technologies.

Why it matters. The architecture's structural guarantee is that the Item write and the custody append happen in the same transaction. If the Report module is tightly coupled to both stores, the transactional boundary is hard to maintain and hard to test.

What we need. A boundary that hides the storage technology behind a stable interface, so that the domain logic depends on an abstraction rather than on a concrete driver.

2.2 Problem 2: Variable Behaviour at the Storage Boundary

Where it appears. The Chain of Custody store has a rule: append-only, no update, no delete. The Primary DB has no such rule. The Evidence store has a different rule: encrypted at rest, RBAC-enforced. Three stores, three different sets of rules. If the domain logic has to remember which rules apply to which store, the rules will eventually be violated by accident.

Why it matters. BR-07 (chain of custody completeness) is a structural guarantee, not a policy. If the rule is only enforced by convention, a future developer can bypass it.

What we need. A storage abstraction where each store type exposes only the operations that are legal for it.

2.3 Problem 3: External Format Instability

Where it appears. The application receives authentication tokens from the University OIDC Provider. The token format, claims, and signing keys are controlled by the university, not by us. If the Report module reads claims directly from the token, a change in the provider's claim structure breaks the Report module.

Why it matters. ADR-001 lists OIDC provider changes as a risk. The design must isolate the token parsing so that a change affects one module, not the whole codebase.

What we need. An interface that converts the provider's token into the application's own AuthenticatedUser value object.

2.4 Problem 4: Secondary Effects

Where it appears. When a claim is approved, the system must update the item status, append to the chain of custody, create a handover record, and notify the claimant. These are four effects that need to happen together. If they are scattered across the code, one of them will eventually be forgotten.

Why it matters. BR-05 (handover within 7 days) and BR-07 (chain of custody completeness) both depend on all four effects happening.

What we need. A way to coordinate the secondary effects without turning the Claim module into a procedural script that is hard to test.

2.5 Problem 5: Object Creation Complexity

Where it appears. An Item can be created by a Lost report or a Found report. The two paths share most of the fields but differ in reportType, initial status, and the direction of the custody entry. If the Item constructor takes every possible field, callers can create invalid Items by passing the wrong combination.

Why it matters. BR-02 (status transition order) depends on an Item starting in the correct status for its report type.

What we need. A way to construct Items that guarantees the correct initial state for each report type.


3. Pattern Decisions

We selected three patterns and rejected two others. Each pattern is linked to a named design problem from Section 2.

3.1 Pattern 1: Repository

Design problem solved. Problem 1 (Persistence Coupling) and Problem 2 (Variable Behaviour at the Storage Boundary).

Why this pattern fits. The Repository pattern hides the storage technology behind a stable interface. The domain logic depends on ItemRepository, CustodyRepository, and EvidenceRepository, not on PostgreSQL, the event store client, or the blob store SDK. The CustodyRepository interface exposes only append and findByItemId, which makes the append-only guarantee structural rather than procedural. There is no update method to call.

Participants.

- ItemRepository (interface) — the port
- PostgresItemRepository (class) — the adapter
- CustodyRepository (interface) — the port
- EventStoreCustodyRepository (class) — the adapter
- EvidenceRepository (interface) — the port
- EncryptedBlobEvidenceRepository (class) — the adapter
- ReportFoundItemService — the client that depends on the interfaces

Dependency direction. The service depends on the interfaces. The adapters implement the interfaces. The interfaces do not know about the adapters. This is dependency inversion.

Consequence accepted. The Repository pattern adds an interface layer between the service and the database. This is more code than calling the database directly. The benefit is that the domain logic can be tested without a database, and the append-only guarantee is enforced by the interface rather than by convention. We accept the additional code because BR-07 is a structural guarantee, not a policy.

Alternative considered. Direct database calls from the service. Rejected because it couples the service to three storage technologies and makes the transactional guarantee harder to maintain.

3.2 Pattern 2: Adapter

Design problem solved. Problem 3 (External Format Instability).

Why this pattern fits. The Adapter pattern isolates the provider-specific token parsing in a single class. The rest of the application depends on the IdentityProvider interface and the AuthenticatedUser value object. If the university changes its OIDC claim structure, only UniversityOidcAdapter needs to change.

Participants.

- IdentityProvider (interface) — the port
- UniversityOidcAdapter (class) — the adapter
- AuthenticatedUser (value object) — the stable type the rest of the application depends on
- SessionToken (value object) — the session token issued after authentication

Dependency direction. The domain logic depends on IdentityProvider and AuthenticatedUser. The UniversityOidcAdapter implements IdentityProvider. The adapter is the only class that knows the raw OIDC claim structure.

Consequence accepted. The Adapter pattern adds a translation layer. The domain logic cannot access raw OIDC claims; it can only access the fields that AuthenticatedUser exposes. This is a deliberate restriction. If a new claim is needed, the adapter is updated and the AuthenticatedUser value object is extended.

Alternative considered. Parsing the token directly in the Auth module and passing the raw claims to the service. Rejected because it couples the domain logic to the provider's claim structure, which is a documented risk in ADR-001.

3.3 Pattern 3: Factory Method

Design problem solved. Problem 5 (Object Creation Complexity).

Why this pattern fits. The Factory Method pattern centralizes the construction logic for Items. The ItemFactory has two methods: createFoundItem and createLostItem. Each method sets the correct initial status and report type. Callers cannot create an Item with a mismatched status and report type because the factory does not expose a general-purpose constructor.

Participants.

- ItemFactory (class) — the factory
- Item (entity) — the product
- SubmitFoundReportCommand and SubmitLostReportCommand (commands) — the inputs
- AuthenticatedUser (value object) — the actor

Dependency direction. The service depends on the factory. The factory depends on the Item entity. The Item entity has no dependencies on the factory or the service.

Consequence accepted. The Factory pattern adds a class that would not otherwise be needed. The benefit is that the invariant "a Found report creates an Item with status FOUND and reportType FOUND" is enforced at construction time, not at validation time. This is stronger than a validation check because an invalid Item cannot exist, even transiently.

Alternative considered. A single Item constructor with all fields, plus validation in the service. Rejected because it allows the service to construct an invalid Item and rely on a separate validation step to catch it.

4. Pattern Considered and Rejected

4.1 Observer / Domain Events (Deferred)

Design problem that could justify it. Problem 4 (Secondary Effects): when a claim is approved, four effects must happen — update item status, append to custody, create handover record, notify claimant. The Observer pattern would let the Claim module publish a ClaimApproved event, and each of the four effects would be handled by a subscriber.

Why we deferred it. For a semester project, the number of subscribers is small (four), and the effects are tightly coupled to the claim approval. Introducing an event bus adds infrastructure (a message broker or an in-process event dispatcher) and makes the flow harder to trace. The simpler alternative is to have the Claim module call the four effects directly in a single transaction, with the four calls clearly grouped in one method. If the number of subscribers grows, or if the effects need to be retried independently, the Observer pattern can be introduced later.

Consequence accepted. The Claim module is responsible for coordinating the four effects. This is a procedural approach, but it is transparent and testable. The cost is that the Claim module knows about all four effects; if a fifth effect is added later, the Claim module must be modified. We accept this because the number of effects is small and stable.

Reconsideration trigger. If the number of secondary effects on claim approval exceeds six, or if any effect needs to be retried independently of the others, we will introduce domain events.

4.2 Singleton (Rejected)

What it would be used for. A DatabaseConnection class that guarantees only one connection exists in the application.

Why we rejected it. The Singleton pattern introduces hidden global state, makes testing difficult because tests cannot substitute a different connection, and conflicts with the dependency injection approach we chose for the ports-and-adapters design. The need for a single connection is already met by the connection pool, which is a framework concern, not an application concern. Introducing a Singleton would add accidental complexity without solving a named problem in this project.

Consequence accepted. Connection management is handled by the framework's connection pool. The application code does not manage connections directly; it receives a DataSource through dependency injection. This is simpler and more testable than a Singleton.


5. Pattern Decision Summary

| Pattern                  | Design Problem               | Status         | Consequence Accepted |
| Repository | Problem 1: Persistence Coupling; Problem 2: Variable Behaviour at Storage Boundary | Selected | Additional interface layer; testable domain logic; append-only guarantee enforced by interface |
| Adapter | Problem 3: External Format Instability | Selected | Translation layer; domain logic depends on stable value object, not raw OIDC claims |
| Factory Method | Problem 5: Object Creation Complexity | Selected | Additional factory class; invalid Items cannot exist even transiently |
| Observer / Domain Events | Problem 4: Secondary Effects | Deferred | Claim module coordinates four effects procedurally; simpler and traceable; reconsider if effects exceed six |
| Singleton | (no named problem) | Rejected | Hidden global state and test difficulty; connection management handled by framework |


6. Traceability

| Pattern Decision   | Related Requirement | Related Business Rule | Related Quality Scenario  | Related Architecture Element |
| Repository         | FR-22, FR-25, FR-27 | BR-04, BR-07          | QS-03, QS-07              | Chain of Custody module, Evidence module, Primary DB |
| Adapter            | FR-01, FR-02        | BR-06                 | —                         | Auth module, University OIDC Provider |
| Factory Method     | FR-04, FR-05, FR-06 | BR-02                 | —                         | Report module, Item aggregate root |
| Observer deferred  | FR-17, FR-20, FR-24 | BR-05, BR-07          | — | Claim module, Admin module |
| Singleton rejected | — | — | QS-06 | — |


7. Related Artefacts

The Lab 07 architecture options are in docs/architecture-options.md. The decision record for the architecture is in decisions/ADR-001-architecture.md. The quality-to-architecture traceability is in docs/quality-to-architecture.md. The Lab 08 logical data model is in models/logical-data-model.md. The Lab 08 API contract is in docs/api-contracts/core-operation.md. The Lab 08 deployment model is in models/deployment.md. The Lab 08 failure and recovery model is in models/failure-recovery.md. The Lab 08 data integrity constraints are in docs/data-integrity.md. The Lab 09 detailed class design is in models/detailed-class-design.puml. The Lab 09 framework decision is in docs/framework-decision.md. The Lab 09 framework decision record is in decisions/ADR-002.md. The Lab 09 interface skeletons are in src/interfaces-or-ports/.


8. Exit Record

Pattern or interface boundary with the greatest benefit. The CustodyRepository interface. It solves Problem 2 (Variable Behaviour at the Storage Boundary) by exposing only append and findByItemId, with no update or delete methods. This makes the append-only guarantee structural rather than procedural. A developer who wants to modify a custody entry has no method to call.

Cost of the decision. The interface adds a layer between the Report module and the event store. This is more code than calling the event store directly, and it requires the team to maintain the interface and the adapter as the event store evolves.

Test that will confirm the design works. A test that attempts to call CustodyRepository.update(...) or CustodyRepository.delete(...) should fail to compile, because those methods do not exist on the interface. A test that calls CustodyRepository.append(...) and then queries the event store directly should find exactly one new row. A test that attempts to modify the appended row through the application's database credentials should be rejected by the database.