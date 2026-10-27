UI States — Campus Lost and Found Management System

Project: Campus Lost and Found Management System
Team: CSI473 Team 12
Date: 23 October 2026
Branch: lab-10

1. Purpose

This document lists the alternative UI states that the Campus Lost and Found Management System must handle. It is the reference for the wireframes in prototype/wireframes.md and the usability test in evidence/usability-test.md. Each state is mapped to the API error code from docs/api-contracts/core-operation.md so that the UI and the API stay consistent, and to the failure scenarios from models/failure-recovery.md so that the design is traceable to the Lab 08 integrity guarantee.

The states are listed only where they are genuinely relevant to the two core workflows: Report Found Item (UC-04) and Verify Claim (UC-06). We have deliberately not included offline caching, because the chain of custody must be recorded at the moment of the report and the system is not designed to accept reports without an active connection. This is a design decision, not an omission, and it is recorded in Section 4.

2. State Catalogue

 2.1 Loading

Trigger. The user submits a form or the page loads a resource.

UI behaviour. The submit button shows a spinner and the text "Saving…". All form fields are disabled to prevent double submission. The page does not reload; the spinner appears in place. When the response arrives, the spinner is replaced by the success confirmation or the relevant error state.

Accessibility. The spinner has an aria-label="Saving report, please wait". The disabled fields remain labelled so a screen reader still announces their purpose. The form does not move focus away from the submit button when the spinner appears.

Related API. Any in-flight request.

 2.2 Empty

Trigger. The user has no recent reports, or the search returns no results.

UI behaviour. On the Home page, the recent reports list shows the message "You have no reports yet" and a link to Report Found Item. In search, the results list shows "No items match your search" and a suggestion to try different keywords or a wider location. The empty state is never a blank page; it always offers a next action.

Accessibility. The empty message is plain text, not an image. The message has role="status" so it is announced when it replaces the results list.

Related API. 200 with an empty result set.

 2.3 Validation Error

Trigger. A required field is missing or invalid. The user has submitted the form.

UI behaviour. An error summary appears at the top of the form: "Please fix the errors below and try again." Each field that has an error shows a red border, an error icon, and a text message directly below the field. The message names the field and the problem, for example "Location Found is required". The invalid field receives focus when the user clicks the error summary link. Previously entered data is preserved.

Accessibility. The error text is not conveyed by colour alone: it includes an icon and a text message. The invalid field has aria-invalid="true" and aria-describedby pointing to the error message, so a screen reader announces the error when the field receives focus. The error summary has role="alert".

Related API. 400 MISSING_REQUIRED_FIELD, 400 INVALID_CATEGORY, 400 INVALID_DATE, 422 EVIDENCE_INVALID.

Related acceptance criteria. AC-04-02 (all required fields must be completed), AC-04-03 (category must be from a valid list).

 2.4 Duplicate Warning

Trigger. The user submits a report that matches an existing report by the same user within 24 hours, or submits a second active claim on the same item.

UI behaviour. For duplicate reports: a yellow banner at the top of the form reads "A similar report already exists. [View existing report]". The form is not submitted; the user is given the choice to review the existing report. For duplicate claims: the claim form is replaced by "You already have an active claim on this item. [View your claim]".

Accessibility. The banner has role="status" so it is announced without interrupting the user. The "View existing report" link is the first focusable element in the banner.

Related API. 200 with duplicate: true; 409 ACTIVE_CLAIM_EXISTS.

Related business rules. BR-01 (unique reporting within 24 hours), BR-08 (one active claim per item per user).

 2.5 Error (General)

Trigger. An unexpected server error occurs (500).

UI behaviour. A red banner at the top of the form reads "Something went wrong. Please try again or contact support." The form is preserved. A "Try Again" button resubmits the same payload; a "Contact Support" link opens the university IT help page.

Accessibility. The banner has role="alert". The "Try Again" button receives focus when the banner appears, so a keyboard user can retry without hunting for the button.

Related API. 500 INTERNAL_ERROR.

 2.6 Unavailable: Event Store

