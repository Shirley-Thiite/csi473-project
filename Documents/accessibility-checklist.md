
Accessibility Checklist — Campus Lost and Found Management System

Project: Campus Lost and Found Management System
Team: CSI473 Team 12
Date: 23 October 2026
Branch: lab-10
Target standard: WCAG 2.1 Level AA


1. Purpose

This document is the accessibility checklist for the Campus Lost and Found Management System. It states the accessibility requirements that the user interface must satisfy, how each requirement is met in the wireframes and the UI states, and how compliance will be verified.

The checklist is written against WCAG 2.1 Level AA, which is the level the university requires for its public-facing web applications. It is consistent with the design decisions in prototype/wireframes.md, the states in docs/ui-states.md, the user profiles in docs/user-profile-and-tasks.md, and the usability test in evidence/usability-test.md.

The checklist covers the two core workflows — Report Found Item (UC-04) and Verify Claim (UC-06) — because these are the workflows the Lab 10 wireframes cover. Other screens will be added to the checklist when they are designed.


2. Checklist

2.1 Keyboard Access

Requirement. Every interactive element must be reachable and operable using only a keyboard.

How the design meets it. All form fields are native <input>, <select>, <textarea>, and <button> elements, which are keyboard-accessible by default. The date and time pickers use native browser controls where available. The file upload field is a native file input, not a custom drag-and-drop zone. The error summary links are focusable and jump to the field they refer to. The "Show full evidence" button on the administrator review page is a native button, not a clickable <div>.

Verification. Manual test: complete Report Found Item using only the Tab key, the Shift+Tab key, and Enter. Pass criterion: the entire form can be completed and submitted without using a mouse or trackpad.

WCAG reference. 2.1.1 Keyboard, 2.1.2 No Keyboard Trap.


2.2 Visible Focus

Requirement. The keyboard focus indicator must be visible and must meet contrast requirements.

How the design meets it. Every interactive element has a visible focus ring, implemented as an outline or a box shadow with at least 3:1 contrast against the background. The focus ring is not removed or hidden. The CSS for the focus ring is applied at the framework level, so a developer cannot accidentally remove it.

Verification. Visual inspection: tab through every interactive element on the Report Found Item form and the Claim Review page and confirm the focus ring is visible on every element. Also verify against grayscale rendering, to confirm the focus ring is not conveyed only by colour.

WCAG reference. 2.4.7 Focus Visible.


2.3 Persistent Labels

Requirement. Every form field has a visible, persistent label. Placeholders are not used as the only label.

How the design meets it. Every field has a <label> element associated with the input via for and id. The label is above the field, not inside it. Placeholder text is used only for additional hints, never for the label. The label text remains visible even when the field is filled in.

Verification. Screen reader test: navigate through the Report Found Item form with a screen reader and confirm that each field is announced with its label. Visual inspection: confirm that no field relies on a placeholder for its label, and that removing focus or entering text does not hide the label.

WCAG reference. 1.3.1 Info and Relationships, 3.3.2 Labels or Instructions.


2.4 Non-Colour Cues

Requirement. Information must not be conveyed by colour alone.

How the design meets it. Required fields are marked with * and the text * = required at the bottom of the form, so the requirement is not conveyed only by an asterisk (which some users do not associate with "required"). Errors are shown with an error icon and text ("Location Found is required"), not only with a red border. Success is shown with a checkmark icon and text, not only with a green background. The evidence state on the administrator review page shows the masked value and the "Show full evidence" button as text, not only as a coloured lock icon.

Verification. Grayscale rendering test: take a screenshot of the form in normal, error, and success states, convert to grayscale, and confirm that each state is still distinguishable. Also confirm with a colour-blindness simulator.

WCAG reference. 1.4.1 Use of Color.


2.5 Clear Errors

Requirement. Error messages must identify the field and the problem, in text, near the field.

How the design meets it. Error messages appear directly below the field where the error occurred, and the message names both the field and the problem ("Location Found is required"). An error summary appears at the top of the form with links to each invalid field. The invalid field has aria-invalid="true" and aria-describedby pointing to its error message, so a screen reader announces the error when the field receives focus.

Verification. Manual test: submit the form with each required field missing, one at a time, and confirm the error message names the field and appears near the field. Screen reader test: confirm the error is announced when the invalid field receives focus.

WCAG reference. 3.3.1 Error Identification, 3.3.3 Error Suggestion.


2.6 Readable and Responsive Layout

Requirement. The layout must reflow to fit narrow screens and must remain readable at all sizes.

