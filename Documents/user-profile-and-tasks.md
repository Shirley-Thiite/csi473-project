User Profiles and Task Scenarios

Project: Campus Lost and Found Management System
Team: CSI473 Team 12
Date: 23 October 2026
Branch: lab-10

1. Purpose

This document defines the primary user profile and the task scenarios that the Lab 10 wireframes, navigation model, and usability test are based on. It identifies one primary task (report a found item, UC-04) and two supporting tasks (claim a found item, UC-06; verify a claim, UC-06). Each task is described with a clear goal state and measurable success criteria so that the usability test in evidence/usability-test.md can check whether the design works.

The document is written to be consistent with the Phase 1 stakeholder analysis, the Lab 08 API contract (docs/api-contracts/core-operation.md), the Lab 08 failure and recovery model (models/failure-recovery.md), and the Lab 09 detailed design (models/detailed-class-design.puml). Where a task requirement comes from a specific functional requirement or acceptance criterion, the link is stated.

2. Primary User Profile: Security Personnel

Name. Thabo M. (a representative persona, not a real person).

Role. Campus Security Personnel. Works at the Student Center security desk, which is one of the two main lost-and-found drop-off points on campus.

Age and experience. 34 years old. Uses a smartphone daily for messaging and social media. Has used the university's online systems for timesheets and shift scheduling, but is not a power user of web applications. Has not used a dedicated lost-and-found system before; the current process is a paper log book at the security desk.

Context of use. Works an eight-hour shift at the security desk. When a student or staff member hands in a found item, Thabo has to log it in the system. During term time, this happens several times a day. Thabo uses a desktop computer at the desk, but sometimes uses a tablet when doing rounds in the building. The desk is in a public area, so there is background noise and occasional interruptions.

Goals. Log the found item quickly and accurately so that the owner can find it. Avoid making mistakes that cause the item to be mis-filed or lost. Know that the report has been saved and that the audit trail is intact, because Thabo has been told that the chain of custody matters if there is a dispute.

Frustrations. Long forms with many fields. Ambiguous field labels that do not say what is expected. Losing what has been typed if there is an error or a network problem. Not knowing whether the report was saved. Being asked to re-enter data that was already entered.

Accessibility considerations. Reads text at a normal size and does not use a screen reader. Uses a standard mouse and keyboard on the desktop, and touch on the tablet. Benefits from large touch targets, clear labels, and a visible focus indicator.

Why this profile is the primary user. The Phase 1 stakeholder analysis identifies Security Personnel as the role responsible for logging found items (FR-05). UC-04 is the entry point of the vertical slice, and the majority of reports are created by this role. If the design works for Thabo, it works for the entry point of the system.

3. Primary Task Scenario: Report a Found Item

Task. Report a found item as found.

Actor. Thabo, Security Personnel.

Related use case. UC-04 Report Item as Found (Phase 1 use case model).

Related API operation. submitFoundReport in docs/api-contracts/core-operation.md.

Precondition. Thabo is logged in with his university credentials. His session has not expired.

Trigger. A student hands in a found smartphone at the security desk. Thabo opens the system on the desk computer.

Goal state. A found report is created with a unique ID. The item status is FOUND. The chain of custody entry is created with Thabo's identity and the timestamp. Thabo sees a confirmation with the report ID and the custody entry ID, so he can tell the student that the report is audited.

Success criteria.

1. Thabo can complete the task in under 2 minutes from opening the form to receiving the confirmation, without assistance. This is the measurable target for the usability test.
2. The confirmation shows the report ID and the chain of custody entry ID, so the audit trail is visible to Thabo and to the student.
3. If a required field is missing, the error appears next to the field where the problem is, with the field name, and the previously entered data is preserved. This is required by AC-04-02 in Phase 1.
4. If the event store is unavailable (the failure scenario from Lab 08), the form shows a clear message that the report was *not* saved and that Thabo should try again, and the entered data is preserved so he does not have to re-enter it. This is the UI translation of the 503 EVENT_STORE_UNAVAILABLE outcome.
5. If the report is a duplicate of one submitted by the same user in the last 24 hours (BR-01), the form shows a warning with a link to the existing report, rather than silently creating a second report.

