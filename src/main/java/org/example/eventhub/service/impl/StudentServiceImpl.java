package org.example.eventhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.eventhub.common.BusinessException;
import org.example.eventhub.common.ErrorCode;
import org.example.eventhub.domain.Student;
import org.example.eventhub.dto.LoginRequest;
import org.example.eventhub.dto.StudentCreateRequest;
import org.example.eventhub.dto.StudentQueryRequest;
import org.example.eventhub.dto.StudentUpdateRequest;
import org.example.eventhub.mapper.StudentMapper;
import org.example.eventhub.service.StudentService;
import org.example.eventhub.util.JwtUtil;
import org.example.eventhub.vo.LoginResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    private final PasswordEncoder passwordEncoder;
    /** 允许排序的字段白名单 */
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "studentNo", "name");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStudent(StudentCreateRequest request) {
        // 1. 检查学号是否已存在
        long count = this.lambdaQuery()
                .eq(Student::getStudentNo, request.getStudentNo())
                .count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.STUDENT_NO_DUPLICATED);
        }

        // 2. 组装实体
        Student student = new Student();
        student.setStudentNo(request.getStudentNo());
        student.setName(request.getName());
        student.setPhone(request.getPhone());
        student.setStatus(1);
        student.setPassword(passwordEncoder.encode("123456"));//默认密码123456
        // createdAt / updatedAt 由 MyMetaObjectHandler 自动填充
        // deleted 由 MyBatis-Plus 逻辑删除默认填充 0

        // 3. 保存
        boolean saved = this.save(student);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "保存学生失败");
        }
        return student.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateStudent(Long id, StudentUpdateRequest request) {
        // 1. 检查学生是否存在
        Student existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }

        // 2. 只更新允许的字段，绝不让前端直接覆盖 id / studentNo / createdAt
        Student update = new Student();
        update.setId(id);
        update.setName(request.getName());
        update.setPhone(request.getPhone());
        update.setStatus(request.getStatus());
        // updatedAt 由 MyMetaObjectHandler 自动填充

        boolean updated = this.updateById(update);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "更新学生失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStudent(Long id) {
        Student existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }
        // 逻辑删除：@TableLogic 会自动把 SQL 从 DELETE 改成 UPDATE deleted=1
        this.removeById(id);
    }

    @Override
    public Student getStudentById(Long id) {
        Student student = this.getById(id);
        if (student == null) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }
        return student;
    }

    @Override
    public Page<Student> pageStudents(StudentQueryRequest request) {
        // 1. 校验页码和页大小
        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        if (pageSize > 100) {
            pageSize = 100; // 兜底，防止恶意传大 pageSize
        }

        // 2. 动态条件：LambdaQueryWrapper 单表动态筛选
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(request.getStudentNo()),
                Student::getStudentNo, request.getStudentNo());
        wrapper.like(StringUtils.hasText(request.getName()),
                Student::getName, request.getName());
        wrapper.eq(request.getStatus() != null,
                Student::getStatus, request.getStatus());

        // 3. 排序：白名单校验，防 SQL 注入
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        if (StringUtils.hasText(sortField) && ALLOWED_SORT_FIELDS.contains(sortField)) {
            boolean asc = "asc".equalsIgnoreCase(sortOrder);
            switch (sortField) {
                case "studentNo" -> wrapper.orderBy(true, asc, Student::getStudentNo);
                case "name" -> wrapper.orderBy(true, asc, Student::getName);
                case "createdAt" -> wrapper.orderBy(true, asc, Student::getCreatedAt);
                default -> wrapper.orderByDesc(Student::getCreatedAt);
            }
        } else {
            // 默认按创建时间倒序
            wrapper.orderByDesc(Student::getCreatedAt);
        }

        // 4. 分页
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 按学号查学生
        Student student = this.lambdaQuery()
                .eq(Student::getStudentNo, request.getStudentNo())
                .one();

        // 2. 学号不存在 → 统一错误码
        if (student == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 3. 密码校验
        if (student.getPassword() == null
                || !passwordEncoder.matches(request.getPassword(), student.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 4. 学生被停用
        if (student.getStatus() == 0 || student.getStatus() != 1) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 5. 生成 token
        String token = jwtUtil.generateToken(student.getId(), student.getStudentNo());

        return new LoginResponse(token, student.getId(), student.getStudentNo(), student.getName());
    }
}