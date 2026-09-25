Architecture Options for the Campus Lost and Found Management System

Project: Campus Lost and Found Management System
Team: CSI473 Team 12
Date: 25 September 2026
Branch: lab-07


1. Purpose

This document compares two realistic architecture alternatives for the Campus Lost and Found Management System. It states what forces are acting on the system, how each alternative responds to those forces, and why one alternative was chosen over the other. The comparison uses the same project-specific criteria for both alternatives, and it records the consequences accepted with the decision, including the negative ones.

The two alternatives are a modular monolith with an event-sourced audit log (Alternative A) and microservices with separate audit and evidence services (Alternative B). Both are genuine, buildable structures for this project.


2. Architecture Drivers and Design Obligations

An architecture driver is a requirement, quality scenario, or constraint that has a measurable effect on the structure of the system. Not every requirement is a driver. "The system shall allow users to update their profile information" does not force any particular structure. "Every status change must be recorded in an immutable log retained for three years" does force a structure, because it rules out whole classes of design.

We identified four architecture drivers.

AD-01: Auditability and Non-Repudiation. Sources: FR-22, BR-07, QS-07, Approval Condition 4. The design obligation is that every item status change must be recorded in an append-only store with no update or delete operations, retained for at least three years. The quality attributes affected are integrity, compliance, and legal defensibility. If ignored, the system cannot resolve disputes and fails the university's retention requirements. The architectural consequence is that the audit trail cannot be a side effect of ordinary CRUD operations. It must be a first-class module with its own storage and its own access rules.

 AD-02: Secure Handling of Private Evidence.  Sources: FR-25, FR-27, FR-28, BR-04, QS-03, Approval Conditions 2 and 3. The design obligation is that private evidence must be encrypted at rest and in transit, stored separately from public item data, and accessible only to the Administrator role through RBAC enforcement at the data access level. The quality attributes affected are security and privacy. If ignored, the system exposes sensitive student information and enables fraudulent claims. The architectural consequence is that evidence cannot share a storage boundary with public listings. It must be isolated so that a bug in a public query cannot leak evidence.

 AD-03: Integration with University Identity and Mobile Access.  Sources: FR-01, FR-02, FR-29, QS-02, QS-04, Approval Conditions 1 and 5. The design obligation is that authentication must be delegated to the university's OIDC provider, the interface must be mobile-responsive, and the system must maintain at least ninety-nine percent uptime during core hours. The quality attributes affected are interoperability, usability, and availability. If ignored, students cannot use their existing credentials, the system is inaccessible on mobile devices, and downtime during peak hours defeats the purpose of a real-time lost-and-found system. The architectural consequence is that the auth integration must be isolated so that a change in the university's configuration does not ripple through the codebase, and the deployment must be simple enough for a small team to keep running.

 AD-04: Category Maintainability.  Sources: FR-06, QS-05. The design obligation is that item categories must be stored as configuration data, not as hardcoded enums or code, so that a new category can be added in production within thirty minutes without a code deployment. The quality attributes affected are modifiability and operational efficiency. If ignored, every new category requires a code change, a test run, and a deployment, which is disproportionate for a data change. The architectural consequence is that the category list must live in the database and be read at runtime by the Report module.

These four drivers do not all point in the same direction. AD-01 and AD-02 favour strong isolation and clear boundaries. AD-03 and AD-04 favour simplicity and low operational overhead. The architecture decision is essentially about how to satisfy the isolation requirements without incurring the operational cost that full service isolation would impose.

| Driver    | Source                                        | Design Obligation                                                 | Consequence if Ignored |
| AD-01     | FR-22, BR-07, QS-07, AC-4                     | Append-only audit store, no update/delete, retained ≥ 3 years     | Loss of legal defensibility; non-compliance |
| AD-02     | FR-25, FR-27, FR-28, BR-04, QS-03, AC-2, AC-3 | Encrypted evidence store, separate from public data, RBAC-enforced | Data breach; fraudulent claims |
| AD-03     | FR-01, FR-02, FR-29, QS-02, QS-04, AC-1, AC-5 | OIDC integration, mobile-responsive, ≥ 99% uptime                  | Inaccessible system; poor adoption |
| AD-04     | FR-06, QS-05                                  | Categories as configuration data, ≤ 30 min to add                  | Disproportionate deployment overhead |