How the design meets it. The layout uses a single-column responsive grid. Form fields reflow to full width at narrow screen sizes. The body font size is at least 16 px. Line length is at most 80 characters on wide screens. Horizontal scrolling is not required at any screen width from 320 px to 1440 px.

Verification. Browser resize test at 320 px, 768 px, 1024 px, and 1440 px. Pass criterion: no horizontal scroll bar, all fields are visible and usable, and text is readable without zooming.

WCAG reference. 1.4.4 Resize Text, 1.4.10 Reflow.


2.7 Sufficient Contrast

Requirement. Text and UI components must have sufficient contrast against their background.

How the design meets it. Body text has at least 4.5:1 contrast against its background. Large text (at least 18 pt or 14 pt bold) has at least 3:1 contrast. UI components (buttons, focus rings, error borders) have at least 3:1 contrast against adjacent colours. The colour palette was checked with a contrast tool before the wireframes were finalised.

Verification. Contrast checker on every text style and UI component in the wireframes. The results are recorded in the checklist log (Section 3).

WCAG reference. 1.4.3 Contrast (Minimum), 1.4.11 Non-text Contrast.


2.8 Alternative Text for Icons

Requirement. Icons that convey meaning must have alternative text. Decorative icons must be hidden from assistive technology.

How the design meets it. Icons that convey meaning (the error icon, the success checkmark, the evidence lock) have alt text that describes their meaning ("Error", "Success", "Evidence is private"). Decorative icons (arrow icons, spacing icons) have alt="" and role="presentation" so they are skipped by screen readers.

Verification. Inspect each icon in the wireframes and confirm it has the correct alt attribute. Run the page through an accessibility checker (such as axe or Lighthouse) and confirm no "missing alt" warnings.

WCAG reference. 1.1.1 Non-text Content.


2.9 Form Instructions Before Input

Requirement. Instructions that the user needs in order to complete a field must appear before the field, not only in an error message.

How the design meets it. The allowed file types and size limit are shown above the file input ("Allowed: jpg, png, pdf. Max 5 MB."), not only after a failed upload. The evidence label now includes a concrete example ("Proof of ownership (optional, e.g. serial number or photo)"), so the user knows what is expected before they attempt to upload. The character counter (25 / 2000) shows the limit before the user reaches it.

Verification. Visual inspection of each form field and its associated instructions. Confirm that no instruction appears only in an error message.

WCAG reference. 3.3.2 Labels or Instructions.


2.10 Timeout Warning

Requirement. If a session can expire due to inactivity, the user must be warned before it does.

How the design meets it. The session expiry modal appears one minute before the token expires, giving the user time to extend the session. The modal states the remaining time and offers a "Stay logged in" button. If the user does not respond, the modal redirects to the OIDC provider on expiry.

Verification. Manual test: log in, leave the page idle, and confirm the modal appears before expiry and that "Stay logged in" extends the session.

WCAG reference. 2.2.1 Timing Adjustable.


2.11 Page Titles

Requirement. Each page must have a unique, descriptive title.

How the design meets it. Every page sets its <title> to reflect the current task ("Report Found Item — Lost & Found", "Claim Review — Lost & Found", "Access Denied — Lost & Found"). The title changes on navigation, so a screen reader user knows when the page has changed.

Verification. Navigate through each page and confirm the title changes and is descriptive.

WCAG reference. 2.4.2 Page Titled.


2.12 Heading Hierarchy

Requirement. Headings must follow a logical hierarchy and be used to structure the content.

How the design meets it. Each page has exactly one <h1> (the page title). Sections within a page use <h2>, and subsections use <h3>. The heading hierarchy is not used for visual styling; it is used for structure, and the visual styling is applied through CSS. A screen reader user can navigate the page by headings.

Verification. Run the page through a heading structure checker. Confirm each page has exactly one <h1> and that the heading levels do not skip.

WCAG reference. 1.3.1 Info and Relationships, 2.4.6 Headings and Labels.


2.13 Language of Page

Requirement. The language of the page must be declared in the HTML.

How the design meets it. Every page sets <html lang="en">. If any content is in a different language, it is marked with a lang attribute on the element.

Verification. Inspect the HTML source of each page.

WCAG reference. 3.1.1 Language of Page.


2.14 Consistent Navigation

Requirement. Navigation must be consistent across pages.

How the design meets it. The top navigation (Logo, Home, Report, Search, My Claims, and Admin for administrators) appears on every authenticated page in the same position. The navigation order does not change between pages. The current page is indicated with aria-current="page".

Verification. Navigate through every page and confirm the navigation is in the same position and order.

WCAG reference. 3.2.3 Consistent Navigation.


2.15 Pause, Stop, Hide