Alternative states that must be handled. The full list is in docs/ui-states.md. The states that are specific to this task are:

- Loading state while the form submits.
- Empty state if Thabo has no recent reports (shown on the Home page, not the form).
- Validation error state if a required field is missing or invalid.
- Duplicate warning state if the report is a duplicate within 24 hours.
- Unavailable state if the event store is down.
- Unavailable state if the blob store is down and evidence was attached.
- Unauthorized state if Thabo's role does not permit reporting (this should not happen for Security Personnel, but the state is designed in case it does).
- Session expired state if the session expires while Thabo is filling in the form.

Why this scenario is the primary task. It is the entry point of the vertical slice and the task that the primary user performs most often. It is also the task that the API contract is written for, so the UI can be checked directly against the contract.

4. Secondary Task Scenario: Claim a Found Item

Task. Claim a found item.

Actor. Naledi, a Student.

Related use case. UC-06 Verify Claim and Create Handover Record (Phase 1 use case model, extended in Lab 07).

Precondition. Naledi is logged in with her university credentials. A found report exists for her lost item.

Trigger. Naledi has lost her phone. She searches the found items list and finds a match. She opens the item detail page and selects "Submit Claim".

Goal state. Naledi has submitted a claim with proof of ownership (serial number or photo). The claim status is PENDING. She has received a confirmation with the claim ID and an indication that the claim is pending review.

Success criteria.

1. Naledi can find the item in under 3 minutes using search.
2. Naledi can submit a claim in under 3 minutes including the evidence upload.
3. The claim form shows only the fields required for her role; she does not see administrator fields. This is the RBAC requirement from FR-25 and BR-06.
4. If the item is already claimed by another user, she sees a clear message and the option to search again. This is the response to a duplicate claim conflict, which the API enforces through BR-08.
5. If she has already submitted an active claim on the item, the form is replaced with a "You already have an active claim on this item" message and a link to her claim detail page.
6. If the evidence upload fails because the blob store is unavailable, she sees the message on the evidence field, the rest of the form is preserved, and she can submit without evidence or retry.

