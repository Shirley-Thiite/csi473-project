

package com.team12.lostandfound.interfaces;

import com.team12.lostandfound.domain.Notification;
import java.util.List;

/**
 * Persistence port for the notification log. The log is used for retrying
 * failed sends; the notification content itself is not part of the audit trail.
 */
public interface NotificationRepository {

    void log(Notification notification);

    List<Notification> findPending();
}