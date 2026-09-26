package org.example.eventhub.mapper;

import org.example.eventhub.domain.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class StudentMapperTest {
    @Autowired
    private StudentMapper mapper;


    @Test
    public void testSelect() {
        Student student = new Student();
// student.setId(...); 不要设置 id
        student.setName("John Doe");
        student.setStudentNo("S12345");
        student.setPhone("1234567890");
        student.setStatus(1);
        mapper.insert(student);
        System.out.println("新生成的主键 ID: " + student.getId());
    }
}
