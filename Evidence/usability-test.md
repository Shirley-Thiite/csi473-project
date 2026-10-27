Usability Test Record — Report Found Item (UC-04)

Project: Campus Lost and Found Management System
Team: CSI473 Team 12
Date: 23 October 2026
Branch: lab-10
Task tested: Report a Found Item (UC-04)
Method: Task-based usability check with three participants

1. Purpose

This document records the task-based usability check that was run during Lab 10. The purpose is to observe real users attempting the primary task (Report a Found Item), identify the usability problems they encounter, revise the design in response, and state how the revision will be verified after implementation.

The test was run against a low-fidelity clickable prototype based on the wireframes in prototype/wireframes.md, not against the real system. This is consistent with the Lab 10 brief, which asks for a three-person task-based check at the design stage.

2. Test Setup

 2.1 Participants

Three participants were recruited. None were team members.

Participant 1. Student, 21 years old. Uses a smartphone daily and a laptop for coursework. Has used the university's online systems for course registration.

Participant 2. Student, 23 years old. Uses a smartphone daily. Has previously lost a phone and searched the physical lost-and-found box at the library.

Participant 3. Staff member, 47 years old. Previously worked at a security desk for two years. Uses a desktop computer daily for administrative work. Has used the paper lost-and-found log book at the security desk.

The participants represent the two main users of the Report Found Item workflow: students who might occasionally report a found item, and a former security officer who reported found items as part of their job.

 2.2 Task

Scenario given to each participant. "A student has just handed you a found smartphone at the security desk. Use this prototype to log the item in the system so the owner can find it."

Success criterion. Complete the task — including submitting the form and reaching the confirmation page — in under 2 minutes without assistance from the observer.

Stop condition. If a participant was stuck for more than 60 seconds without making progress, the observer asked a neutral prompt ("What are you looking for?") but did not point to the answer.

 2.3 Method

Each participant was tested individually in the same room, one at a time. The observer explained the scenario, showed the participant the clickable prototype (a sequence of linked wireframe images), and asked the participant to think aloud while completing the task. The observer took notes in real time. The session was not recorded on video, to reduce participant anxiety and to avoid the need for consent forms for a Lab 10 exercise.

The prototype included the normal state, the validation error state, and the unavailable state. The participant was not told in advance which state would appear.

 2.4 Environment

The prototype was shown on a laptop running a browser in full-screen mode. The browser window was set to 1024 px wide, which is the typical width of the desktop computer at the security desk. The participant used the laptop's keyboard and trackpad.

 3. Observations

 3.1 Participant 1 (Student, 21)

Task completed. Yes. Time: 1 minute 40 seconds.

Observation. Participant 1 moved through the form smoothly until reaching the "Private evidence (optional)" field. They paused, looked at the field, and said "I don't know what goes here." They then noticed the helper text ("Allowed: jpg, png, pdf. Max 5 MB.") and said "Oh, it's a file." They clicked "Choose file" and completed the task without further problems.

Quote. "The form is shorter than I expected. That's good. But I didn't know what the evidence field was for until I read the small text."

 3.2 Participant 2 (Student, 23)

Task completed. Yes, but with one validation error. Time: 2 minutes 10 seconds.

Observation. Participant 2 filled in the Category field, then jumped straight to the Description text area, then to the Date and time field, and then clicked "Submit Report" without entering the Location Found field. The form returned to the validation error state, and the participant saw the error "Location Found is required" directly below the Location field. They said "Oh, I missed that one." They filled it in and resubmitted, reaching the confirmation page.

The observer noted that Participant 2's eye path skipped the Location Found field, which sits between the Description text area and the Date and time field. The large text area visually dominates the field above and below it.

Quote. "The location field is kind of hidden. It's between the big box and the date."

 3.3 Participant 3 (Staff, 47, former security desk)

Task completed. Yes. Time: 1 minute 30 seconds.

