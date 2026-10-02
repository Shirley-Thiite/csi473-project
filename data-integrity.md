

Project: Campus Lost and Found Management System
Team: CSI473 Team 12
Date: 2 October 2026
Branch: lab-08

1. Purpose

This document records the data integrity constraints that the system enforces across the logical data model, the API contract, the deployment model, and the failure and recovery design. It links each constraint to the business rule, functional requirement, quality scenario, or use case it protects, and it states where the constraint is enforced and how it is verified.

The document is written to be consistent with the Phase 1 baseline (CSI473_Phase1_Team12.pdf), with the Lab 07 architecture decision (decisions/ADR-001-architecture.md), and with the Lab 08 artefacts (models/logical-data-model.md, docs/api-contracts/core-operation.md, models/deployment.md, models/failure-recovery.md). Where Lab 08 refines the Phase 1 model, the refinement is explained rather than hidden.

2. Constraints
2.1 Status Transition Order (BR-02)

Constraint. An Item's status may only transition in the sequence LOST → FOUND → RESOLVED.

Enforcement. The Report module implements a state machine. Before any status change, the module checks that the requested transition is allowed from the current status. The Chain of Custody table records the previous status and the new status on every entry, so the transition order is auditable.

Verification. Attempt to transition an Item from LOST to RESOLVED directly. The API returns 422 with error code INVALID_STATUS_TRANSITION. Inspect the Chain of Custody table and confirm that no entry was appended.

Reconciliation with Phase 1. Phase 1 BR-02 stated the order as LOST → FOUND → CLAIMED → VERIFIED → HANDOVER_PENDING → RESOLVED. Lab 08 simplifies the Item lifecycle to three states and represents the claim lifecycle states as attributes of the Claim entity. This keeps the Item aggregate root simple and pushes the claim lifecycle into the Claim entity where it belongs. The Chain of Custody log records both item-level and claim-level events in a single append-only log, distinguished by a category field, so no audit trail is lost by the simplification.

Related requirements and rules. FR-23, BR-02, QS-07.


2.2 Claim Restriction (BR-03, revised)

Constraint. A user may not claim an item they reported as found. A user may claim an item they reported as lost.

Enforcement. The Claim module joins Claim to Item on itemId and compares the claimant to the reporter. If the item's reportType is FOUND and the reporter is the claimant, the claim is rejected. If the item's reportType is LOST and the reporter is the claimant, the claim is allowed. The reportType field on Item is the discriminator that makes this check possible; it was added in Lab 08 specifically to resolve the Phase 1 ambiguity.

Verification. Authenticate as a user who reported a FOUND item and attempt to claim that item. The API returns 403 with error code CANNOT_CLAIM_OWN_FOUND_REPORT. Authenticate as a user who reported a LOST item and attempt to claim the matching FOUND item. The claim is accepted.

Reconciliation with Phase 1. The Phase 1 feedback flagged BR-03 as ambiguous because it could block genuine lost-item owners from claiming their own items. Lab 08 resolves the ambiguity at the data level by distinguishing found reports from lost reports via reportType.

Related requirements and rules. FR-14, FR-16, BR-03.


2.3 Evidence Privacy (BR-04, QS-03)

Constraint. Private evidence is never visible to non-administrative users.

Enforcement. Evidence blobs are stored in the encrypted blob store, separate from the primary database. The Evidence module is the only code with access to the blob store, and it performs an RBAC check before every read. Public item listing queries do not join to the Evidence table. The API contract for public search does not include evidence fields in the response. The deployment model places the encrypted blob store on a separate node from the primary database, so a SQL injection vulnerability in the primary database cannot reach the evidence blobs.

Verification. Authenticate as a Student. Query the public search endpoint for an item that has evidence attached. Verify that the response does not contain any evidence fields. Attempt to call the Admin evidence endpoint directly. The API returns 403.

Related requirements and rules. FR-25, FR-27, FR-28, BR-04, QS-03.


2.4 Handover Within 7 Days (BR-05)

Constraint. A HandoverRecord must be created within 7 days of claim approval.

