package org.example.eventhub.service;

import org.example.eventhub.common.BusinessException;
import org.example.eventhub.common.ErrorCode;
import org.example.eventhub.dto.LoginRequest;
import org.example.eventhub.dto.StudentCreateRequest;
import org.example.eventhub.util.JwtUtil;
import org.example.eventhub.vo.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AuthTest {

    @Autowired private StudentService studentService;
    @Autowired private JwtUtil jwtUtil;

    private String createStudent() {
        String no = "AUTH_" + UUID.randomUUID().toString().substring(0, 8);
        StudentCreateRequest req = new StudentCreateRequest();
        req.setStudentNo(no);
        req.setName("登录测试");
        req.setPhone("13800000000");
        studentService.createStudent(req);
        return no;
    }

    @Test
    void login_success_shouldReturnToken() {
        String no = createStudent();

        LoginRequest req = new LoginRequest();
        req.setStudentNo(no);
        req.setPassword("123456"); // createStudent 里的默认密码

        LoginResponse resp = studentService.login(req);

        assertThat(resp.getToken()).isNotBlank();
        assertThat(resp.getStudentNo()).isEqualTo(no);
        // token 能被解析且 studentId 正确
        assertThat(jwtUtil.getStudentId(resp.getToken())).isEqualTo(resp.getStudentId());
    }

    @Test
    void login_wrongPassword_shouldThrow() {
        String no = createStudent();

        LoginRequest req = new LoginRequest();
        req.setStudentNo(no);
        req.setPassword("wrong");

        assertThatThrownBy(() -> studentService.login(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.LOGIN_FAILED.getCode());
    }

    @Test
    void login_nonexistentStudent_shouldThrowSameError() {
        LoginRequest req = new LoginRequest();
        req.setStudentNo("NOT_EXIST_999");
        req.setPassword("123456");

        // 学号不存在和密码错误返回同一个错误码
        assertThatThrownBy(() -> studentService.login(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.LOGIN_FAILED.getCode());
    }

    @Test
    void login_disabledStudent_shouldThrow() {
        String no = createStudent();

        // 停用学生
        var updateReq = new org.example.eventhub.dto.StudentUpdateRequest();
        updateReq.setName("登录测试");
        updateReq.setPhone("13800000000");
        updateReq.setStatus(0);
        Long id = studentService.lambdaQuery().eq(org.example.eventhub.domain.Student::getStudentNo, no).one().getId();
        studentService.updateStudent(id, updateReq);

        LoginRequest req = new LoginRequest();
        req.setStudentNo(no);
        req.setPassword("123456");

        assertThatThrownBy(() -> studentService.login(req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.LOGIN_FAILED.getCode());
    }
}