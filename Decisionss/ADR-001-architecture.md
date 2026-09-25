ADR-001: Architecture Selection for the Campus Lost and Found Management System

Project: Campus Lost and Found Management System
Team: CSI473 Team 12
Date: 25 September 2026
Branch: lab-07
Status: Accepted

 1. Context

The Campus Lost and Found Management System must satisfy four architectural drivers that have a measurable effect on the structure of the system. These drivers were identified in docs/architecture-options.md and are summarised here because they are the reason this decision is necessary.

AD-01: Auditability and Non-Repudiation. Sources: FR-22, BR-07, QS-07, Approval Condition 4. Every item status change must be recorded in an append-only store with no update or delete operations, retained for at least three years. The threat model includes a compromised or malicious administrator, so the immutability guarantee must be structural rather than procedural.

AD-02: Secure Handling of Private Evidence. Sources: FR-25, FR-27, FR-28, BR-04, QS-03, Approval Conditions 2 and 3. Private evidence must be encrypted at rest and in transit, stored separately from public item data, and accessible only to the Administrator role through RBAC enforcement at the data access level.

AD-03: Integration with University Identity and Mobile Access. Sources: FR-01, FR-02, FR-29, QS-02, QS-04, Approval Conditions 1 and 5. Authentication must be delegated to the university's OIDC provider, the interface must be mobile-responsive, and the system must maintain at least ninety-nine percent uptime during core hours.

AD-04: Category Maintainability. Sources: FR-06, QS-05. Item categories must be stored as configuration data, not as hardcoded enums or code, so that a new category can be added in production within thirty minutes without a code deployment.

The team is a small student group of four to five members with limited teaching weeks and bounded infrastructure. The system must be delivered as a semester-sized vertical slice, not a full production rollout. This constraint is not a driver in the architectural sense, but it is decisive in the choice between alternatives.

 2. Decision

We will implement Alternative A: a modular monolith with an event-sourced audit log.

The system will be a single deployable web application with clearly separated modules for Auth, Report, Claim, Admin, Matching, Notification, Chain of Custody, and Evidence. The Chain of Custody module will be backed by an append-only event store with no update or delete operations. The Evidence module will store encrypted blobs in a separate store from the primary database, with RBAC enforcement in the module layer rather than the database query layer. Authentication will be delegated to the university OIDC provider. The front end will be server-rendered with a responsive CSS framework, with a REST API exposed for future mobile clients. Item categories will be stored as configuration data in the primary database and read by the Report module at runtime.

The key architectural property is that the audit log is not a table the application writes to as a side effect. It is a module whose only operation is append. There is no update method, no delete method, and no admin override. The Report module does not have direct write access to the event store.

 3. Alternatives Considered

 3.1 Alternative B: Microservices with Separate Audit and Evidence Services

The system is decomposed into independent services: Auth, Item, Claim, Audit, Evidence, Notification, and an API Gateway. Each service has its own datastore and communicates over REST or a message queue. The Audit service uses write-once storage. The Evidence service uses a key management service for encryption. The front end is a single-page application that calls the API gateway.

This alternative provides stronger process-level isolation in theory. A bug in the Item service cannot corrupt the Audit service. The Evidence service can be scaled or secured independently. However, it requires seven deployment pipelines, seven sets of logs, seven failure modes, and a service mesh or API gateway to manage routing and authentication between services. For a team of four to five students in a single semester, this is not a realistic operational burden.

The audit requirement is also harder to satisfy in a distributed system. If the Item service updates an item's status and the call to the Audit service fails, the system is inconsistent unless a saga or outbox pattern is implemented. That is additional code that must be written and tested, and it is code that Alternative A does not need because both operations happen in the same transaction.

 3.2 Options Rejected Before Full Comparison

A pure three-tier MVC application with a standard relational database was rejected because it cannot satisfy AD-01 without significant additional machinery. A mutable log table satisfies the letter of the audit requirement but not its spirit, because the threat model includes a compromised or malicious administrator. It also tends to blur the evidence boundary that AD-02 requires.

A serverless function-per-endpoint design was rejected because the audit and custody requirements demand transactional consistency that is awkward to achieve across stateless functions. If the function that updates an item's status and the function that appends to the audit log are separate, a failure between them leaves the system inconsistent.

A full event-sourced system with CQRS was rejected as over-engineering for a semester project. We need event sourcing for the custody log, not for the entire domain. Applying it to every entity would multiply the code and the operational complexity without a corresponding benefit.

 4. Consequences

 4.1 Positive Consequences

The auditability requirement is satisfied structurally rather than procedurally. The event store has no update or delete operations, and the only code path to it is through a module that appends. This is not a policy an administrator could override; it is a property of the code. The evidence privacy requirement is satisfied because the Evidence module is the only code that reads from the encrypted blob store, and it performs its own RBAC check. A developer writing a new query in the Report module cannot accidentally expose evidence because the Report module does not have a reference to the blob store.

