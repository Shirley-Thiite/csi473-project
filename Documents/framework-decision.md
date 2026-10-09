Framework and Dependency-Management Decision

Project: Campus Lost and Found Management System
Team: CSI473 Team 12
Date: 9 October 2026
Branch: lab-09


1. Purpose

This document records the framework and dependency-management choices for the Campus Lost and Found Management System. It states which frameworks we adopt, which we reject, and why. The decisions are made against the four architecture drivers from Lab 07 (AD-01 auditability, AD-02 evidence security, AD-03 university integration and mobile access, AD-04 category maintainability), against the quality scenarios from Phase 1 (QS-01 through QS-07), and against the team's semester constraints recorded in Phase 1.

The document is written to be consistent with docs/pattern-decisions.md, decisions/ADR-001-architecture.md, and the Lab 08 deployment and failure models. Where a framework choice is driven by a pattern decision, the link is stated.

2. Adopted Frameworks and Libraries

2.1 Backend Application Framework

Choice. A lightweight, opinionated web framework in the team's primary language. The framework must provide routing, request parsing, middleware, dependency injection, and scheduled jobs. It must not enforce a particular persistence layer or a particular architecture.

Reasoning. The selected architecture (Alternative A, modular monolith with event-sourced audit log) needs explicit control over transaction boundaries. The Report module writes an Item to the Primary DB and appends a custody entry to the Event Store in a single transaction. A heavyweight framework with an integrated ORM that automatically manages transactions makes this difficult, because the ORM's unit of work is bound to a single data source. A lightweight framework lets us express the transaction boundary explicitly in the application service.

Alternative considered. A full-stack framework with an integrated ORM and migration tool. We rejected this because the integrated ORM encourages a single data access mechanism, and the event store requires a different mechanism — a thin client with INSERT-only privileges. Managing two mechanisms inside one ORM configuration is possible but fragile, and it risks the append-only guarantee.

Consequence accepted. We write more of the transaction and repository code ourselves. This is more code than using an ORM's automatic unit of work, but it directly supports AD-01 and QS-07. The transaction boundary is explicit, testable, and preserved across the two stores.

Related pattern decision. The Repository pattern in docs/pattern-decisions.md (Section 3.1). The framework supports it by providing dependency injection and by not imposing a persistence mechanism.

2.2 Database and Migrations

Choice. A relational database for both the Primary DB and the Event Store, with table-level privilege controls so that the application's role for the Event Store has INSERT privilege only. Migrations managed with a lightweight migration tool or plain SQL scripts.

Reasoning. The Primary DB needs partial unique indexes (for BR-08: one active claim per item per user), check constraints (for BR-02: status values), and foreign keys (for referential integrity across users, items, claims, custody entries, and handovers). The Event Store needs the ability to grant INSERT-only privileges to the application's role, so that no code path can update or delete a custody entry even if the application is compromised. A relational database provides all of these. The choice of a specific relational product is recorded in ADR-002.

Alternative considered. A document database for the Event Store, on the grounds that an append-only log is a natural fit for a document store. We rejected this because the project already needs a relational database for the Primary DB, and introducing a second database technology would double the operational complexity without a corresponding benefit. The append-only guarantee can be enforced at the privilege level in the relational database, which is simpler than managing two stores.

Consequence accepted. We rely on relational features that are specific to the chosen relational product. If the university mandates a different relational database, we would need to verify that the same constraints and privileges are available. This is a low risk because the features we need (partial unique indexes, check constraints, column-level privileges) are common across modern relational databases.

Related pattern decision. The Repository pattern in docs/pattern-decisions.md (Section 3.1) and the rejection of the full ORM for the Event Store (Section 4.2 of this document).

2.3 Frontend Framework

Choice. Server-rendered views with a responsive CSS framework, plus a REST API exposed for future mobile clients.

Reasoning. The architecture decision in ADR-001 already selected server-rendered responsive views. A full single-page application framework (React, Vue, Angular, Svelte) is not required for the semester-sized vertical slice. Server-rendered forms are easier to make accessible, they degrade gracefully on older browsers, and they do not require a separate build pipeline or a separate deployment. The REST API that exposes the same operations lets us add a mobile client later without changing the backend.

Alternative considered. A single-page application with a modern JavaScript framework. We rejected this for the reasons recorded in ADR-001: it increases the risk of usability bugs (QS-04), complicates the deployment (Lab 08), and is not necessary for the mobile responsiveness required by FR-29 and Approval Condition 5.

Consequence accepted. The user experience is less fluid than a modern SPA. We accept this because the system is used occasionally by students and security guards, not continuously by power users, and because the primary design goal is accessibility and reliability rather than interactivity.

2.4 Authentication

Choice. An OIDC client library for the chosen backend language, integrating with the university's OIDC provider, wrapped in an IdentityProvider adapter.

Reasoning. The university's identity provider is an OIDC-compliant service. Using a maintained OIDC client library avoids reimplementing token validation, which is a common source of security bugs and would be difficult to test thoroughly in the semester timeframe. The library is wrapped in the IdentityProvider adapter from docs/pattern-decisions.md, so the application logic does not depend on the library directly. If the library needs to be replaced, only the adapter changes.

