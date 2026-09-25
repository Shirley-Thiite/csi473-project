Quality-to-Architecture Traceability

Project: Campus Lost and Found Management System
Team: CSI473 Team 12
Date: 25 September 2026
Branch: lab-07

 1. Purpose

This document links each quality scenario from the Phase 1 baseline to the architectural element that satisfies it. The purpose is to show that the architecture in docs/architecture-options.md and decisions/ADR-001-architecture.md actually responds to the quality requirements rather than merely asserting that it does. Each entry states the quality scenario, the architectural element responsible, the design obligation that follows from the scenario, and the verification method that would demonstrate the scenario is satisfied.

This traceability is required by the Lab 07 brief: the artefact must be "linked to a named requirement, use case, scenario or quality concern in the team project," and the traceability from quality scenarios to architecture elements must be updated.


2. Quality Scenario to Architecture Element

QS-01: Performance

Stimulus. An Administrator initiates a search for a specific item in the database.

Context. The system is under normal operating load.

Expected Response. The system displays the search results within 3 seconds.

Measurable Response Level. Average search response time ≤ 3 seconds.

Architectural Element Responsible. Primary DB (indexed queries) and Matching Module (search logic).

Design Obligation. The Primary DB must have indexes on the attributes used for search: category, location, and description keywords. The Matching Module must use paginated queries so that large result sets do not block the response.

Verification Method. Performance test with 10,000 items in the database. Measure average search response time across 100 queries. Pass if the average is ≤ 3 seconds.

Traceability. QS-01 → FR-09 (searchable database) → Matching Module and Primary DB.


QS-02: Availability

Stimulus. A student attempts to report a lost item.

Context. The system is in operation during core university hours (8:00 AM – 10:00 PM).

Expected Response. The system is available to accept the report.

Measurable Response Level. System uptime of ≥ 99.0% during core university hours.

Architectural Element Responsible. Single deployable (Alternative A) with health checks.

Design Obligation. The deployable must expose a health check endpoint. Non-critical modules (Notification, Matching) must degrade gracefully if they fail, so that the core report workflow remains available. The Chain of Custody module must block status changes if the event store is unavailable rather than allow unlogged changes.

Verification Method. Uptime monitoring during core hours. Alert if uptime falls below 99.0% in any calendar month.

Traceability. QS-02 → AD-03 (integration with university identity and mobile access) → single deployable with health checks.


QS-03: Security

Stimulus. A user attempts to access the serial number field of an item listing via the public search page.

Context. The user is authenticated but is not an Administrator.

Expected Response. The system prevents the disclosure of the serial number, displaying a "Private" placeholder or no value.

Measurable Response Level. 100% of attempts by non-administrative users to view private evidence are blocked.

Architectural Element Responsible. Evidence Module and Encrypted Blob Store, with RBAC enforcement.

Design Obligation. Private evidence must be stored in a separate store from public item data. The Evidence Module must be the only code with a reference to the blob store. Every read from the blob store must be preceded by an RBAC check. The public item listing query must not join to the evidence store.

Verification Method. Authenticate as a non-Administrator and attempt to access the evidence endpoint directly. Expect a 403 response. Also verify that the public search results do not contain any evidence fields.

Traceability. QS-03 → BR-04 (evidence privacy) → FR-25 (RBAC) and FR-27 (separate secure storage) → Evidence Module and Encrypted Blob Store.


QS-04: Usability

Stimulus. A new security guard is asked to report a found item after a 15-minute training session.

Context. The system's UI is in its final, production state.

Expected Response. The guard can complete the Report Found Item workflow without any assistance.

Measurable Response Level. ≥ 90% of users can successfully complete the workflow on their first try after brief training.

Architectural Element Responsible. Presentation Layer (server-rendered responsive forms).

Design Obligation. The Report Found Item form must use a responsive CSS framework, clear field labels, inline validation messages, and a visible progress indicator. The form must degrade gracefully on older browsers and small screens.

Verification Method. Usability test with 10 security guards. Give each a 15-minute training session, then ask them to report a found item without assistance. Pass if ≥ 9 of 10 complete the workflow on their first try.

Traceability. QS-04 → FR-29 (mobile responsive interface) → Presentation Layer.


QS-05: Maintainability (Modifiability)

Stimulus. A new "Wallet" item category needs to be added to the list of item categories.

Context. The system is live and in production.

Expected Response. An Administrator or Developer can add the new category to the system's configuration within 30 minutes.

Measurable Response Level. Time to modify the category list in a production environment is ≤ 30 minutes.

Architectural Element Responsible. Admin Module and Primary DB (category configuration).

Design Obligation. Categories must be stored as rows in the Primary DB, not as hardcoded enums or constants in the application code. The Report Module must read the category list at runtime. The Admin Module must expose an interface for adding a category.

Verification Method. Add a new category through the Admin interface. Measure the time from start to when the category appears in the Report Found Item form. Pass if ≤ 30 minutes.

