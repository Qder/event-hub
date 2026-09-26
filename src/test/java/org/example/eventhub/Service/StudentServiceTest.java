package org.example.eventhub.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.eventhub.common.BusinessException;
import org.example.eventhub.domain.Student;
import org.example.eventhub.dto.StudentCreateRequest;
import org.example.eventhub.dto.StudentQueryRequest;
import org.example.eventhub.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional  // 测试完自动回滚，避免污染数据库
class StudentServiceTest {

    @Autowired
    private StudentService studentService;

    private StudentCreateRequest buildRequest(String no, String name) {
        StudentCreateRequest req = new StudentCreateRequest();
        req.setStudentNo(no);
        req.setName(name);
        req.setPhone("13800000000");
        return req;
    }

    @Test
    void createStudent_shouldGenerateId() {
        Long id = studentService.createStudent(buildRequest("T2026001", "张三"));
        assertThat(id).isNotNull();

        Student student = studentService.getById(id);
        assertThat(student.getStudentNo()).isEqualTo("T2026001");
        assertThat(student.getStatus()).isEqualTo(1);
        assertThat(student.getCreatedAt()).isNotNull(); // 验证自动填充
    }

    @Test
    void createStudent_duplicateStudentNo_shouldThrow() {
        studentService.createStudent(buildRequest("T2026002", "张三"));

        assertThatThrownBy(() -> studentService.createStudent(buildRequest("T2026002", "李四")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("学号已存在");
    }

    @Test
    void deleteStudent_shouldBeLogicDelete() {
        Long id = studentService.createStudent(buildRequest("T2026003", "王五"));
        studentService.deleteStudent(id);
        // 普通查询查不到（@TableLogic 自动加 deleted = 0）
        assertThat(studentService.getById(id)).isNull();
    }

    @Test
    void pageStudents_shouldFilterAndPage() {
        studentService.createStudent(buildRequest("T2026010", "赵六"));
        studentService.createStudent(buildRequest("T2026011", "钱七"));

        StudentQueryRequest query = new StudentQueryRequest();
        query.setName("赵");
        query.setPageNum(1);
        query.setPageSize(10);

        Page<Student> page = studentService.pageStudents(query);
        assertThat(page.getTotal()).isGreaterThanOrEqualTo(1);
        assertThat(page.getRecords()).allMatch(s -> s.getName().contains("赵"));
    }
}