Alternative considered. A local username/password system. We rejected this because Approval Condition 1 requires integration with university authentication, and because a local credential store would add a password management burden that the team does not want to carry, and would introduce a security surface that we would have to defend without the university's support.

Consequence accepted. The system depends on the university's OIDC provider for authentication. This is a hard dependency recorded in the Lab 08 deployment model, with the mitigation that existing sessions continue until expiry if the provider becomes unavailable. A fallback local authentication mode exists for development and testing only, as recorded in ADR-001.

Related pattern decision. The Adapter pattern in docs/pattern-decisions.md (Section 3.2).

2.5 Email

Choice. A lightweight SMTP client library for the chosen backend language, wrapped in an EmailSender adapter.

Reasoning. Email delivery is a secondary concern (notifications), and it does not require a heavy library. A lightweight SMTP client is sufficient and easy to swap in tests with a fake sender. This supports the reliability scenario QS-06, because a failure of the SMTP server does not affect core workflows; notifications are queued and retried.

Alternative considered. A transactional email service (SendGrid, Mailgun, Amazon SES). We rejected this because it introduces an external dependency, a paid account, and an API key that the team would need to manage. For a semester project, the university's SMTP server is sufficient. If the system is deployed beyond the semester and needs higher deliverability, a transactional email service can be substituted behind the same EmailSender interface.

Consequence accepted. Email delivery depends on the university's SMTP server. If the SMTP server is unavailable, notifications are queued and retried, and core workflows continue. This is consistent with the Lab 08 deployment model, where SMTP is a soft dependency.

Related pattern decision. The Adapter pattern in docs/pattern-decisions.md (Section 3.2).

2.6 Scheduled Jobs

Choice. The scheduler built into the chosen backend framework, used to run the daily job that monitors pending handovers (BR-05) and the retry job that resends failed notifications.

Reasoning. The framework's built-in scheduler is sufficient for a small number of jobs and does not require a separate scheduling service. The jobs are idempotent and safe to re-run, so the framework's at-least-once execution model is acceptable.

Alternative considered. A dedicated scheduler service (Quartz, Airflow, cron on a separate host). We rejected this as over-engineering for two jobs. If the number of scheduled jobs grows, or if the jobs need to be distributed across multiple application nodes, a dedicated scheduler can be introduced.

Consequence accepted. If the application server is down, the scheduled jobs do not run. On restart, the jobs catch up on missed runs. For the handover reminder, this means a reminder might be sent a day late; for the notification retry, this means a failed notification might be retried a few hours late. Both are acceptable.

2.7 Dependency Management

Choice. The standard package manager for the chosen backend language (npm, pip, Maven, or equivalent), with a lock file committed to the repository.

Reasoning. The lock file ensures reproducible builds and makes it possible to audit which versions are in use. Dependencies are kept low in number, and every third-party library is isolated behind an interface from src/interfaces-or-ports/.

Alternative considered. Vendoring dependencies or using a monorepo with shared internal packages. We rejected both as unnecessary for a single-application project of this size.

Consequence accepted. We depend on the package manager's ecosystem for updates and security fixes. The lock file makes the current state inspectable, and the interface isolation makes any single library replaceable.

3. Dependency-Management Approach

We manage dependencies with three rules.

Rule 1: Keep the dependency count low. Every dependency is a maintenance burden and a security surface. We add a dependency only when it solves a problem that we cannot reasonably solve ourselves in the time available. For example, we use an OIDC client library because implementing token validation ourselves would be error-prone; we do not use a dependency injection framework beyond what the backend framework already provides, because our object graph is small and we can wire it by hand.

Rule 2: Isolate third-party libraries behind interfaces. Every third-party library sits behind an interface from src/interfaces-or-ports/. The OIDC client library is behind IdentityProvider. The SMTP client library is behind EmailSender. The database client is behind ItemRepository, CustodyRepository, and UnitOfWork. This means that if a library needs to be replaced, only the adapter changes, and the rest of the application continues to compile and pass its tests.

Rule 3: Pin versions and commit the lock file. Dependencies are pinned to specific versions, and the lock file is committed to the repository. This ensures reproducible builds and makes it possible to audit which versions are in use at any time. Security updates are applied deliberately, with a test run, rather than automatically.

4. Explicit Rejection

The Lab 09 brief asks for at least one explicit rejection. We record two.

4.1 Rejected: Full ORM for the Event Store

What it is. A full object-relational mapper (Hibernate, Entity Framework, Sequelize, TypeORM) applied uniformly to both the Primary DB and the Event Store.

Why we reject it. A full ORM provides update and delete operations by default, which conflicts with the append-only guarantee that BR-07 and QS-07 depend on. Configuring the ORM to prevent updates and deletes on a specific table is possible but fragile. A future developer could accidentally add an update method, and the ORM's cascading delete behavior could remove custody entries as a side effect of deleting a related entity. The risk is not theoretical; it is a common source of integrity bugs in systems that use ORMs for audit data.