Traceability. QS-05 → FR-06 (item category) → Admin Module and Primary DB.


QS-06: Reliability

Stimulus. The system experiences a database connection failure during report submission.

Context. The system is under normal operating load.

Expected Response. The system gracefully handles the error and notifies the user to try again.

Measurable Response Level. Error recovery is successful in ≥ 99.9% of failure cases.

Architectural Element Responsible. Report Module with a single connection pool and retry logic.

Design Obligation. The Report Module must catch database connection failures and return a user-friendly error message rather than a stack trace. The connection pool must retry transient failures before giving up. The error must be logged for operational review.

Verification Method. Simulate a database connection failure during report submission. Verify that the user receives a "Please try again" message and that the failure is logged. Repeat 1,000 times and verify that ≥ 999 recover gracefully.

Traceability. QS-06 → Report Module → single connection pool with retry logic.


QS-07: Data Integrity

Stimulus. An Administrator updates an item's status.

Context. The system is in normal operation.

Expected Response. The Chain of Custody record is updated with the change, timestamp, and actor identity.

Measurable Response Level. 100% of status changes are logged with complete audit trail.

Architectural Element Responsible. Chain of Custody Module and Event Store (append-only).

Design Obligation. Every status change must be routed through the Chain of Custody Module, which appends an event with the timestamp and actor identity. The Event Store must have no update or delete operations. No other module may write directly to the Event Store.

Verification Method. Update an item's status through the normal application flow. Verify that the Event Store contains a new entry with the correct timestamp and actor identity. Repeat for every status transition in the item lifecycle (LOST → FOUND → RESOLVED). Pass if 100% of transitions are logged.

Traceability. QS-07 → BR-07 (chain of custody completeness) → FR-22 (immutable chain of custody) → Chain of Custody Module and Event Store.

3. Summary Table

| Quality Scenario          | Architectural Element                  | Design Obligation |
Verification Method |

| QS-01 (Performance)       | Primary DB + Matching Module           | Indexed queries, pagination | 
Performance test with 10,000 items; average ≤ 3s |

| QS-02 (Availability)      | Single deployable + health checks      | Health endpoint, graceful degradation, CoC blocks status changes if event store unavailable | Uptime monitoring during core hours; ≥ 99.0% |

| QS-03 (Security)          | Evidence Module + Encrypted Blob Store | Separate storage, RBAC before every read, no join to public listings | Non-admin access attempt returns 403; public search has no evidence fields |

| QS-04 (Usability)         | Presentation Layer                      | Responsive CSS, clear labels, inline validation, progress indicator | Usability test with 10 guards; ≥ 90% first-try completion |

| QS-05 (Maintainability)   | Admin Module + Primary DB               | Categories as DB rows, read at runtime, admin interface for adding | Add "Wallet" category; verify appears in report form within 30 min |

| QS-06 (Reliability)       | Report Module + connection pool         | Catch failures, retry transient, log errors, friendly message | Simulate 1,000 DB failures; ≥ 999 recover gracefully |

| QS-07 (Data Integrity)    | Chain of Custody Module + Event Store | Append-only store, all status changes routed through CoC, no direct writes | Update status; verify event store entry with timestamp and actor |

4. Traceability to Requirements and Business Rules

The quality scenarios do not exist in isolation. Each one is linked to a functional requirement, a business rule, or an approval condition. This section shows those links so that the traceability can be followed from the quality scenario all the way back to the source requirement.

| Quality Scenario | Linked Functional Requirements | Linked Business Rules | Linked Approval Conditions |
| QS-01            | FR-09                          | —                     | —                          |
| QS-02            | FR-01, FR-02, FR-29            | BR-06                 | Condition 1, Condition 5   |
| QS-03            | FR-25, FR-27, FR-28            | BR-04                 | Condition 2, Condition 3   |
| QS-04            | FR-29                          | —                     | Condition 5                |
| QS-05            | FR-06                          | —                     | —                          |       
| QS-06            | —                              | —                     | —                          |
| QS-07            | FR-22                          | BR-07                 | Condition 4                |

5. Notes on Traceability Gaps

QS-06 (Reliability) is not linked to a specific functional requirement or business rule in the Phase 1 baseline. It emerged from the team's review of the failure modes of the system and is recorded as an additional quality scenario. This is noted here so that the traceability is honest about the gap.

QS-01 (Performance) is linked to FR-09 but not to a business rule. The performance target is derived from the usability expectation that administrators can search the database efficiently, not from a formal business rule. This is also noted here.

QS-02 (Availability) is linked to BR-06 (Authentication Required) because the system must be available for users to authenticate, but the availability requirement is broader than authentication alone. The link is included for completeness but is not the primary source of the scenario.

6. Related Artefacts

The full comparison of architecture alternatives is in docs/architecture-options.md. The decision record is in decisions/ADR-001-architecture.md. The component structure is in models/component-architecture.puml and models/component-architecture.svg. The Lab 07 evidence index is in evidence/lab-07/README.md.