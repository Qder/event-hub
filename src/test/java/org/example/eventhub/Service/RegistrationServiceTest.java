package org.example.eventhub.Service;

import org.example.eventhub.common.BusinessException;
import org.example.eventhub.domain.Event;
import org.example.eventhub.domain.Registration;
import org.example.eventhub.dto.EventCreateRequest;
import org.example.eventhub.dto.StudentCreateRequest;
import org.example.eventhub.service.EventService;
import org.example.eventhub.service.RegistrationService;
import org.example.eventhub.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class RegistrationServiceTest {

    @Autowired private RegistrationService registrationService;
    @Autowired private StudentService studentService;
    @Autowired private EventService eventService;

    private Long createStudent() {
        StudentCreateRequest req = new StudentCreateRequest();
        req.setStudentNo("T" + UUID.randomUUID().toString().substring(0, 8));
        req.setName("测试学生");
        req.setPhone("13800000000");
        return studentService.createStudent(req);
    }

    private Long createPublishedEvent(int capacity) {
        EventCreateRequest req = new EventCreateRequest();
        req.setTitle("测试活动");
        req.setCategory("讲座");
        req.setLocation("A101");
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        req.setCapacity(capacity);
        Long id = eventService.createEvent(req);
        eventService.publishEvent(id);
        return id;
    }

    @Test
    void register_success() {
        Long eventId = createPublishedEvent(10);
        Long studentId = createStudent();

        Long regId = registrationService.register(eventId, studentId);

        assertThat(regId).isNotNull();
        Registration reg = registrationService.getById(regId);
        assertThat(reg.getStatus()).isEqualTo("REGISTERED");

        Event event = eventService.getById(eventId);
        assertThat(event.getAvailableSlots()).isEqualTo(9); // 名额减一
    }

    @Test
    void register_eventFull_shouldThrow() {
        Long eventId = createPublishedEvent(1);
        Long student1 = createStudent();
        Long student2 = createStudent();

        registrationService.register(eventId, student1);

        assertThatThrownBy(() -> registrationService.register(eventId, student2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("名额已满");

        Event event = eventService.getById(eventId);
        assertThat(event.getAvailableSlots()).isZero();
    }

    @Test
    void register_duplicate_shouldThrow() {
        Long eventId = createPublishedEvent(10);
        Long studentId = createStudent();

        registrationService.register(eventId, studentId);

        assertThatThrownBy(() -> registrationService.register(eventId, studentId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("重复报名");

        // 名额只扣了一次
        Event event = eventService.getById(eventId);
        assertThat(event.getAvailableSlots()).isEqualTo(9);
    }

    @Test
    void register_disabledStudent_shouldThrow() {
        Long eventId = createPublishedEvent(10);
        Long studentId = createStudent();

        // 停用学生
        studentService.updateStudent(studentId, buildUpdateRequest(0));

        assertThatThrownBy(() -> registrationService.register(eventId, studentId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("停用");

        // 名额没被扣
        Event event = eventService.getById(eventId);
        assertThat(event.getAvailableSlots()).isEqualTo(10);
    }

    @Test
    void register_eventNotPublished_shouldThrow() {
        // 创建但不发布
        EventCreateRequest req = new EventCreateRequest();
        req.setTitle("未发布活动");
        req.setCategory("讲座");
        req.setLocation("A101");
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        req.setCapacity(10);
        Long eventId = eventService.createEvent(req);

        Long studentId = createStudent();
        assertThatThrownBy(() -> registrationService.register(eventId, studentId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未发布");
    }

    @Test
    void cancel_success() {
        Long eventId = createPublishedEvent(10);
        Long studentId = createStudent();
        Long regId = registrationService.register(eventId, studentId);

        registrationService.cancel(regId);

        Registration reg = registrationService.getById(regId);
        assertThat(reg.getStatus()).isEqualTo("CANCELLED");
        assertThat(reg.getCancelledAt()).isNotNull();

        Event event = eventService.getById(eventId);
        assertThat(event.getAvailableSlots()).isEqualTo(10); // 名额归还
    }

    @Test
    void cancel_twice_onlyRestoreOnce() {
        Long eventId = createPublishedEvent(10);
        Long studentId = createStudent();
        Long regId = registrationService.register(eventId, studentId);

        registrationService.cancel(regId);

        assertThatThrownBy(() -> registrationService.cancel(regId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只有 REGISTERED");

        // 名额只归还一次
        Event event = eventService.getById(eventId);
        assertThat(event.getAvailableSlots()).isEqualTo(10);
    }

    private org.example.eventhub.dto.StudentUpdateRequest buildUpdateRequest(int status) {
        var req = new org.example.eventhub.dto.StudentUpdateRequest();
        req.setName("测试学生");
        req.setPhone("13800000000");
        req.setStatus(status);
        return req;
    }
}