3. Alternatives Considered and Rejected

We considered several options and eliminated them before the full comparison. Recording them here prevents the comparison from looking like a choice between two arbitrary designs.

A  pure three-tier MVC application with a standard relational database  was rejected because it cannot satisfy AD-01 without significant additional machinery. A mutable log table satisfies the letter of the audit requirement but not its spirit, because the threat model includes a compromised or malicious administrator. It also tends to blur the evidence boundary that AD-02 requires, since public item listings and private evidence would naturally live in the same database and be exposed through the same query layer.

A  serverless function-per-endpoint design  was rejected because the audit and custody requirements demand transactional consistency that is awkward to achieve across stateless functions. If the function that updates an item's status and the function that appends to the audit log are separate, a failure between them leaves the system inconsistent. Coordinating them requires a saga or outbox pattern, which is additional complexity the team does not need.

A  full event-sourced system with CQRS  was rejected as over-engineering for a semester project. We need event sourcing for the custody log, not for the entire domain. Applying it to every entity would multiply the code and the operational complexity without a corresponding benefit, because most entities do not have the same audit requirements as the custody log.

That left two alternatives that are genuinely feasible and that differ in ways that matter.


4. Alternative A: Modular Monolith with an Event-Sourced Audit Log

4.1 Description

The entire system is a single deployable web application. Internally, it is divided into modules with explicit responsibilities and dependency directions: Auth, Report, Claim, Admin, Matching, Notification, Chain of Custody, and Evidence. Each module owns its data and exposes an internal API to other modules. The Chain of Custody module is backed by an append-only event store, meaning a store where records can be inserted but never updated or deleted. The Evidence module stores encrypted blobs in a separate store from the primary database, and access to that store is mediated by RBAC checks that happen in the module layer, not in the database query layer. The front end is server-rendered with a responsive CSS framework, with a REST API exposed for future mobile clients. Authentication is delegated to the university OIDC provider.

4.2 Key Architectural Property

The audit log is not a table the application writes to as a side effect. It is a module whose only operation is append. There is no update method, no delete method, and no admin override. If a status change needs to be recorded, it goes through the Chain of Custody module, which appends an event. The Report module does not have direct write access to the event store. This is a structural guarantee, not a policy one.

4.3 How It Responds to the Drivers

Against  AD-01 , the event store satisfies the immutability requirement structurally. The only code path to it is through a module that performs validation and appends an event. No other module has a reference to the event store, which means no other module can modify it even accidentally.

Against  AD-02 , the Evidence module is the only code that reads from the encrypted blob store, and it performs its own RBAC check. The Report module does not have a reference to the blob store, so a developer writing a new query in the Report module cannot accidentally expose evidence.

Against  AD-03 , a single deployable is easier to keep running than seven services, which supports the availability target. Server-rendered responsive forms are straightforward to make accessible and mobile-friendly, which supports the usability target. The Auth module is the only module that communicates with the OIDC provider, so a change in the provider's configuration affects one module.

Against  AD-04 , categories are stored in the primary database and read by the Report module at runtime. Adding a category is a data change, not a code change.

4.4 Pros

The strongest argument for this alternative is that it satisfies the auditability requirement structurally rather than procedurally. The event store has no update or delete operations, and the only code path to it is through a module that appends. This is not a policy an administrator could override; it is a property of the code. The same is true for evidence privacy: the Evidence module is the only code that reads from the encrypted blob store, and it performs its own RBAC check.

The second argument is feasibility. A single deployable is easier to build, test, deploy, and monitor than seven services. The team can use a single repository, a single CI pipeline, and a single database cluster. More of the semester is spent on the actual lost-and-found workflows and less on infrastructure. The quality scenarios that depend on operational reliability, such as uptime and graceful error handling, are more likely to be met because there are fewer things to configure correctly.

The third argument is consistency. All operations happen in a single transaction, so there is no risk of the item state and the audit log diverging. This is particularly important for AD-01, where an unlogged status change would be a failure.

4.5 Cons

