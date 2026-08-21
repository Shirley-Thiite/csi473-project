# Acceptance Criteria

## UC-04: Report Item as Found by Security Personnel

### AC-01: Successful Submission
**Given** a Security Personnel member is authenticated to the system  
**When** they submit a completed Report Found Item form with all required fields  
**Then** the system creates a new Found Item report, assigns a unique ID and displays a success message

### AC-02: Missing Required Field Validation
**Given** a Security Personnel member is authenticated to the system  
**When** they submit a Report Found Item form missing the Location Found field  
**Then** the system prevents submission, highlights the missing field and displays an error message "Location Found is required"

### AC-03: Status and Chain of Custody Update
**Given** a Security Personnel member is authenticated to the system and an item has been reported found  
**When** the report is saved in the system's database  
**Then** the item's status is set to FOUND and the Chain of Custody log is updated with a new entry including the timestamp and actor's identity

---

## Additional Acceptance Criteria (Inferred)

### AC-04: User Registration
**Given** a user with valid university credentials  
**When** they complete the registration form  
**Then** their account is created and they receive a confirmation

### AC-05: Authentication
**Given** a registered user  
**When** they provide valid credentials  
**Then** they are granted access to the system

### AC-06: Claim Submission
**Given** a user has found a matching item  
**When** they submit a claim with proof of ownership  
**Then** the claim is recorded and pending administrative review

### AC-07: Claim Approval
**Given** an Administrator reviews a claim with valid proof  
**When** they approve the claim  
**Then** the item status is updated to Resolved and a handover record is created

### AC-08: Claim Rejection
**Given** an Administrator reviews a claim with insufficient proof  
**When** they reject the claim  
**Then** the claimant is notified with a reason and the item remains available