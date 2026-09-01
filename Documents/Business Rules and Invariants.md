 Business Rules and Invariants
BR-01: Unique Reporting
•	Rule: A user cannot report the same item as lost more than once within a 24-hour period
•	Rationale: Prevents duplicate reports and system spam
•	Implementation: Check for existing reports with same description/location within timeframe
BR-02: Status Transition Rules
•	Rule: An item's status can only transition in the following sequence: LOST → FOUND → RESOLVED
•	Rationale: Maintains proper item lifecycle and chain of custody
•	Implementation: Validate status transitions before updating
BR-03: Claim Restrictions
•	Rule: A user cannot claim an item they reported as lost
•	Rationale: Prevents fraudulent claims and conflicts of interest
•	Implementation: Check reporter identity against claimant identity
BR-04: Evidence Privacy
•	Rule: Private evidence (serial numbers, unique marks) must never be visible to non-administrative users
•	Rationale: Protects sensitive information and prevents false claims
•	Implementation: RBAC enforcement at data access level
BR-05: Handover Requirement
•	Rule: A handover record must be created within 7 days of claim approval
•	Rationale: Ensures timely resolution of claims and prevents items from being unclaimed
•	Implementation: Scheduler to monitor pending handovers
BR-06: Authentication Required
•	Rule: All actions except registration and login require authentication
•	Rationale: Ensures system security and accountability
•	Implementation: Authentication middleware on all protected endpoints
BR-07: Chain of Custody Completeness
•	Rule: Every status change of an item must be recorded in the Chain of Custody with timestamp and actor identity
•	Rationale: Maintains complete audit trail for legal and security purposes
•	Implementation: Trigger-based logging on all status updates
BR-08: Unique Claim per Item per User
•	Rule: A user can only submit one active claim per found item
•	Rationale: Prevents multiple claims from same user and reduces administrative burden
•	Implementation: Check for existing active claims before submission

 Business Rules and Invariants
BR-01: Unique Reporting
•	Rule: A user cannot report the same item as lost more than once within a 24-hour period
•	Rationale: Prevents duplicate reports and system spam
•	Implementation: Check for existing reports with same description/location within timeframe
BR-02: Status Transition Rules
•	Rule: An item's status can only transition in the following sequence: LOST → FOUND → RESOLVED
•	Rationale: Maintains proper item lifecycle and chain of custody
•	Implementation: Validate status transitions before updating
BR-03: Claim Restrictions
•	Rule: A user cannot claim an item they reported as lost
•	Rationale: Prevents fraudulent claims and conflicts of interest
•	Implementation: Check reporter identity against claimant identity
BR-04: Evidence Privacy
•	Rule: Private evidence (serial numbers, unique marks) must never be visible to non-administrative users
•	Rationale: Protects sensitive information and prevents false claims
•	Implementation: RBAC enforcement at data access level
BR-05: Handover Requirement
•	Rule: A handover record must be created within 7 days of claim approval
•	Rationale: Ensures timely resolution of claims and prevents items from being unclaimed
•	Implementation: Scheduler to monitor pending handovers
BR-06: Authentication Required
•	Rule: All actions except registration and login require authentication
•	Rationale: Ensures system security and accountability
•	Implementation: Authentication middleware on all protected endpoints
BR-07: Chain of Custody Completeness
•	Rule: Every status change of an item must be recorded in the Chain of Custody with timestamp and actor identity
•	Rationale: Maintains complete audit trail for legal and security purposes
•	Implementation: Trigger-based logging on all status updates
BR-08: Unique Claim per Item per User
•	Rule: A user can only submit one active claim per found item
•	Rationale: Prevents multiple claims from same user and reduces administrative burden
•	Implementation: Check for existing active claims before submission