Enforcement. The Admin module records the approval timestamp on the Claim. A scheduled job runs daily and checks for approved claims without a HandoverRecord. If a claim has been approved for more than 7 days and no handover record exists, a reminder notification is sent to the Administrator. The deployment model places the scheduler inside the application server; if the application server is down, the scheduled job resumes on restart and catches up on missed reminders.

Verification. Approve a claim in a test environment. Do not create a handover record. Advance the system clock by 8 days. Verify that the reminder notification is created.

Related requirements and rules. FR-24, BR-05.


2.5 Chain of Custody Completeness (BR-07, QS-07)

Constraint. Every status change of an Item is recorded in the Chain of Custody with a timestamp and actor identity.

Enforcement. The Report module, the Claim module, and the Admin module all perform status changes through the Chain of Custody module. The Chain of Custody module is the only code path that writes to the event store. The application's database role has INSERT privilege on the event store table but no UPDATE or DELETE privilege. The Item write and the custody append are performed in the same transaction; if either fails, both roll back.

Verification. Perform every status transition in the item lifecycle. Query the Chain of Custody table and verify that every transition has a corresponding entry with the correct timestamp and actor. Attempt to update or delete a custody entry using the application's database credentials; the database rejects the operation.

Related requirements and rules. FR-22, BR-07, QS-07.


 2.6 One Active Claim Per Item Per User (BR-08, FR-18)

Constraint. A user may have at most one active (PENDING) claim on a given item.

Enforcement. A partial unique index on (itemId, claimantId) WHERE status = 'PENDING' is created on the Claim table. The Claim module also checks for an existing pending claim before inserting a new one, to return a clean error rather than a database constraint violation.

Verification. Submit a claim as a user. Before the claim is decided, submit another claim on the same item as the same user. The API returns 409 with error code ACTIVE_CLAIM_EXISTS.

Related requirements and rules. FR-18, BR-08.


2.7 Unique Reporting Within 24 Hours (BR-01)

Constraint. A user may not report the same item as found more than once within 24 hours.

Enforcement. The Report module checks for an existing Item with the same category, description, and location, created by the same user within 24 hours. If a match is found, the API returns 200 with duplicate: true and the existing item rather than creating a new one.

Verification. Submit a found report. Immediately submit the same report again. The second submission returns 200 with the existing item and the duplicate flag set.

Related requirements and rules. FR-08, BR-01.


2.8 Category as Configuration (QS-05, FR-06)

Constraint. Item categories are configuration data, not code, and can be added in production within 30 minutes.

Enforcement. Categories are rows in the Category table. The Report module reads the category list at runtime. The Admin module exposes a category management interface. The deployment model places the Category table in the primary database, which the application server reads over the private network.

Verification. Add a new category through the Admin interface. Verify that it appears in the Report Found Item form within the same session. Measure the time from start to visibility; it should be under 30 minutes.

Related requirements and rules. FR-06, QS-05.


2.9 Evidence Encrypted at Rest (FR-25, FR-27)

Constraint. Private evidence is encrypted at rest.

Enforcement. Evidence blobs are stored in the encrypted blob store. The blob store uses a key management service. The Evidence table stores an encryptionKeyId that references the key used for the blob. The deployment model places the KMS in the data zone, reachable only by the application server.

Verification. Inspect the blob store directly. Verify that the stored bytes are not readable as plaintext. Verify that the encryptionKeyId in the Evidence table references an active key in the KMS.

Related requirements and rules. FR-25, FR-27, BR-04.


2.10 Transactional Integrity (BR-07, QS-07)

Constraint. No status change is ever committed without a corresponding Chain of Custody entry, and no Chain of Custody entry is ever committed without a corresponding status change.

Enforcement. The Item write and the custody append are performed in a single database transaction. The Report module opens the transaction, inserts the Item, calls the Chain of Custody module to append the entry, and commits only if both succeed. If either fails, the transaction rolls back.

Verification. Simulate event store unavailability during a Report Found Item submission. Verify that the response is 503 with error code EVENT_STORE_UNAVAILABLE. Verify that no Item row was committed to the primary database. Verify that after the event store recovers, a retry with the same payload creates exactly one Item and one custody entry.