The main disadvantage is scalability. A modular monolith scales vertically, not horizontally. If the system were deployed across multiple campuses with tens of thousands of users, the single deployable would become a bottleneck. The Phase 1 scope explicitly excludes multi-campus support, and the expected load is a single university's lost-and-found traffic, which is well within the capacity of a single well-provisioned server. But the limitation is real.

The second disadvantage is deployment coupling. A change to any module requires redeploying the whole application. For a semester project with infrequent releases, this is acceptable. If release frequency increased, it would become painful.

The third disadvantage is that module boundaries can erode over time if the team is not disciplined. In a monolith, it is easy to reach across modules and call a function directly, bypassing the intended interface. We mitigate this by enforcing package structure in code review and by keeping the dependency graph explicit in the architecture documentation.

The fourth disadvantage is that the event store will grow indefinitely. Every status change appends a row, and rows are never deleted. Over three years, this could accumulate to a significant size. We will archive events older than three years to cold storage, but the active event store will still contain three years of history.


5. Alternative B: Microservices with Separate Audit and Evidence Services

5.1 Description

The system is decomposed into independent services: Auth, Item, Claim, Audit, Evidence, Notification, and an API Gateway. Each service has its own datastore and communicates over REST or a message queue. The Audit service uses write-once storage. The Evidence service uses a key management service for encryption. The front end is a single-page application that calls the API gateway. Authentication is handled by an SSO service.

5.2 Key Architectural Property

The audit and evidence concerns are isolated at the process and network level, not just the module level. A bug in the Item service cannot corrupt the Audit service. The Evidence service can be scaled or secured independently. Each service can be deployed and scaled on its own.

5.3 How It Responds to the Drivers

Against  AD-01 , the dedicated Audit service with write-once storage provides strong immutability, and the process boundary means a crash in the Item service cannot corrupt the audit trail. The weakness is that the Item service and the Audit service are separate processes, so if the Item service updates a status and the call to the Audit service fails, the system is inconsistent unless a saga or outbox pattern is implemented.

Against  AD-02 , the Evidence service is behind its own network boundary, so a misconfigured query in another service cannot reach the evidence store. The weakness is that the guarantee depends on the service mesh or API gateway being configured correctly, which is an operational risk for a small team.

Against  AD-03 , the SPA can be responsive and the SSO service handles authentication. The weakness is that the SPA requires the team to manage client-side routing, state, and API integration separately, which increases the risk of usability bugs that only appear in production.

Against  AD-04 , categories can be stored in the Item service's database, but the change must propagate to any service that caches categories, which adds coordination overhead.

5.4 Pros

The strongest argument for this alternative is isolation at the process level. A bug or crash in the Item service cannot take down the Audit service. The Evidence service can be deployed behind its own network boundary with its own access controls. For an organization with a dedicated platform team and a mature DevOps practice, this is a real advantage.

The second argument is independent scalability. If one service becomes a bottleneck, it can be scaled without scaling the others. For a system that expects uneven load, such as high read traffic on item listings and low write traffic on evidence, this can be efficient.

The third argument is independent deployment. A change to the Notification service does not require redeploying the Item service. For a large team with frequent releases, this reduces coordination overhead.

5.5 Cons

The disadvantage is that the isolation is only as strong as the operational discipline behind it. In practice, a small team will not have the time or expertise to configure a service mesh correctly, to implement distributed tracing, to handle partial failures between services, or to manage seven deployment pipelines. The result is likely to be a system that is theoretically more secure and more available but practically less so, because the operational complexity introduces new failure modes that the team cannot debug.

The audit requirement is particularly problematic. If the Item service updates an item's status and the call to the Audit service fails, the system is inconsistent unless a saga or outbox pattern is implemented. That is additional code that must be written and tested, and it is code that Alternative A does not need.

The second disadvantage is cost. Seven services means seven sets of infrastructure, seven sets of logs, and seven sets of metrics. For a semester project with no budget, this is not realistic.

The third disadvantage is that distributed failures are harder to debug. When a request spans multiple services and one of them fails, tracing the failure requires distributed tracing infrastructure that the team would have to set up and learn.


6. Comparison Using the Same Project-Specific Criteria

