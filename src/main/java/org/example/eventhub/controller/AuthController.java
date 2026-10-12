package org.example.eventhub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventhub.common.ApiResponse;
import org.example.eventhub.common.BusinessException;
import org.example.eventhub.common.ErrorCode;
import org.example.eventhub.dto.LoginRequest;
import org.example.eventhub.service.StudentService;
import org.example.eventhub.vo.LoginResponse;
import org.springframework.web.bind.annotation.*;
import org.example.eventhub.common.UserContext;
import org.example.eventhub.vo.CurrentUserVO;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final StudentService studentService;

    /** 学生登录 */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(studentService.login(request));
    }

    /**
     * 获取当前登录用户信息
     * <p>
     * 需要带 Authorization: Bearer xxx，由 JwtInterceptor 校验并把 userId 放进 UserContext。
     */
    @GetMapping("/me")
    public ApiResponse<CurrentUserVO> me() {
        Long studentId = UserContext.getCurrentUserId();
        if (studentId == null) {
            // 理论上不会到这里，因为拦截器已经校验过了
            // 但作为兜底，还是判断一下
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "未登录");
        }
        return ApiResponse.success(studentService.getCurrentUser(studentId));
    }
}