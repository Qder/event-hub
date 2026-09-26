package org.example.eventhub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventhub.common.ApiResponse;
import org.example.eventhub.dto.RegistrationRequest;
import org.example.eventhub.service.RegistrationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    /** 学生报名活动 */
    @PostMapping("/events/{eventId}/registrations")
    public ApiResponse<Long> register(@PathVariable Long eventId,
                                      @Valid @RequestBody RegistrationRequest request) {
        return ApiResponse.success(registrationService.register(eventId, request.getStudentId()));
    }

    /** 取消报名 */
    @PostMapping("/registrations/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        registrationService.cancel(id);
        return ApiResponse.success();
    }
}