Requirement. Any moving, blinking, or auto-updating content must be pausable, stoppable, or hideable.

How the design meets it. The system does not have any auto-updating content in the core workflows. The loading spinner is the only moving element, and it is transient (it disappears when the request completes). There are no carousels, no auto-playing videos, and no blinking notifications. The in-app notification bell updates its unread count when the user navigates, not on a timer.

Verification. Inspect each page for moving or auto-updating content and confirm that none exists in the core workflows. If a notification count is updated on a timer in a future version, it must be pausable.

WCAG reference. 2.2.2 Pause, Stop, Hide.


3. Checklist Log

The following table records the verification status of each item. Status values are: Verified (confirmed and recorded), Planned (design meets it, verification scheduled), Open (not yet addressed).

| Item                        | Status  | Verification Method             | Notes                                           |
| 2.1 Keyboard Access        | Planned  | Manual keyboard-only test       | Scheduled for implementation phase              |
| 2.2 Visible Focus          | Verified | Visual inspection of wireframes | Focus ring shown in every wireframe             |
| 2.3 Persistent Labels      | Verified | Screen reader test on prototype | Labels announced correctly                      |
| 2.4 Non-Colour Cues        | Verified | Grayscale rendering test        | Error, success, and required states distinguishable in grayscale |
| 2.5 Clear Errors           | Verified | Manual form test                | Inline errors in wireframes                     |
| 2.6 Readable Layout        | Verified | Browser resize test             | Reflows at 320, 768, 1024, 1440 px              |
| 2.7 Sufficient Contrast    | Verified | Contrast checker                | All text and UI components pass                 |
| 2.8 Alternative Text       | Verified | Icon inspection                 | All meaning-bearing icons have alt text         |
| 2.9 Form Instructions      | Verified | Visual inspection               | Instructions before input                       |
| 2.10 Timeout Warning       | Planned  | Manual idle test                | Scheduled for implementation phase              |    
| 2.11 Page Titles           | Verified | Page navigation test            | Titles descriptive and unique                   |
| 2.12 Heading Hierarchy     | Verified | Heading structure checker       | One h1 per page, no skipped levels              |
| 2.13 Language of Page      | Verified | HTML inspection                 | lang="en" on all pages                          |
| 2.14 Consistent Navigation | Verified | Navigation inspection           | Navigation in same position and order           |
| 2.15 Pause, Stop, Hide     | Verified | Content inspection              | No auto-updating content in core workflows      |


4. Open Items

The following items are not yet fully verified and are recorded as open. They will be closed during the implementation phase.

Open item 1: Screen reader test. The prototype has been checked with a screen reader by a team member, but not by a user who uses a screen reader as their primary interface. A future usability test should include at least one participant who uses a screen reader, to verify that the labels, error messages, and state changes are announced as expected. This was flagged in evidence/usability-test.md Section 7.

Open item 2: Keyboard-only test. The wireframes have been inspected for keyboard-accessibility, but the full keyboard-only test will be run against the implemented system, because the browser's focus behaviour cannot be fully validated on a static prototype. Scheduled for the implementation phase.

Open item 3: Timeout warning. The session expiry modal is designed but has not been tested against the real OIDC provider's expiry behaviour. The test is scheduled for the implementation phase.

Open item 4: Real screen sizes. The wireframes have been tested at four screen widths in the browser, but not on actual mobile and tablet hardware. A future round should include testing on a real tablet (the security desk uses a tablet during rounds) and on a phone (students may report a found item from a phone).


5. Related Artefacts

The user profiles and task scenarios are in docs/user-profile-and-tasks.md. The navigation model is in models/navigation.puml and models/navigation.svg. The wireframes are in prototype/wireframes.md. The UI states are in docs/ui-states.md. The usability test record is in evidence/usability-test.md. The API contract is in docs/api-contracts/core-operation.md. The failure and recovery model is in models/failure-recovery.md. The data integrity constraints are in docs/data-integrity.md.


6. Exit Record

Accessibility evidence summary. The wireframes satisfy the WCAG 2.1 AA requirements listed in Section 2 for the two core workflows (Report Found Item and Verify Claim). Thirteen of the fifteen items are verified against the wireframes; two are planned for verification against the implemented system. Four open items are recorded: a screen reader usability test, a keyboard-only test, a timeout test, and a real-device test.

Most important accessibility risk. The main risk is that the wireframes have been inspected but not yet tested with users who rely on assistive technology. The accessibility checklist is evidence that the design intent is accessible, but the design intent must be confirmed with real assistive technology users before the system is deployed. This is the reason for open item 1.