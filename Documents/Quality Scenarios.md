# Quality Scenarios

These scenarios specify measurable quality attributes for the system.

## 1. Performance

| Attribute | Value |
|-----------|-------|
| **Stimulus** | An Administrator initiates a search for a specific item in the database |
| **Context** | The system is under normal operating load |
| **Expected Response** | The system displays the search results within 3 seconds |
| **Measurable Response Level** | Average search response time ≤ 3 seconds |

---

## 2. Availability

| Attribute | Value |
|-----------|-------|
| **Stimulus** | A student attempts to report a lost item |
| **Context** | The system is in operation during core university hours (8:00 AM - 10:00 PM) |
| **Expected Response** | The system is available to accept the report |
| **Measurable Response Level** | System uptime of ≥ 99.0% during core university hours |

---

## 3. Security

| Attribute | Value |
|-----------|-------|
| **Stimulus** | A user attempts to access the serial number field of an item listing via the public search page |
| **Context** | The user is authenticated but is not an Administrator |
| **Expected Response** | The system prevents the disclosure of the serial number, displaying a "Private" placeholder or no value |
| **Measurable Response Level** | 100% of attempts by non-administrative users to view private evidence are blocked |

---

## 4. Usability

| Attribute | Value |
|-----------|-------|
| **Stimulus** | A new security guard is asked to report a found item after a 15-minute training session |
| **Context** | The system's UI is in its final, production state |
| **Expected Response** | The guard can complete the Report Found Item workflow without any assistance |
| **Measurable Response Level** | ≥ 90% of users can successfully complete the workflow on their first try after brief training |

---

## 5. Maintainability (Modifiability)

| Attribute | Value |
|-----------|-------|
| **Stimulus** | A new "Wallet" item category needs to be added to the list of item categories |
| **Context** | The system is live and in production |
| **Expected Response** | An Administrator or Developer can add the new category to the system's configuration within 30 minutes |
| **Measurable Response Level** | Time to modify the category list in a production environment is ≤ 30 minutes |

---

## 6. Reliability (Additional)

| Attribute | Value |
|-----------|-------|
| **Stimulus** | The system experiences a database connection failure during report submission |
| **Context** | The system is under normal operating load |
| **Expected Response** | The system gracefully handles the error and notifies the user to try again |
| **Measurable Response Level** | Error recovery is successful in ≥ 99.9% of failure cases |

---

## 7. Data Integrity (Additional)

| Attribute | Value |
|-----------|-------|
| **Stimulus** | An Administrator updates an item's status |
| **Context** | The system is in normal operation |
| **Expected Response** | The Chain of Custody record is updated with the change, timestamp, and actor identity |
| **Measurable Response Level** | 100% of status changes are logged with complete audit trail |