The criteria are chosen because they correspond to the architecture drivers and the quality scenarios that discriminate between designs. Each criterion is evaluated for both alternatives on the same scale.

 Auditability and immutability.  Alternative A scores strongly because the event store is a structural property of the code, not a policy that can be overridden. Alternative B also scores strongly because the Audit service has write-once storage, but the distributed transaction problem weakens the guarantee unless additional patterns are implemented.

Evidence security. Alternative A scores strongly because the Evidence module is the only code that reads from the blob store, and the Report module has no reference to it. Alternative B scores strongly because the Evidence service is behind its own network boundary, but the guarantee depends on correct service mesh configuration.

Integration and mobile. Alternative A scores well because server-rendered responsive forms are straightforward to build and test. Alternative B scores well because the SPA can be responsive and the SSO service handles authentication, but the SPA increases the risk of usability bugs.

Semester feasibility. Alternative A scores excellently because a single deployable means one repository, one CI pipeline, and one database cluster. Alternative B scores poorly because seven services require seven deployment pipelines and a level of operational discipline the team does not have.

Maintainability. Alternative A scores well because categories are database configuration, and the module structure allows extraction later if needed. Alternative B scores well in theory because services can be deployed independently, but the operational complexity makes actual maintenance harder.

Operational cost. Alternative A scores well because there is one process to monitor. Alternative B scores poorly because seven services means seven sets of infrastructure, logs, and metrics.

Failure boundaries. Alternative A scores moderately: a module failure can affect the whole application, but the Chain of Custody failure is designed to block status changes rather than allow them without logging. Alternative B scores strongly in theory because services are isolated, but distributed failures are harder to debug.

Data consistency. Alternative A scores strongly because all operations happen in a single transaction. Alternative B scores weaker because operations span services and require sagas or outbox patterns to avoid inconsistency.


7. Why Alternative A Was Selected

Alternative A was selected because it satisfies the quality requirements with fewer moving parts, and the guarantees it provides are structural rather than operational.

The audit requirement is satisfied because the event store has no update or delete operations, and the only code path to it is through a module that appends. This is not a policy an administrator could override; it is a property of the code. The evidence privacy requirement is satisfied because the Evidence module is the only code that reads from the encrypted blob store, and it performs its own RBAC check. A developer writing a new query in the Report module cannot accidentally expose evidence because the Report module does not have a reference to the blob store. The availability requirement is more likely to be met because a single deployable is easier to keep running than seven services. The maintainability requirement is satisfied because categories are configuration data, not code.

Alternative B provides stronger isolation in theory, but that isolation is only as strong as the operational discipline behind it, which is precisely what a small student team lacks. The audit requirement is particularly problematic for Alternative B because if the Item service updates a status and the call to the Audit service fails, the system is inconsistent unless a saga or outbox pattern is implemented. That is additional complexity that Alternative A does not require because both operations happen in the same transaction.

The Phase 2 recommendation is Alternative A, with the note that the Evidence and Chain of Custody modules are designed with clear boundaries and minimal dependencies so that they can be extracted into separate services later if the system needs to scale or if the university mandates service isolation.


8. Consequences Accepted

Choosing Alternative A means accepting several consequences, including negative ones.

The system will not scale horizontally without modification. If the university later decides to roll the system out to multiple campuses, or if the load grows beyond what a single server can handle, we will need to extract the Evidence and Chain of Custody modules into separate services. The architecture is designed to make this extraction possible, but it is still work that would need to be done.

The event store will grow indefinitely. Every status change appends a row, and rows are never deleted. Over three years, this could accumulate to a significant size. We will mitigate this by archiving events older than three years to cold storage, but the active event store will still contain three years of history.

The team must be disciplined about module boundaries. In a monolith, it is easy to reach across modules and call a function directly, bypassing the intended interface. If we allow the Report module to write to the Evidence store directly, we lose the structural guarantee that evidence is only accessible through the Evidence module. We will enforce this through code review and by keeping the architecture documentation up to date.

The front end is server-rendered rather than a single-page application. This is a deliberate choice because server-rendered forms are easier to make accessible and mobile-responsive, and they degrade gracefully on older browsers. The cost is that the user experience is less fluid than a modern SPA. For a lost-and-found system used occasionally by students and security guards, this is an acceptable trade-off.


