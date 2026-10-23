

package com.team12.lostandfound.interfaces;

/**
 * Port for the external email sender. The Notification module depends on
 * this interface, not on the SMTP client library. If the SMTP server is
 * unavailable, the Notification module logs the failure and retries on a
 * schedule; core workflows continue.
 *
 * See docs/framework-decision.md Section 2.5 and models/failure-recovery.md.
 */
public interface EmailSender {

    void send(String to, String subject, String body);
}