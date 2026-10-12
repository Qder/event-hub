package org.example.eventhub.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.eventhub.domain.Student;
import org.example.eventhub.dto.LoginRequest;
import org.example.eventhub.dto.StudentCreateRequest;
import org.example.eventhub.dto.StudentQueryRequest;
import org.example.eventhub.dto.StudentUpdateRequest;
import org.example.eventhub.vo.CurrentUserVO;
import org.example.eventhub.vo.LoginResponse;

public interface StudentService extends IService<Student> {
    /**
     * 创建学生
     * @param studentCreateRequest 学生创建请求
     * @return 学生ID
     */
    Long createStudent(StudentCreateRequest studentCreateRequest);

    /** 更新学生姓名、电话和状态。 */
    void updateStudent(Long id, StudentUpdateRequest request);

    /*
     * 逻辑删除学生
     * @param studentId 学生ID
     */
    void deleteStudent(Long studentId);

    /*
     * 根据id查询学生
     * @param studentId 学生ID
     */
    Student getStudentById(Long studentId);

    /**
     * 分页查询
     *
     * @return
     */
    Page<Student> pageStudents(StudentQueryRequest request);

    /**
     * 学生登录
     * <p>
     * 校验学号和密码，成功返回 JWT token。
     * 学号不存在、密码错误、学生被停用都返回同一个错误码，防止信息泄露。
     *
     * @param request 登录请求
     * @return 登录响应（含 token）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 获取当前登录用户信息
     *
     * @param studentId 当前用户 ID（从 ThreadLocal 取）
     * @return 当前用户信息
     */
    CurrentUserVO getCurrentUser(Long studentId);
}
