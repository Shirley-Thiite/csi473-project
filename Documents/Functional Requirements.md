# Functional Requirements

## User Authentication & Profile

- **FR-01:** The system shall allow users to register and create an account using their university credentials.

- **FR-02:** The system shall authenticate users before allowing them to report items, search the database or submit claims.

- **FR-03:** The system shall allow users to update their profile information e.g contact email, phone number.

## Item Reporting

- **FR-04:** The system shall provide a form for authenticated users to report an item as Lost.

- **FR-05:** The system shall provide a form for authenticated users and Security personnel to report an item as Found.

- **FR-06:** The item report form Lost or Found shall capture the following details:
  - Item category (e.g Phone, Laptop, ID Card, Keys)
  - A detailed description of the item
  - Location where the item was lost or found
  - Date and time of the incident

- **FR-07:** The system shall allow a user reporting a lost item to include optional private evidence e.g serial number, unique marks or a photo of a similar item. This evidence must be stored securely.

- **FR-08:** The system shall record the timestamp and identity of the user or administrator who created the report.

## Search & Matching

- **FR-09:** The system shall provide a searchable database for all users to find reported lost and found items.

- **FR-10:** The system shall automatically scan new reports against existing entries to generate a list of potential matches based on key attributes (e.g category, location and description keywords).

- **FR-11:** The system shall only present suggested matches to an Administrator not to end-users to prevent automated claims.

- **FR-12:** The system shall allow an Administrator to manually search the database to check if a physical item handed in was logged in the system.

## Claim and Verification

- **FR-13:** The system shall notify a user via in-app notification or email when a potential match for their lost item is found.

- **FR-14:** The system shall provide a form for users to submit a claim on an item that has been found.

- **FR-15:** The claim submission form shall allow the user to provide proof of ownership (e.g serial number, unique marks) to support their claim.

- **FR-16:** The system must enforce that no claim can be approved without administrative review. This is a critical non-automated step.

- **FR-17:** The system shall allow an Administrator to review the claim and the private evidence submitted by the user.

- **FR-18:** The system shall allow an Administrator to approve or reject a claim.

- **FR-19:** Upon rejection, the system shall notify the claimant with the reason for rejection and make the item available for other potential claimants.

- **FR-20:** Upon approval, the system shall update the item's status to Resolved and initiate the handover process.

## Administration & Chain of Custody

- **FR-21:** The system shall provide a secure administrative interface for managing all items and claims.

- **FR-22:** The system shall maintain an immutable Chain of Custody record for each item, logging all status changes with a timestamp and the actor who performed the action.

- **FR-23:** The system shall allow an Administrator to update the status of an item.

- **FR-24:** The system shall allow an Administrator to create a handover record that includes pickup time, location and verification notes upon approving a claim.

## Security & Data Management

- **FR-25:** The system shall implement Role-Based Access Control (RBAC) to restrict access to sensitive data.

- **FR-26:** The system shall securely hash all user passwords before storing them in the database.

- **FR-27:** The system shall store private evidence (serial numbers, unique marks) in a separate, secure database table with restricted access.

- **FR-28:** The system shall ensure that private evidence is never displayed on public item listings or to other non-administrative users.