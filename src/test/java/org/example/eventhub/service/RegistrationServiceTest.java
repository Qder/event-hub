package org.example.eventhub.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.eventhub.common.BusinessException;
import org.example.eventhub.domain.Event;
import org.example.eventhub.domain.Registration;
import org.example.eventhub.dto.EventCreateRequest;
import org.example.eventhub.dto.RegistrationQueryRequest;
import org.example.eventhub.dto.StudentCreateRequest;
import org.example.eventhub.mapper.EventMapper;
import org.example.eventhub.vo.EventStatisticsVO;
import org.example.eventhub.vo.RegistrationDetailVO;
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
    @Autowired private EventMapper eventMapper;

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

        registrationService.cancel(regId, studentId);  // ← 加第二个参数

        // 后续断言不变
    }

    @Test
    void cancel_twice_onlyRestoreOnce() {
        Long eventId = createPublishedEvent(10);
        Long studentId = createStudent();
        Long regId = registrationService.register(eventId, studentId);

        registrationService.cancel(regId,studentId);

        assertThatThrownBy(() -> registrationService.cancel(regId,studentId))
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

    @Test
    void checkIn_success() {
        // 1. 创建活动：开始时间是 5 分钟前（已进入签到窗口）
        EventCreateRequest req = new EventCreateRequest();
        req.setTitle("签到测试");
        req.setCategory("讲座");
        req.setLocation("A101");
        req.setStartTime(LocalDateTime.now().minusMinutes(5));
        req.setEndTime(LocalDateTime.now().plusHours(1));
        req.setCapacity(10);
        Long eventId = eventService.createEvent(req);
        eventService.publishEvent(eventId);

        // 2. 直接造学生
        Long studentId = createStudent();

        // 3. 直接写报名记录，绕过 register 的"活动未开始"检查
        Registration registration = new Registration();
        registration.setEventId(eventId);
        registration.setStudentId(studentId);
        registration.setStatus("REGISTERED");
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setVersion(0);
        registrationService.save(registration);
        Long regId = registration.getId();

        // 4. 手动扣一个名额，保持数据一致
        eventMapper.decreaseAvailableSlots(eventId);

        // 5. 签到
        registrationService.checkIn(regId);

        Registration reg = registrationService.getById(regId);
        assertThat(reg.getStatus()).isEqualTo("CHECKED_IN");
        assertThat(reg.getCheckedInAt()).isNotNull();
    }

    @Test
    void checkIn_duplicated_shouldThrow() {
        // 同上：直接构造"已报名 + 活动已开始"的数据
        EventCreateRequest req = new EventCreateRequest();
        req.setTitle("签到测试");
        req.setCategory("讲座");
        req.setLocation("A101");
        req.setStartTime(LocalDateTime.now().minusMinutes(5));
        req.setEndTime(LocalDateTime.now().plusHours(1));
        req.setCapacity(10);
        Long eventId = eventService.createEvent(req);
        eventService.publishEvent(eventId);

        Long studentId = createStudent();

        Registration registration = new Registration();
        registration.setEventId(eventId);
        registration.setStudentId(studentId);
        registration.setStatus("REGISTERED");
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setVersion(0);
        registrationService.save(registration);
        Long regId = registration.getId();

        eventMapper.decreaseAvailableSlots(eventId);

        registrationService.checkIn(regId);

        // 第二次签到应该失败
        assertThatThrownBy(() -> registrationService.checkIn(regId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("签到");
    }

    @Test
    void pageRegistrationDetails_shouldFilterByKeyword() {
        Long eventId = createPublishedEvent(10);
        Long s1 = createStudent();
        Long s2 = createStudent();
        registrationService.register(eventId, s1);
        registrationService.register(eventId, s2);

        RegistrationQueryRequest req = new RegistrationQueryRequest();
        req.setKeyword("测试"); // 两个学生都叫"测试学生"
        req.setPageNum(1);
        req.setPageSize(10);

        IPage<RegistrationDetailVO> page =
                registrationService.pageRegistrationDetails(eventId, req);

        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getRecords()).allMatch(vo -> vo.getEventTitle() != null);
        assertThat(page.getRecords()).allMatch(vo -> vo.getStudentName() != null);
    }

    @Test
    void pageRegistrationDetails_cancelledExcludedByStatus() {
        Long eventId = createPublishedEvent(10);
        Long s1 = createStudent();
        Long s2 = createStudent();
        Long r1 = registrationService.register(eventId, s1);
        registrationService.register(eventId, s2);
        registrationService.cancel(r1, s1);   // ← 加 s1

        RegistrationQueryRequest req = new RegistrationQueryRequest();
        req.setStatus("REGISTERED");

        IPage<RegistrationDetailVO> page =
                registrationService.pageRegistrationDetails(eventId, req);

        assertThat(page.getTotal()).isEqualTo(1);
    }

    @Test
    void getEventStatistics_shouldAggregate() {
        Long eventId = createPublishedEvent(10);
        Long s1 = createStudent();
        Long s2 = createStudent();
        Long s3 = createStudent();
        Long r1 = registrationService.register(eventId, s1);
        registrationService.register(eventId, s2);
        registrationService.register(eventId, s3);
        registrationService.cancel(r1, s1);   // ← 加 s1

        EventStatisticsVO vo = registrationService.getEventStatistics(eventId);

        assertThat(vo.getTotalRegistrationCount()).isEqualTo(3);
        assertThat(vo.getActiveRegistrationCount()).isEqualTo(2);
        assertThat(vo.getCancelledCount()).isEqualTo(1);
        assertThat(vo.getCheckedInCount()).isZero();
        assertThat(vo.getCheckInRate()).isZero();
    }

    @Test
    void cancel_otherStudentRegistration_shouldThrow() {
        Long eventId = createPublishedEvent(10);
        Long student1 = createStudent();
        Long student2 = createStudent();

        Long regId = registrationService.register(eventId, student1);

        // student2 尝试取消 student1 的报名
        assertThatThrownBy(() -> registrationService.cancel(regId, student2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只能取消自己的报名");

        // 报名记录没变
        Registration reg = registrationService.getById(regId);
        assertThat(reg.getStatus()).isEqualTo("REGISTERED");
    }
}