Alternative states. The full list is in docs/ui-states.md. For this task, the relevant states are: loading, empty search results, validation error on evidence, duplicate claim, unauthorized (if the item's reportType is FOUND and Naledi was the reporter — the revised BR-03 case), and unavailable (blob store).

Why this scenario is secondary. It is the task Naledi performs most often, but each student loses items only occasionally, so the aggregate frequency is lower than for Security Personnel. The task is more complex than the primary task because of the search step and the evidence upload, so it stresses the design in a different way.

5. Administrative Task Scenario: Verify a Claim

Task. Verify a claim and create a handover record.

Actor. Olga, an Administrator.

Related use case. UC-06 Verify Claim and Create Handover Record (Phase 1 use case model, extended in Lab 07).

Precondition. Olga is logged in with the Administrator role. A claim has been submitted and is pending review.

Trigger. Olga opens the Admin Claim Queue from her dashboard and selects a pending claim.

Goal state. The claim is approved or rejected. If approved, the item status is updated to RESOLVED and a handover record is created with the pickup time and location. If rejected, the claimant is notified with the reason, and the item becomes available for other claimants. In both cases, the chain of custody is updated and the decision is recorded.

Success criteria.

1. Olga can see the claim details and the private evidence in one place, without navigating away from the claim.
2. Olga can approve or reject in under 2 minutes per claim.
3. The system requires a reason for rejection, and the reason is shown to Olga before she confirms, so she does not reject without an explanation.
4. If the handover record cannot be created within the same transaction as the approval, the approval rolls back, and Olga sees a clear error. This is the transactional guarantee from Lab 08, translated into a UI state.
5. The evidence is masked by default and requires a deliberate click to reveal, so shoulder-surfing in a shared office is reduced. This is the UI expression of BR-04 (evidence privacy).

Alternative states. The full list is in docs/ui-states.md. For this task, the relevant states are: loading the claim, empty queue, validation error (reason missing on reject), unauthorized (if Olga's role is not Admin), and unavailable (event store down during approval).

Why this scenario is included. It is the task that completes the vertical slice. Without it, the item never reaches RESOLVED, and the claim never reaches a decision. The design must work for the Administrator as well as for the Security Personnel and the Student.

 6. Task Scenario Summary

| Task | Actor | Use Case | API Operation | Time Target | Success Criteria |
| Report a Found Item | Thabo (Security Personnel) | UC-04 | submitFoundReport | ≤ 2 minutes | Report ID and custody entry ID shown; errors inline; input preserved on failure |
| Claim a Found Item | Naledi (Student) | UC-06 | submitClaim | ≤ 6 minutes (3 find + 3 claim) | Find item in ≤ 3 min; submit claim in ≤ 3 min; duplicate claim handled |
| Verify a Claim | Olga (Administrator) | UC-06 | decideClaim, createHandover | ≤ 2 minutes per claim | Evidence visible in one place; rejection reason required; rollback on handover failure |

 7. Traceability

| Element | Related Requirement | Related Business Rule | Related Quality Scenario | Related API Contract |
| Primary user: Security Personnel | FR-05 (Security Personnel can report found items) | — | QS-04 (usability) | submitFoundReport |
| Primary task: Report Found Item | FR-05, FR-06, FR-07, FR-08 | BR-01 (duplicate within 24h), BR-07 (custody) | QS-04, QS-07 | submitFoundReport |
| Validation error handling | AC-04-02, AC-04-03 | — | — | MISSING_REQUIRED_FIELD, INVALID_CATEGORY, INVALID_DATE |
| Duplicate warning | FR-08 | BR-01 | — | 200 with duplicate: true |
| Unavailable (event store) | FR-22 | BR-07 | QS-07 | 503 EVENT_STORE_UNAVAILABLE |
| Unavailable (blob store) | FR-27 | BR-04 | QS-03 | 503 BLOB_STORE_UNAVAILABLE |
| Secondary user: Student | FR-13 (submit claims) | BR-03 (revised) | QS-04 | submitClaim |
| Secondary task: Claim Found Item | FR-13, FR-14, FR-15, FR-18 | BR-03, BR-08 | QS-04 | submitClaim |
| Duplicate claim handling | FR-18 | BR-08 | — | 409 ACTIVE_CLAIM_EXISTS |
| Administrative user: Administrator | FR-16 (manual review), FR-18 (approve/reject) | BR-09 | — | decideClaim |
| Administrative task: Verify Claim | FR-16, FR-17, FR-18, FR-20, FR-24 | BR-05, BR-07 | QS-07 | decideClaim, createHandover |
| Evidence masking on review | FR-28 | BR-04 | QS-03 | — |
| Rollback on handover failure | FR-24 | BR-05, BR-07 | QS-07 | 503 EVENT_STORE_UNAVAILABLE |

 8. Related Artefacts

The Phase 1 baseline is in CSI473_Phase1_Team12.pdf. The Lab 08 API contract is in docs/api-contracts/core-operation.md. The Lab 08 failure and recovery model is in models/failure-recovery.md. The Lab 08 data integrity constraints are in docs/data-integrity.md. The Lab 09 pattern decisions are in docs/pattern-decisions.md. The Lab 09 detailed class design is in models/detailed-class-design.puml. The Lab 10 navigation model is in models/navigation.puml and models/navigation.svg. The Lab 10 wireframes are in prototype/wireframes.md. The Lab 10 UI states are in docs/ui-states.md. The Lab 10 accessibility checklist is in docs/accessibility-checklist.md. The Lab 10 usability test record is in evidence/usability-test.md.