What we do instead. The Event Store is accessed through a thin database client, and the database role used by the application has INSERT privilege only. This makes the append-only guarantee a property of the database, not of the ORM configuration. The CustodyRepository interface exposes only append and findByItemId; there is no update method to call.

Consequence accepted. We maintain two data access mechanisms: a lightweight ORM or query builder for the Primary DB, and a thin client for the Event Store. This is more code than using a single mechanism everywhere, but it makes the integrity guarantee structural rather than configured. The separation also makes the failure-recovery design from Lab 08 easier to implement, because the two mechanisms can be tested independently.

Related pattern decision. Section 3.1 of docs/pattern-decisions.md (Repository) and Section 4.2 (Rejected: Framework-Managed ORM for the Event Store).

4.2 Rejected: Event Bus or Message Queue

What it is. An asynchronous event bus or message queue (RabbitMQ, Kafka, AWS SNS/SQS, or an in-process event dispatcher) to coordinate the secondary effects of claim decisions.

Why we reject it. The secondary effects on a claim decision — notification, handover record creation, custody append — are few and known. They must happen in a well-ordered flow that preserves the transactional guarantee that the custody append is written in the same transaction as the item status change. An event bus would introduce asynchronous decoupling that complicates this guarantee. If the item status commits but the custody append is delivered asynchronously and then fails, the system is inconsistent in a way that is not visible to the user. The operational complexity of running a message broker is also disproportionate for a semester project.

What we do instead. The Claim module calls the secondary effects directly in a synchronous flow, in a defined order. The notification is the only effect that is not required to be in the same transaction; it is logged and retried by a scheduled job if the email sender fails. The other effects (handover record creation, custody append) are inside the same transaction as the claim decision.

Consequence accepted. The ClaimService.decide method is longer and must be updated when a new secondary effect is added. This is a small cost compared to the complexity of an event bus and the risk to the transactional guarantee. If the number of secondary effects grows beyond six, or if any effect needs to be retried independently of the others, we will revisit this decision.

Related pattern decision. Section 4.1 of docs/pattern-decisions.md (Rejected: Observer Pattern for Claim Decisions).

5. Traceability

| Decision    | Related Architecture Driver | Related Requirement | Related Business Rule | Related Quality Scenario |
| Lightweight backend framework   | AD-01    | FR-22 | BR-07 | QS-07 |
| Relational DB for both stores   | AD-01, AD-02                | FR-22, FR-25, FR-27  | BR-04, BR-07 | QS-03, QS-07 |
| Server-rendered frontend        | AD-03                       | FR-29 | — | QS-04 |
| OIDC client for auth            | AD-03                       | FR-01, FR-02 | BR-06 | QS-02 |
| Lightweight SMTP client         | AD-03                       | FR-17, FR-19 | — | QS-06 |
| Framework scheduler for jobs    | —                           | FR-24 | BR-05 | — |
| Low dependency count            | AD-04                       |  — | — | QS-05 |
| Isolate libraries behind interfaces | AD-02, AD-03            | FR-25, FR-27 | BR-04 | QS-03 |
| Pin versions and lock file          | —                       | — | — | QS-06 |
| Rejection: full ORM for Event Store  | AD-01                  | FR-22 | BR-07 | QS-07 |
| Rejection: event bus for claim decisions| AD-01               | FR-16, FR-17 | BR-07 | QS-07 |

6. Related Artefacts

The Lab 07 architecture options are in docs/architecture-options.md. The decision record for the architecture is in decisions/ADR-001-architecture.md. The quality-to-architecture traceability is in docs/quality-to-architecture.md. The Lab 08 logical data model is in models/logical-data-model.md. The Lab 08 API contract is in docs/api-contracts/core-operation.md. The Lab 08 deployment model is in models/deployment.md. The Lab 08 failure and recovery model is in models/failure-recovery.md. The Lab 08 data integrity constraints are in docs/data-integrity.md. The Lab 09 pattern decisions are in docs/pattern-decisions.md. The Lab 09 detailed class design is in models/detailed-class-design.puml. The Lab 09 framework decision record is in decisions/ADR-002.md. The Lab 09 interface skeletons are in src/interfaces-or-ports/.

7. Exit Record

Framework decision with the greatest benefit. The decision to use a lightweight backend framework without an integrated ORM, combined with the Repository pattern and the INSERT-only privilege on the Event Store. This decision directly implements the append-only guarantee that AD-01, BR-07, and QS-07 depend on. Without it, the integrity guarantee would be a policy rather than a structural property.

Cost of the decision. The team writes more transaction and repository code than would be necessary with a full-stack framework. The Event Store uses a different data access mechanism from the Primary DB, which means the team must maintain two mechanisms and understand when to use which. This is a real cost in development time.

Test that will confirm the design works. A test that attempts to update or delete a custody entry through the application's database credentials should be rejected by the database. A test that performs a claim decision should verify that the item status change and the custody append are committed in the same transaction, and that a simulated failure of the custody append causes the item status change to roll back. A test that simulates an SMTP failure should verify that the claim decision still succeeds and that the notification is queued for retry.