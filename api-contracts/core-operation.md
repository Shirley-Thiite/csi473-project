Project: Campus Lost and Found Management System
Team: CSI473 Team 12
Date: 2 October 2026
Branch: lab-08
Operation: UC-04 Report Item as Found

1. Purpose

This document specifies the API contract for one core operation: the submission of a Found Item report by a Security Personnel member or an authenticated user. This operation corresponds to UC-04 in the Phase 1 use case model, and it is the operation that most directly exercises the architecture's guarantees: it creates an Item, appends to the Chain of Custody, and may attach private evidence.

The contract defines the request, the response, the validation rules, the success outcome, and the stable error meanings. Stable error meanings matter because the front end and the tests depend on them; an error code that changes meaning between releases breaks both.

The contract is consistent with:

- The Phase 1 use case UC-04 and its acceptance criteria AC-04-01 through AC-04-07
- The Phase 1 business rules BR-01, BR-02, BR-04, BR-07
- The Phase 1 functional requirements FR-05, FR-06, FR-07, FR-08, FR-22
- The Phase 1 quality scenarios QS-03, QS-07
- The Lab 07 architecture decision in decisions/ADR-001-architecture.md
- The Lab 08 logical data model in models/logical-data-model.md
- The Lab 08 failure and recovery model in models/failure-recovery.md

2. Operation

Name: submitFoundReport
Method: POST

Authentication: Required. Bearer token from the university OIDC provider. The authenticated user must have role SECURITY or be any authenticated user (FR-05 allows authenticated users and Security personnel to report found items).

Authorization: Authenticated user. No specific role required beyond authentication.

Idempotency: The operation is idempotent within a 24-hour window for the same actor and the same (category, description, location) tuple. Repeated submissions return the existing report with a duplicate flag rather than creating a new item. This implements BR-01 (unique reporting) and is the mechanism the system uses to prevent spam. An optional Idempotency-Key header makes client retries safe after a network timeout.

Rate limiting: Not enforced in this contract. If abuse is observed, a rate limit can be added at the reverse proxy without changing the API contract.


3. Request
3.1 Headers

| Header          | Required | Description                                                                                      |
| Authorization   | Yes      | Bearer token from university OIDC provider                                                       |
| Content-Type    | Yes      | application/json (or multipart/form-data if evidence is a file)                                  |
| Idempotency-Key | No       | Optional client-generated key to make retries safe. Recommended when the client may retry after a timeout. |

3.2 Body

json
{
  "category": "Phone",
  "description": "Black smartphone with cracked screen protector",
  "locationFound": "Library, 2nd floor, near the printing station",
  "incidentDateTime": "2026-10-01T14:30:00Z",
  "evidence": [
    {
      "evidenceType": "SERIAL_NUMBER",
      "value": "IMEI-356938035643809"
    }
  ]
}


3.3 Field Validation

| Field            | Type     | Required | Validation                                                                       |
| category         | String   | Yes      |  Must be an active category in the Category table                                |
| description      | String   | Yes      | Length 10–2000 characters                                                        |
| locationFound    | String   | Yes      | Length 3–255 characters                                                          |
| incidentDateTime | ISO 8601 | Yes      | Must be in the past; no more than 30 days before submission                      |
| evidence         | Array    | No       | At most 5 items; each must have a valid evidenceType and a value of length 1–500 |

The validation order matters because the error code the client sees should be the most relevant one. The order is: authentication, authorization, required fields, field-level validation (category, description, location, incidentDateTime), evidence validation, duplicate detection.



4. Response
4.1 Success Response (201 Created)

json
{
  "itemId": "8f14e45f-ceea-467a-9c1b-3d6a1f0e2b7c",
  "status": "FOUND",
  "reportType": "FOUND",
  "createdAt": "2026-10-02T09:15:42Z",
  "chainOfCustodyEntry": {
    "entryId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "previousStatus": null,
    "newStatus": "FOUND",
    "timestamp": "2026-10-02T09:15:42Z",
    "actorId": "3c2b1a09-8f7e-6d5c-4b3a-210987654321"
  },
  "duplicate": false
}


The response includes the itemId, the item status (which is FOUND because this is a found report), and the chainOfCustodyEntry that was created in the same transaction. The evidenceBlobId is not returned in this response; it is stored securely and is not exposed to the client, not even to the reporter. The reporter can later see the evidence they submitted through their own profile, but the API contract for the public search does not expose it.

 4.2 Duplicate Response (200 OK)

When a report matches an existing report by the same actor within 24 hours (BR-01), the response returns the existing item with duplicate: true. The status code is 200 rather than 201 because no new resource was created.

