Consistency Matrix: UC-04 Report Item as Found

Requirement/ID: FR-05 - Report Found Item form
Use Case Step: Step 1: Select "Report Found Item"
Sequence Message: SP -> UI: Select option
Domain Responsibility: UI presents form
State Transition: -
Business Rule: BR-06: Authentication required
Quality Scenario: Usability: Security guard completes workflow after 15-min training

Requirement/ID: FR-06 - Capture required details
Use Case Step: Step 3: Input required fields
Sequence Message: SP -> UI: Input fields
Domain Responsibility: Form fields mapped to Report entity
State Transition: -
Business Rule: -
Quality Scenario: -

Requirement/ID: FR-07 - Optional private evidence
Use Case Step: Step 4: Input optional private evidence
Sequence Message: SP -> UI: Input serial number, marks, photo
Domain Responsibility: PrivateEvidence entity stored securely
State Transition: -
Business Rule: BR-04: Evidence privacy
Quality Scenario: Security: 100% of private evidence access attempts blocked for non-admins

Requirement/ID: FR-08 - Record timestamp & identity
Use Case Step: Step 7: Record report with timestamp & identity
Sequence Message: Controller -> Repo: saveReport(reportData, reportID, timestamp, actor)
Domain Responsibility: Report metadata includes reporter identity and creation timestamp
State Transition: -
Business Rule: -
Quality Scenario: -

Requirement/ID: FR-22 - Immutable Chain of Custody
Use Case Step: Step 9: Log creation in Chain of Custody
Sequence Message: Controller -> Custody: updateCustodyLog(itemID, "FOUND", timestamp, actor)
Domain Responsibility: ChainOfCustody entry created
State Transition: LOST → FOUND
Business Rule: BR-07: Chain of Custody completeness
Quality Scenario: Data Integrity: 100% of status changes logged

Requirement/ID: UC-04 Main Flow
Use Case Step: Step 6: Validate fields
Sequence Message: Controller -> Validator: validateRequiredFields()
Domain Responsibility: ValidationService
State Transition: -
Business Rule: -
Quality Scenario: -

Requirement/ID: AC-01 - Successful submission
Use Case Step: Step 10: Display success message
Sequence Message: UI -> SP: Display success message
Domain Responsibility: Success response generated
State Transition: LOST → FOUND
Business Rule: BR-02: Status transition rules
Quality Scenario: -

Requirement/ID: AC-02 - Missing field validation
Use Case Step: Step 6: Validation fails
Sequence Message: Validator -> Controller: Validation Failed
Domain Responsibility: ValidationError exception
State Transition: -
Business Rule: -
Quality Scenario: Performance: Search results < 3 sec

Requirement/ID: AC-03 - Status & Chain of Custody update
Use Case Step: Step 8-9: Status set to FOUND, Custody log updated
Sequence Message: Controller -> Repo: saveReport(); Controller -> Custody: updateCustodyLog()
Domain Responsibility: FoundReport created with status FOUND; Custody entry
State Transition: LOST → FOUND
Business Rule: BR-07, BR-02
Quality Scenario: Data Integrity: Complete audit trail

Requirement/ID: FR-25 - RBAC
Use Case Step: Throughout
Sequence Message: UI -> Auth: checkAuthentication()
Domain Responsibility: AuthenticationMiddleware
State Transition: -
Business Rule: BR-06: Authentication required
Quality Scenario: Security: Access control enforced

Requirement/ID: BR-01 - Unique reporting
Use Case Step: Pre-condition check
Sequence Message: -
Domain Responsibility: ReportRepository
State Transition: LOST → LOST (prevents duplicate)
Business Rule: BR-01: Unique reporting
Quality Scenario: -

Requirement/ID: BR-02 - Status transition rules
Use Case Step: Step 8
Sequence Message: Controller -> Repo: saveReport() with status FOUND
Domain Responsibility: Validates LOST → FOUND transition
State Transition: LOST → FOUND
Business Rule: BR-02: Status transition rules
Quality Scenario: -

Requirement/ID: BR-07 - Chain of Custody completeness
Use Case Step: Step 9
Sequence Message: Controller -> Custody: updateCustodyLog()
Domain Responsibility: Custody entry records all status changes
State Transition: All transitions
Business Rule: BR-07: Chain of Custody completeness
Quality Scenario: Data Integrity: 100% logging

Requirement/ID: Quality: Availability
Use Case Step: Step 7-10
Sequence Message: System responses
Domain Responsibility: System accepts reports
State Transition: -
Business Rule: -
Quality Scenario: Availability: 99% uptime during 8AM-10PM

Requirement/ID: Quality: Reliability
Use Case Step: Step 8
Sequence Message: System saves to DB
Domain Responsibility: Error handling if DB connection fails
State Transition: -
Business Rule: -
Quality Scenario: Reliability: 99.9% error recovery success rate