Related requirements and rules. FR-22, BR-07, QS-07.


3. Traceability

| Constraint                         | Related Requirement | Related Business Rule | Related Quality Scenario | Related Use Case |
| Status transition order            | FR-23               | BR-02                 | QS-07                    | UC-06            |
| Claim restriction                  | FR-14, FR-16        | BR-03                 | —                        | UC-06            |
| Evidence privacy                   | FR-25, FR-27, FR-28 | BR-04                 | QS-03                    | UC-06            |
| Handover within 7 days             | FR-24               | BR-05                 | —                        | UC-06            |
| Chain of custody completeness      | FR-22               | BR-07                 | QS-07                    | UC-04, UC-06     |
| One active claim per item per user | FR-18               | BR-08                 | —                        | UC-06            |
| Unique reporting within 24 hours   | FR-08               | BR-01                 | —                        | UC-04            |
| Category as configuration          | FR-06               | —                     | QS-05                    | UC-04            |
| Evidence encrypted at rest         | FR-25, FR-27        | BR-04                 | QS-03                    | UC-06            |
| Transactional integrity            | FR-22               | BR-07                 | QS-07                    | UC-04            |


4. Integrity Risks

The highest integrity risk in this design is the same as the highest architectural risk recorded in ADR-001: the event store grows indefinitely and query performance degrades, potentially encouraging a future developer to bypass the append-only guarantee for performance reasons. The design addresses this with archival to cold storage after three years, and with the database privilege restriction that prevents UPDATE and DELETE even if a developer tries.

The second integrity risk is that the duplicate detection within 24 hours is based on category, description, and location, which are free-text fields. Two reports that describe the same item in different words would not be detected as duplicates. This is acceptable because the purpose of BR-01 is to prevent accidental double submission, not to detect all possible duplicates. The Matching module handles similarity detection for administrative review, so near-duplicates still surface for a human to decide.

The third integrity risk is that the OIDC provider is a hard dependency for authentication. If it is unavailable, no one can log in, and no protected operation can be performed. This is an availability risk, not a data integrity risk, but it is recorded here because it affects the system's ability to serve users. The mitigation is that the OIDC provider is operated by the university and is expected to have its own high availability, and existing sessions continue until expiry.

The fourth integrity risk is that the status transition simplification introduced in Lab 08 (three-state Item lifecycle instead of the Phase 1 six-state model) could lose information about intermediate claim states if the Chain of Custody log does not record claim-level events. The design addresses this by recording claim-level events (CLAIMED, VERIFIED, HANDOVER_PENDING) in the same Chain of Custody log, distinguished by a category field. This preserves the audit trail even though the Item status itself only carries three values.


5. Exit Record

Integrity risk: The Chain of Custody event store is unavailable when a status change is committed, allowing the status change to commit without a corresponding custody entry. This would violate BR-07 and QS-07 and would break the audit trail that the entire system is built around.

Design mechanism: The status change and the custody append are performed in a single database transaction. If either fails, the transaction rolls back. The database role used by the application has INSERT privilege but no UPDATE or DELETE privilege on the event store, so the log cannot be tampered with even if the application is compromised.

Test needed to verify the mechanism: Simulate event store unavailability during Report Found Item submission. Verify that the response is 503 with error code EVENT_STORE_UNAVAILABLE. Verify that no Item row was committed to the primary database. Verify that after the event store recovers, a retry with the same payload succeeds and creates exactly one Item and one custody entry. Repeat the test with a duplicate submission to verify that the duplicate detection prevents a second Item from being created.


6. Related Artefacts

The Lab 07 architecture options are in docs/architecture-options.md. The decision record is in decisions/ADR-001-architecture.md. The quality-to-architecture traceability is in docs/quality-to-architecture.md. The Lab 08 logical data model is in models/logical-data-model.md. The Lab 08 API contract is in docs/api-contracts/core-operation.md. The Lab 08 deployment model is in models/deployment.md. The Lab 08 failure and recovery model is in models/failure-recovery.md. The Lab 08 evidence index is in evidence/lab-08/README.md.