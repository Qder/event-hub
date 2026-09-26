package org.example.eventhub.mapper;

import org.example.eventhub.domain.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mapper（DAO）层测试。
 * <p>
 * 两个必须注意的点：
 * 1. 加 @Transactional：每个测试方法结束自动回滚，否则数据真的落库，
 *    第二次跑就会撞 student_no 的唯一索引（DuplicateKey）。
 * 2. 不要手工 set id：Student.id 是 IdType.AUTO，交给数据库自增，
 *    插入后 MyBatis 会把生成的主键回填到实体上。
 */
@SpringBootTest
@Transactional
class StudentMapperTest {

    @Autowired
    private StudentMapper mapper;

    @Test
    void testInsertGeneratesId() {
        Student student = new Student();
        // 不要设置 id：IdType.AUTO 让数据库自增
        student.setName("John Doe");
        // 用随机学号，即使库里有历史数据也不会撞唯一索引
        student.setStudentNo("T" + UUID.randomUUID().toString().substring(0, 8));
        student.setPhone("1234567890");
        student.setStatus(1);

        int rows = mapper.insert(student);
        System.out.println("新生成的主键 ID: " + student.getId());

        assertThat(rows).isEqualTo(1);
        assertThat(student.getId()).isNotNull();  // 断言：自增主键被回填

        // 再查一次，确认真的落库了
        Student loaded = mapper.selectById(student.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getStudentNo()).isEqualTo(student.getStudentNo());
    }
}
