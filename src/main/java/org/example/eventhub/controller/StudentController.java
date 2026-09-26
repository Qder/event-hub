package org.example.eventhub.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.eventhub.common.ApiResponse;
import org.example.eventhub.domain.Student;
import org.example.eventhub.dto.StudentCreateRequest;
import org.example.eventhub.dto.StudentQueryRequest;
import org.example.eventhub.service.RegistrationService;
import org.example.eventhub.service.StudentService;
import org.example.eventhub.vo.StudentRegistrationVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor


public class StudentController {

    private final StudentService studentService;
    private final RegistrationService registrationService;

    //创建学生
    @PostMapping
    public ApiResponse<Long> createStudent(@Valid @RequestBody StudentCreateRequest request) {
        //调用service层的创建学生方法
        Long studentId = studentService.createStudent(request);
        return ApiResponse.success(studentId);
    }

    //查询学生
    @GetMapping("{id}")
    public ApiResponse<Student> getStudentById(@PathVariable Long id) {
        //调用service层的查询学生方法
        Student student = studentService.getStudentById(id);
        return ApiResponse.success(student);
    }

    //更新学生
    @PutMapping("{id}")
    public ApiResponse<Void> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentCreateRequest request) {
        //调用service层的更新学生方法
        studentService.updateStudent(id, request);
        return ApiResponse.success();
    }

    //逻辑删除学生
    @DeleteMapping("{id}")
    public ApiResponse<Void> deleteStudent(@PathVariable Long id) {
        //调用service层的逻辑删除学生方法
        studentService.deleteStudent(id);
        return ApiResponse.success();
    }

    //分页查询学生
    @GetMapping
    public ApiResponse<Page<Student>> pageStudents(@Valid StudentQueryRequest request) {
        //调用service层的分页查询学生方法
        return ApiResponse.success(studentService.pageStudents(request));
    }

    @GetMapping("/{id}/registrations")
    public ApiResponse<List<StudentRegistrationVO>> registrations(@PathVariable Long id) {
        return ApiResponse.success(registrationService.getStudentRegistrationHistory(id));
    }
}