json
{
  "itemId": "8f14e45f-ceea-467a-9c1b-3d6a1f0e2b7c",
  "status": "FOUND",
  "reportType": "FOUND",
  "createdAt": "2026-10-01T10:00:00Z",
  "chainOfCustodyEntry": null,
  "duplicate": true,
  "message": "A similar report already exists. Review the existing report before submitting a new one."
}


The message field is for human consumption and may change. The duplicate: true field is the contract; clients should branch on it rather than on the message.

 4.3 Error Response Shape

All error responses use the same shape:

json
{
  "error": {
    "code": "MISSING_REQUIRED_FIELD",
    "message": "Location Found is required",
    "field": "locationFound"
  }
}


The code is the stable contract. The message is for human consumption and may change. The field is present for field-level validation errors and absent for operation-level errors.

5. Error Outcomes

Errors use a stable error code and an HTTP status. The error code is the contract; the message is for human consumption and may change.

| HTTP Status            | Error Code                              | Meaning         | Cause                                | Recovery |
| 400 | INVALID_CATEGORY | The category is not an active category | Field validation | Choose a valid category and resubmit |
| 400 | INVALID_DATE | incidentDateTime is in the future or more than 30 days in the past | Field validation | Correct the date and resubmit |
| 400 | MISSING_REQUIRED_FIELD | One or more required fields are missing | Field validation | Fill in the missing field and resubmit |
| 400 | EVIDENCE_LIMIT_EXCEEDED | More than 5 evidence items submitted | Field validation | Reduce the number of evidence items and resubmit |
| 401 | UNAUTHENTICATED | No valid bearer token | Authentication | Re-authenticate with the OIDC provider |
| 403 | FORBIDDEN | The authenticated user lacks permission for this operation | Authorization | Do not retry; contact administrator |
| 409 | DUPLICATE_REPORT | A matching report exists and the client did not accept the duplicate outcome | Business rule | Review the existing report; do not create a new one |
| 422 | EVIDENCE_INVALID | Evidence value fails validation (length, type) | Field validation | Correct the evidence and resubmit |
| 500 | INTERNAL_ERROR | Unexpected server error | System | Retry; if persistent, contact support |
| 503 | EVENT_STORE_UNAVAILABLE | The Chain of Custody event store is unavailable; the report was not saved | Failure handling | Retry after a short delay; if persistent, contact support |
| 503 | BLOB_STORE_UNAVAILABLE | The Evidence module could not store evidence; the report was not saved | Failure handling | Retry, or resubmit without the evidence field |

6. Validation Rules

The following validations are performed in order. Each validation, if it fails, produces a stable error code.

V-01: Authentication. The caller must present a valid bearer token. If the token is missing or expired, the response is 401 UNAUTHENTICATED.

V-02: Role. The caller must have the role Security, Student, or Staff. If the role is Admin, the request is rejected with 403 FORBIDDEN because administrators do not report found items directly. If the role is unknown, the request is rejected with 403 FORBIDDEN.

V-03: Required fields. category, description, locationFound, and incidentDateTime must be present and non-empty. If any is missing, the response is 400 MISSING_REQUIRED_FIELD with the field attribute set to the first missing field.

V-04: Category. category must reference an active ItemCategory. If it does not, the response is 400 INVALID_CATEGORY.

V-05: Description length. description must be between 10 and 2000 characters. If it is outside this range, the response is 400 MISSING_REQUIRED_FIELD with the field attribute set to description. The same error code is used for length failures as for missing fields because both are field-level validation failures that the client can fix by editing the field.

V-06: Location length. locationFound must be between 3 and 255 characters. If it is outside this range, the response is 400 MISSING_REQUIRED_FIELD with the field attribute set to locationFound.

V-07: Incident date. incidentDateTime must be a valid ISO 8601 datetime and must not be in the future or more than 30 days in the past. If it is, the response is 400 INVALID_DATE.

V-08: Evidence limit. evidence must contain at most 5 items. If it contains more, the response is 400 EVIDENCE_LIMIT_EXCEEDED.

V-09: Evidence items. Each evidence item must have a valid evidenceType (SERIAL_NUMBER, UNIQUE_MARK, PHOTO, RECEIPT) and a value of length 1–500. If any item fails, the response is 422 EVIDENCE_INVALID with the field attribute set to the offending item's index or type.

V-10: Idempotency. If the client supplies an Idempotency-Key, it must not have been used in a previous submission by the same user within 24 hours. If it has, the server returns the result of the first attempt rather than creating a new item.

V-11: Duplicate detection (BR-01). The system checks for an existing ItemReport of type FOUND with the same category, description, and locationFound within the last 24 hours by the same user. If a match is found, the response is 200 OK with duplicate: true and the existing item.