9. Risks and Reconsideration Triggers

The highest architectural risk is that the event store grows faster than expected and that query performance degrades. We will monitor event store size and custody history query time, and we will add indexing or archiving sooner if needed.

The second risk is that the university's OIDC provider changes its configuration or becomes unavailable. We will implement a fallback local authentication mode for development and testing, and we will document the integration points so that a change in the provider's configuration can be accommodated without rewriting the Auth module.

The third risk is that the team does not maintain module boundaries under time pressure. If we find that modules are becoming coupled, we will revisit the architecture and consider extracting the most entangled module into a separate service.

The evidence that would cause us to revise this decision is: if the system needs to support multiple campuses, if the audit log exceeds ten million events per year with degraded query performance, if the team grows to eight or more developers and deployment frequency becomes a bottleneck, or if the university mandates a microservices-based deployment for security isolation. None of these are true today, but they are the conditions under which Alternative B would become the better choice.


10. Traceability to Quality Scenarios

This section links each quality scenario to the architectural element that satisfies it, so that the comparison can be checked against the requirements rather than taken on trust.

QS-07 (Data Integrity) requires that one hundred percent of status changes are logged with a complete audit trail. Alternative A satisfies this through the Chain of Custody module and its append-only event store. Alternative B satisfies this through the Audit service, but only if the saga or outbox pattern is implemented to handle failures between the Item service and the Audit service.

QS-03 (Security) requires that one hundred percent of non-administrative attempts to view private evidence are blocked. Alternative A satisfies this through the Evidence module and its RBAC-enforced access to the encrypted blob store. Alternative B satisfies this through the Evidence service and its network boundary, but only if the service mesh is configured correctly.

QS-02 (Availability) requires at least ninety-nine percent uptime during core university hours. Alternative A satisfies this more easily because a single deployable is simpler to keep running. Alternative B requires each service to be independently available, which is harder to achieve.

QS-05 (Maintainability) requires that a new category can be added in production within thirty minutes. Alternative A satisfies this by storing categories as configuration data in the primary database. Alternative B also satisfies this, but the change must propagate to any service that caches categories.

QS-04 (Usability) requires that at least ninety percent of security guards complete the Report Found Item workflow on their first try after fifteen minutes of training. Alternative A satisfies this through server-rendered responsive forms. Alternative B also satisfies this in principle, but the SPA increases the risk of usability bugs.

QS-01 (Performance) requires that search results are displayed within three seconds. Both alternatives can satisfy this with indexed queries and pagination. The difference is operational: Alternative A's single database is simpler to index and tune than Alternative B's distributed data stores.

QS-06 (Reliability) requires that database connection failures during report submission are handled gracefully in ninety-nine point nine percent of cases. Alternative A satisfies this with a single connection pool and retry logic. Alternative B requires each service to handle its own database failures, which multiplies the failure paths.


11. Summary

Alternative A, a modular monolith with an event-sourced audit log, was selected because it satisfies the architecture drivers with structural guarantees rather than procedural policies, and because it is feasible for a small student team to build and operate within a single semester. Alternative B, microservices with separate audit and evidence services, provides stronger isolation in theory but imposes an operational burden that the team cannot realistically carry, and it weakens the audit guarantee unless additional distributed transaction patterns are implemented.

The decision is recorded in decisions/ADR-001-architecture.md. The component structure is recorded in models/component-architecture.puml and models/component-architecture.svg. The quality-to-architecture traceability is recorded in docs/quality-to-architecture.md. The Lab 07 evidence index is recorded in evidence/lab-07/README.md`.


12. Exit Record

The quality requirement that most influenced this decision was AD-01: Auditability and Non-Repudiation, derived from QS-07 and BR-07. It forced the event-sourced design of the Chain of Custody module and the module boundary that prevents any other module from writing directly to the event store. Without this requirement, a simpler CRUD-based architecture would have been sufficient.

The evidence that would cause the team to revise the decision is: a requirement to support multiple campuses, audit log volume exceeding ten million events per year with degraded query performance, team growth to eight or more developers with deployment frequency becoming a bottleneck, or a university mandate for microservices-based security isolation.