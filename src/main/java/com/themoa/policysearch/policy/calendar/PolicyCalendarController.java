package com.themoa.policysearch.policy.calendar;

import com.themoa.policysearch.common.response.ApiResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
public class PolicyCalendarController {
    private final PolicyCalendarService calendarService;

    public PolicyCalendarController(PolicyCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public ApiResponse<List<PolicyCalendarEventResponse>> events(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.ok(calendarService.events(from, to));
    }
}
