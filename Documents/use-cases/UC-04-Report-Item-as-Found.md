
---

## **File 3: `docs/use-cases/UC-04-Report-Item-as-Found.md`**

```markdown
# Use Case: Report Item as Found

- **Use Case Name:** Report Item as Found
- **ID:** UC-04
- **Primary Actor:** Security Personnel
- **Secondary Actor:** Student, Staff/Faculty

## Stakeholders

- **Finder:** Wants to ensure the item is returned to its rightful owner quickly and securely.
- **Owner:** Wants to be able to search for and claim their lost item.
- **Administrator:** Needs accurate and complete data to facilitate a match and maintain the chain of custody.

## Preconditions

1. The actor is authenticated to the system.

## Postconditions

1. A new Found Item report is created with a unique ID, a timestamp and the identity of the reporter.
2. The item's status is set to FOUND.
3. The chain of custody is updated with an entry logging the item's report creation.

## Normal Flow

1. The actor selects the "Report Found Item" option.
2. The system presents a Found Item Report Form.
3. The actor inputs the required details: Item Category, Description, Location Found, Date/Time Found.
4. The actor optionally inputs the item's serial number, unique marks or uploads a photo of the item.
5. The actor submits the form.
6. The system validates all required fields are completed.
7. The system records the report with a unique ID, the current timestamp and the actor's identity.
8. The system updates the item's status to FOUND.
9. The system logs the creation in the Chain of Custody.
10. The system acknowledges receipt of the report to the actor.

## Alternative Flows

- **A1 - Missing Required Fields:** If a required field is missing, the system informs the actor of the specific missing field and prevents submission. The actor must fill in the missing data before re-submitting. The use case returns to step 3.

- **A2 - Actor is not Authenticated:** The system redirects the actor to the authentication page. The use case is suspended until successful authentication, after which the actor is returned to the "Report Found Item" option.

- **A3 - System Fails to Save:** If a system error occurs while saving the data, the system logs the error and notifies the actor that the report could not be submitted, asking them to try again later.

## Special Requirements

- The Found Item Report Form must be responsive for use on mobile devices by Security Personnel.
- Private evidence like serial numbers and photos must be stored in a secure database table and not visible on public listings.

## Related Requirements

- FR-05: The system shall provide a form for authenticated users and Security personnel to report an item as Found.
- FR-06: The item report form shall capture required details.
- FR-07: The system shall allow optional private evidence to be included.
- FR-08: The system shall record timestamp and identity.
- FR-22: The system shall maintain an immutable Chain of Custody record.