Trigger. The Chain of Custody event store is unreachable during submission (the failure scenario from Lab 08).

UI behaviour. A full-page message replaces the form: "The audit service is currently unavailable. Your report was NOT saved. Please try again in a few minutes." Three buttons are offered: "Try Again", "Edit and Retry", and "Contact Support". Below the message, the users entered data is shown in a read-only summary, so they can see it was not lost. The form does not silently discard the input.

Accessibility. The message has role="alert". The read-only summary uses <dl> semantics so the values are announced as label-value pairs. The "Try Again" button is the default focus target.

Related API. 503 EVENT_STORE_UNAVAILABLE.

Related failure model. models/failure-recovery.md Section 5.1. The transaction rolled back, so no partial state was committed. The UI must tell the user this explicitly, because otherwise the user may believe the report was saved.

 2.7 Unavailable: Blob Store

Trigger. The evidence upload fails because the encrypted blob store is unreachable.

UI behaviour. The rest of the form is preserved. The evidence field shows an inline message: "Evidence could not be stored. You can submit without evidence and attach it later, or try again." Two options are offered: "Submit without evidence" and "Retry evidence upload".

Accessibility. The inline message is associated with the evidence field via aria-describedby. The two options are plain buttons, not a modal, so the user can still navigate the rest of the form.

Related API. 503 BLOB_STORE_UNAVAILABLE.

Related failure model. models/failure-recovery.md Section 6. Evidence is optional; the report can be saved without it, but the system must not silently drop evidence that the user attached.

 2.8 Unauthorized

Trigger. The users role does not permit the action they attempted, or the user has navigated to a page that belongs to another role.

UI behaviour. A dedicated Access Denied page replaces the current view: "You do not have permission to review claims. If you believe this is an error, contact the system administrator." A single "Return to Home" button is offered.

Accessibility. The page has exactly one focusable element when it loads, so a screen reader user is not overwhelmed. The page title is "Access Denied", so it is announced when the page loads.

Related API. 403 FORBIDDEN.

Related requirements. FR-25 (RBAC), BR-06 (authentication required for all protected actions).

2.9 Session Expired

Trigger. The users session token has expired while they were working.

UI behaviour. A modal appears: "Your session has expired. Please log in again to continue." A "Log in again" button redirects to the OIDC provider and returns the user to the same page after authentication. The form data is preserved if the expiry is detected on submit; if the expiry is detected on navigation, the user is returned to the home page.

Accessibility. The modal traps focus and the "Log in again" button is the default focus target. The modal has role="dialog" and aria-labelledby pointing to its heading.

Related API. 401 UNAUTHENTICATED.

2.10 Success

Trigger. The report is created, or the claim decision is recorded.

UI behaviour. A confirmation page replaces the form. For Report Found Item, the page shows the report ID, the chain of custody entry ID, the item status ("FOUND"), the timestamp, and a "Report another item" link. The report ID is displayed in a <code> element with a "Copy" button next to it. For a claim decision, the page shows the decision, the handover pickup time and location (if approved), and a "Return to queue" link.

Accessibility. The confirmation page announces the success message as the first focusable element when the page loads. The report ID is selectable text, not an image, so it can be copied by a screen reader user.

Related API. 201 Created, 200 OK (for decisions).

Related acceptance criteria. AC-04-05 (report receives unique identifier), AC-04-06 (chain of custody entry is created).

3. State Coverage by Screen

The following table shows which states apply to which screen.