The system is feasible for a small team to build and operate. A single deployable means one repository, one CI pipeline, and one database cluster. More of the semester is spent on the actual lost-and-found workflows and less on infrastructure. The quality scenarios that depend on operational reliability, such as uptime and graceful error handling, are more likely to be met because there are fewer things to configure correctly.

Data consistency is strong because all operations happen in a single transaction. There is no risk of the item state and the audit log diverging, which is particularly important for AD-01 where an unlogged status change would be a failure.

Category maintainability is satisfied because categories are configuration data, not code. Adding a category is a data change, not a code change.

 4.2 Negative Consequences

The system will not scale horizontally without modification. A modular monolith scales vertically, not horizontally. If the system were deployed across multiple campuses with tens of thousands of users, the single deployable would become a bottleneck. The Phase 1 scope explicitly excludes multi-campus support, and the expected load is a single university's lost-and-found traffic, which is well within the capacity of a single well-provisioned server. But the limitation is real, and addressing it later would require extracting the Evidence and Chain of Custody modules into separate services.

Deployment coupling is a consequence of the single deployable. A change to any module requires redeploying the whole application. For a semester project with infrequent releases, this is acceptable. If release frequency increased, it would become painful.

Module boundaries can erode over time if the team is not disciplined. In a monolith, it is easy to reach across modules and call a function directly, bypassing the intended interface. If we allow the Report module to write to the Evidence store directly, we lose the structural guarantee that evidence is only accessible through the Evidence module.

The event store will grow indefinitely. Every status change appends a row, and rows are never deleted. Over three years, this could accumulate to a significant size. We will archive events older than three years to cold storage, but the active event store will still contain three years of history.

The front end is server-rendered rather than a single-page application. This is a deliberate choice because server-rendered forms are easier to make accessible and mobile-responsive, and they degrade gracefully on older browsers. The cost is that the user experience is less fluid than a modern SPA.

 5. Risks

The highest architectural risk is that the event store grows faster than expected and that query performance degrades. We will monitor event store size and custody history query time, and we will add indexing or archiving sooner if needed.

The second risk is that the university's OIDC provider changes its configuration or becomes unavailable. We will implement a fallback local authentication mode for development and testing, and we will document the integration points so that a change in the provider's configuration can be accommodated without rewriting the Auth module.

The third risk is that the team does not maintain module boundaries under time pressure. If we find that modules are becoming coupled, we will revisit the architecture and consider extracting the most entangled module into a separate service.

 6. Evidence That Would Trigger Reconsideration

The decision recorded here is valid under the current scope and constraints. It would need to be revisited if any of the following become true:

- The system needs to support multiple campuses, which would create load that a single deployable cannot handle.
- The audit log exceeds ten million events per year with degraded query performance, which would require archiving or a dedicated audit service.
- The team grows to eight or more developers and deployment frequency becomes a bottleneck, which would make the deployment coupling of a monolith painful.
- The university mandates a microservices-based deployment for security isolation, which would override the feasibility argument for the monolith.

None of these are true today. They are the conditions under which Alternative B would become the better choice.

 7. Traceability

| Quality Scenario        | How This Architecture Addresses It                                                            |
| QS-03 (Security)        | Evidence Module with encrypted blob store; RBAC middleware; separate storage from public data |
| QS-07 (Data Integrity)  | Append-only event store; 100% of status changes logged with timestamp and actor               |
| QS-02 (Availability)    | Single deployable simplifies high-availability setup; health checks and graceful degradation  |
| QS-05 (Maintainability) | Modular structure allows adding categories via database configuration                         |
| QS-04 (Usability)       | Server-rendered responsive forms; clear validation                                            |
| QS-01 (Performance)     | Indexed queries on Primary DB; pagination for large result sets                               |
| QS-06 (Reliability)     | Single connection pool with retry logic; clear error boundaries in Report module              |

The full comparison of alternatives is in docs/architecture-options.md. The component structure is in models/component-architecture.puml and models/component-architecture.svg. The quality-to-architecture traceability matrix is in docs/quality-to-architecture.md. The Lab 07 evidence index is in evidence/lab-07/README.md.

 8. Review

This ADR was reviewed by all team members on 25 September 2026. The highest architectural risk was identified as event store growth and query performance over three or more years. The team agreed to monitor this risk and to revisit the decision if the reconsideration triggers in Section 6 become true.

 9. Exit Record

The quality requirement that most influenced this decision was AD-01: Auditability and Non-Repudiation, derived from QS-07 and BR-07. It forced the event-sourced design of the Chain of Custody module and the module boundary that prevents any other module from writing directly to the event store. Without this requirement, a simpler CRUD-based architecture would have been sufficient.