Observation. Participant 3 moved through the form without any issues and completed the task on the first attempt. When asked about the validation error state (which was not triggered in their session), they said the inline error message looked clear. When asked about the unavailable state (also not triggered), they said "If it says my report wasn't saved, that's the important thing. I can just try again."

Quote. "The form makes sense. It's not much longer than the paper log."

 3.4 Summary of Times

| Participant | Time | Errors | Completed |
|-------------|------|--------|-----------|
| Participant 1 | 1 min 40 s | 0 | Yes |
| Participant 2 | 2 min 10 s | 1 | Yes |
| Participant 3 | 1 min 30 s | 0 | Yes |

Average time: 1 minute 47 seconds.
Median time: 1 minute 40 seconds.
Participants within the 2-minute target: 2 of 3.
Participants who completed the task: 3 of 3.

 4. Findings

Two usability problems were observed. A third finding is a positive observation that we are recording for traceability, not as a problem.

 4.1 Finding 1: The Location Found field is skipped on first pass

Evidence. Participant 2 skipped the field entirely and submitted without it, triggering a validation error. The field sits between the Description text area and the Date and time field, and the visual hierarchy does not draw the eye to it. This is not a wording problem (the label "Location Found" is clear); it is a layout and visual hierarchy problem.

Impact. The user experiences a validation error on the most frequent task, which adds time and frustration. The API contract rejects the submission with MISSING_REQUIRED_FIELD, so the user is forced to correct the error before proceeding.

Severity. Medium. The user can recover, and the error message is clear. But on the most frequent task, a recovered error is still a cost.

 4.2 Finding 2: The Private evidence label is not self-explanatory

Evidence. Participant 1 paused at the field and said "I don't know what goes here" until reading the helper text. The label "Private evidence (optional)" does not say what kind of evidence is expected; the helper text does, but the user has to read past the label to find out.

Impact. The user hesitates, which slows down the task. For a user who does not read the helper text (for example, a user with a screen reader that announces the label but skips the small text), the field may be skipped entirely.

Severity. Low to medium. The user recovers by reading the helper text, but the label itself does not do its job.

 4.3 Positive observation: The unavailable state message is clear

Evidence. Participant 3, when asked about the unavailable state, said "If it says my report wasn't saved, that's the important thing." The unavailable state message states explicitly that the report was *not* saved and offers a Retry button, and the input is preserved.

Why we record it. The failure state from Lab 08 translates into a clear user-facing message. We are recording this because it shows that the failure design from Lab 08 is usable, not just technically correct. This is a design strength, not a problem, but it is worth noting for traceability.

 5. Revisions

Two revisions were made to the wireframes in response to the findings. Both were made after the test, and the revised wireframes in prototype/wireframes.md reflect them.

 5.1 Revision 1 (from Finding 1): Reorder the form to put short fields first

Change. The "Location Found" field was moved above the Description text area. The form order is now:

1. Category
2. Location Found
3. Date and time
4. Description
5. Private evidence (optional)

This puts all short fields before the long description field, which follows the common form design heuristic of "short fields first, long fields last." The eye is drawn to the short fields first, and the large text area is at the bottom, where the user naturally expects the longest input.

Why this revision. The problem was not that the Location field was missing; it was that the user's eye skipped it because it sat between two visually dominant elements (the large text area and the date/time row). Reordering the form removes the visual dominance problem without changing the field labels or the validation logic.

Alternative considered. Making the Location field larger or bolder. We rejected this because it would make the form visually noisy, and the problem was the surrounding fields, not the Location field itself.

 5.2 Revision 2 (from Finding 2): Make the evidence label self-explanatory

Change. The label was changed from "Private evidence (optional)" to "Proof of ownership (optional, e.g. serial number or photo)." The helper text ("Allowed: jpg, png, pdf. Max 5 MB.") remains.