| Screen                 | Loading | Empty | Validation | Duplicate | Error | Unavailable (event store) | Unavailable (blob store) | Unauthorized | Session expired | Success |
| Report Found Item form | ✓ | — | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Report Lost Item form  | ✓ | — | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Search results         | ✓ | ✓ | — | — | ✓ | — | — | — | ✓ | — |
| Item detail (public)   | ✓ | — | — | — | ✓ | — | — | — | ✓ | — |
| Claim form             | ✓ | — | ✓ | ✓ | ✓ | — | ✓ | ✓ | ✓ | ✓ |
| My Claims list         | ✓ | ✓ | — | — | ✓ | — | — | ✓ | ✓ | — |
| Admin Claim Queue      | ✓ | ✓ | — | — | ✓ | — | — | ✓ | ✓ | — |
| Claim Review           | ✓ | — | ✓ (rejection reason) | — | ✓ | ✓ (on approval) | — | ✓ | ✓ | ✓ |
| Handover Record        | ✓ | — | ✓ | — | ✓ | — | — | ✓ | ✓ | ✓ |

The pattern is that the two main form screens (Report Found Item and Claim form) need the full set of states, because they involve the most writes and the most failure scenarios. The read-only screens (Search, Item Detail, lists) need a smaller set.

4. States Deliberately Not Designed

The Lab 10 brief warns against forcing offline caching, notifications, or university integration into every project. The following states are deliberately not designed, and the reason is stated.

Offline state for the Report Found Item form. The form cannot function offline. The chain of custody must be recorded at the moment of the report, and the transactional guarantee in models/failure-recovery.md requires that the Item write and the custody append happen in the same transaction. Accepting an offline report would either violate the guarantee (by accepting a report that cannot be logged) or require a deferred-custody model that the semester scope does not support. The system assumes the campus network is available for the roles that use the form, as recorded in the Phase 1 assumptions.

Offline state for claim submission. The claim form cannot function offline because the evidence upload requires the blob store and the claim write requires the primary database. The same reasoning as above applies.

Offline state for read-only pages (Search, Item Detail). A read-only offline cache would be technically possible but would add service worker complexity and cache invalidation problems for a benefit that is marginal: the primary users (Security Personnel at a desk, Students on campus) are on the campus network. We do not implement it.

Push notification state. The system sends email and in-app notifications. It does not send push notifications to a mobile app, because a native mobile app is out of scope in Phase 1. The in-app notification state is a bell icon with an unread count, which is a different UI element from the states listed in this document.

"Please wait" state for long-running operations. The system has no operations that take more than a few seconds, because the vertical slice does not include batch processing or report generation. If such an operation is added later, the loading state in Section 2.1 can be extended with a progress bar.

5. Traceability

| State | Related Requirement | Related Business Rule | Related Quality Scenario | Related API Code |
| Loading | — | — | QS-04 | — |
| Empty | — | — | QS-04 | 200 with empty set |
| Validation error | FR-06 (required fields) | — | — | MISSING_REQUIRED_FIELD, INVALID_CATEGORY, INVALID_DATE, EVIDENCE_INVALID |
| Duplicate warning | FR-08 (duplicate prevention) | BR-01, BR-08 | — | 200 with duplicate: true; 409 ACTIVE_CLAIM_EXISTS |
| General error | — | — | QS-06 | INTERNAL_ERROR |
| Unavailable: event store | FR-22 (immutable log) | BR-07 | QS-07 | EVENT_STORE_UNAVAILABLE |
| Unavailable: blob store | FR-27 (separate evidence storage) | BR-04 | QS-03 | BLOB_STORE_UNAVAILABLE |
| Unauthorized | FR-25 (RBAC) | BR-06 | QS-03 | FORBIDDEN |
| Session expired | FR-01, FR-02 (authentication) | BR-06 | — | UNAUTHENTICATED |
| Success | FR-08 (unique ID), FR-22 (custody) | BR-07 | QS-07 | 201, 200 |

6. Related Artefacts

The user profiles and task scenarios are in docs/user-profile-and-tasks.md. The navigation model is in models/navigation.puml and models/navigation.svg. The wireframes are in prototype/wireframes.md, prototype/wireframes.puml, and prototype/wireframes.svg. The accessibility checklist is in docs/accessibility-checklist.md. The usability test record is in evidence/usability-test.md. The API contract is in docs/api-contracts/core-operation.md. The failure and recovery model is in models/failure-recovery.md. The data integrity constraints are in docs/data-integrity.md.