7. Critical Failure Outcomes
7.1 EVENT_STORE_UNAVAILABLE

If the Chain of Custody event store is unavailable, the operation must not save the Item to the primary database. Saving the item without the corresponding custody entry would violate BR-07 (chain of custody completeness) and would leave the system in an inconsistent state. The Report module therefore writes the item and the custody entry in the same transaction. If the event store is unavailable, the transaction rolls back and the client receives 503 with error code EVENT_STORE_UNAVAILABLE. The client can retry with the same Idempotency-Key, and the retry will not create a duplicate because the original transaction rolled back.

7.2 BLOB_STORE_UNAVAILABLE
If evidence is submitted and the blob store is unavailable, the operation must not save the item without the evidence. Partial evidence would violate the integrity of the claim process. The Report module rolls back the transaction and returns 503 with error code BLOB_STORE_UNAVAILABLE. The client is instructed to retry later. If the evidence is optional and the client prefers to proceed without it, the client can resubmit without the evidence field; the item will be saved and the evidence can be attached later through the Admin module. This is different from the event store case because evidence is optional, but if evidence was submitted, the system must not silently drop it.

7.3 Transactional Guarantee
The Report Module and the Chain of Custody Module participate in a single transaction. The Item is inserted into the Primary DB, and the ChainOfCustodyEntry is appended to the Event Store. If either operation fails, the transaction is rolled back, and no partial state is committed. This is the mechanism that enforces BR-07 and prevents an unlogged status change.

8. Idempotency and Retry
The operation accepts an optional Idempotency-Key header. If the client retries with the same key after a timeout, the server returns the result of the first attempt rather than creating a second item. This matters because the operation involves multiple side effects (item creation, custody append, evidence storage) and a network timeout between the client and the server could otherwise cause a duplicate report.

If the original attempt failed with 503 (EVENT_STORE_UNAVAILABLE or BLOB_STORE_UNAVAILABLE), the server has rolled back the transaction, so the retry with the same Idempotency-Key will be treated as a fresh attempt. The key is stored with a short TTL (24 hours) for this purpose.

If the client does not supply an Idempotency-Key, the duplicate detection within 24 hours still applies, so a retry that succeeds will not create a duplicate if the original attempt had actually committed. The combination of the transaction rollback and the duplicate detection makes the retry safe without requiring the client to implement idempotency, but supplying the key is recommended.

9. Traceability

| Element                                        | Source                           |
| UC-04 Report Item as Found                     | Phase 1 use case model           |
| AC-04-01 User must be authenticated            | Phase 1 acceptance criteria      |
| AC-04-02 All required fields must be completed | Phase 1 acceptance criteria      |
| AC-04-03 Category must be from valid list      | Phase 1 acceptance criteria      |
| AC-04-04 Duplicate reports are detected        | Phase 1 acceptance criteria      |
| AC-04-05 Report receives unique identifier     | Phase 1 acceptance criteria      |
| AC-04-06 Chain of custody entry is created     | Phase 1 acceptance criteria      |
| AC-04-07 Private evidence is stored securely   | Phase 1 acceptance criteria      |
| BR-01 Unique Reporting                         | Phase 1 business rules           |
| BR-02 Status Transition Order                  | Phase 1 business rules           |
| BR-04 Evidence Privacy                         | Phase 1 business rules           |
| BR-07 Chain of Custody Completeness            | Phase 1 business rules           |
| QS-03 Security                                 | Phase 1 quality scenarios        |
| QS-07 Data Integrity                           | Phase 1 quality scenarios        |
| FR-05 Found Item reporting                     | Phase 1 functional requirements  |
| FR-06 Required report fields                   | Phase 1 functional requirements  |
| FR-07 Optional private evidence                | Phase 1 functional requirements  |
| FR-08 Duplicate report prevention              | Phase 1 functional requirements  |
| FR-22 Immutable chain of custody               | Phase 1 functional requirements  |
| ADR-001 Alternative A                          | decisions/ADR-001-architecture   |
| Logical Data Model                             | models/logical-data-model        |
| Failure and Recovery Model                     | models/failure-recovery          |

10. Related Artefacts

The Lab 07 architecture options are in docs/architecture-options.md. The decision record is in decisions/ADR-001-architecture.md. The quality-to-architecture traceability is in docs/quality-to-architecture.md. The Lab 08 logical data model is in models/logical-data-model.md. The Lab 08 deployment model is in models/deployment.md. The Lab 08 failure and recovery model is in models/failure-recovery.md. The Lab 08 data integrity constraints are in docs/data-integrity.md. The Lab 08 evidence index is in evidence/lab-08/README.md.