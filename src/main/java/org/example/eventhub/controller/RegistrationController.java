package org.example.eventhub.controller;

import lombok.RequiredArgsConstructor;
import org.example.eventhub.common.ApiResponse;
import org.example.eventhub.common.BusinessException;
import org.example.eventhub.common.ErrorCode;
import org.example.eventhub.common.UserContext;
import org.example.eventhub.service.RegistrationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    /** 学生报名活动：studentId 从 token 取 */
    @PostMapping("/events/{eventId}/registrations")
    public ApiResponse<Long> register(@PathVariable Long eventId) {
        Long studentId = UserContext.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "未登录");
        }
        return ApiResponse.success(registrationService.register(eventId, studentId));
    }

    /** 取消报名：只能取消自己的 */
    @PostMapping("/registrations/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        Long studentId = UserContext.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "未登录");
        }
        registrationService.cancel(id, studentId);   // 多传一个当前用户 id
        return ApiResponse.success();
    }
}