package com.themoa.policysearch.policy.calendar;

import com.themoa.policysearch.policy.domain.PolicyCalendarEvent;
import java.time.LocalDate;

public record PolicyCalendarEventResponse(Integer eventId, Integer policyId, String title, String eventType, LocalDate eventDate) {
    static PolicyCalendarEventResponse from(PolicyCalendarEvent event) {
        return new PolicyCalendarEventResponse(event.getId(), event.getPolicy().getId(), event.getTitle(), event.getEventType(), event.getEventDate());
    }
}