Why this revision. The label itself now tells the user what kind of evidence is expected, without requiring them to read the helper text. This is important for screen reader users, who may announce the label but skip the smaller helper text if it is not associated with the field.

Alternative considered. Adding an info icon with a tooltip. We rejected this because tooltips are not always accessible and because the label change is simpler and more robust.

 6. Verification After Implementation

The revisions will be verified after the wireframes are implemented in the real system.

Revision 1 verification. A second usability test with three new participants, using the implemented system (not the prototype), will measure whether the "Location Found" field is skipped on first pass. The pass criterion is that all three participants complete the task in under 2 minutes on their first attempt, without a validation error for the Location field.

Revision 2 verification. A short comprehension test with three new participants: each is shown the form and asked "What kind of evidence can you attach here?" The pass criterion is that all three participants name a serial number, a photo, or a similar concrete example, without reading the helper text.

Schedule. These verifications are scheduled for the implementation phase, after the wireframes are built. They are recorded here so that the revision is linked to its verification, and so the marker can see that the design change is not just a claim but a change with a stated test.

 7. What the Test Did Not Cover

The test was run with three participants, which is the minimum the Lab 10 brief asks for. It is not statistically meaningful, and it would not detect problems that affect only users with severe visual impairments or motor disabilities. The accessibility checklist in docs/accessibility-checklist.md is the primary evidence for those users; this usability test is the evidence for the primary user journey.

Two specific gaps are worth noting:

No participant used a screen reader. The test measured the visual and cognitive flow of the form, not the screen reader experience. A future round of testing should include at least one participant who uses a screen reader, to verify that the labels are announced correctly, the error messages are announced when the field receives focus, and the state changes (loading, error, success) are announced.

No participant used keyboard-only navigation. The test measured mouse and trackpad use. A future round should include at least one participant who completes the form using only the keyboard, to verify that focus order is logical and that the focus indicator is visible on every field.

Both of these gaps are recorded as open items in the Lab 10 accessibility checklist (Section 7 of docs/accessibility-checklist.md) and will be addressed before the system is deployed.

 8. Traceability

| Finding | Related Requirement | Related Business Rule | Related API Code | Revision | Verification |
|---------|---------------------|-----------------------|------------------|----------|--------------|
| Location field skipped | FR-06 (required fields), QS-04 (usability) | — | MISSING_REQUIRED_FIELD | Form reorder | Second usability test: 3 participants, < 2 min, first attempt |
| Evidence label unclear | FR-07 (optional private evidence), QS-04 | — | — | Label rewrite | Comprehension test: 3 participants name an example |
| Unavailable state clear (positive) | FR-22 (custody), BR-07 | BR-07 | EVENT_STORE_UNAVAILABLE | No change | — |

 9. Exit Record

Most important usability problem observed. Participant 2 skipped the "Location Found" field on the first pass because it sat between the large Description text area and the Date and time row, and the visual hierarchy did not draw the eye to it. This caused a validation error on the most frequent task, which added time and frustration.

Interface change made. The form was reordered to put short fields (Category, Location Found, Date and time) before the long Description text area. The "Private evidence (optional)" label was changed to "Proof of ownership (optional, e.g. serial number or photo)" so that the label itself conveys what kind of evidence is expected.

How it will be verified after implementation. A second usability test with three new participants will measure whether the Location field is still skipped on the first pass, with a pass criterion of all three participants completing the task in under 2 minutes without a Location validation error. A comprehension test will check whether the revised label is self-explanatory.

 10. Related Artefacts

The user profiles and task scenarios are in docs/user-profile-and-tasks.md. The navigation model is in models/navigation.puml and models/navigation.svg. The wireframes are in prototype/wireframes.md. The UI states are in docs/ui-states.md. The accessibility checklist is in docs/accessibility-checklist.md. The API contract is in docs/api-contracts/core-operation.md. The failure and recovery model is in models/failure-recovery.md. The data integrity constraints are in docs/data-integrity.md.
