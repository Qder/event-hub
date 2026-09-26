package org.example.eventhub.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.eventhub.domain.Student;
import org.example.eventhub.dto.StudentCreateRequest;
import org.example.eventhub.dto.StudentQueryRequest;
import org.example.eventhub.dto.StudentUpdateRequest;
import org.springframework.transaction.annotation.Transactional;

public interface StudentService extends IService<Student> {
    /**
     * 创建学生
     * @param studentCreateRequest 学生创建请求
     * @return 学生ID
     */
    Long createStudent(StudentCreateRequest studentCreateRequest);

    /*
    * 更新学生
    * @param studentId 学生ID
    * @param studentCreateRequest 学生创建请求
    */

    void updateStudent(Long studentId, StudentCreateRequest studentCreateRequest);

    @Transactional(rollbackFor = Exception.class)
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
}
