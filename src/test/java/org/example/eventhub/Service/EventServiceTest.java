package org.example.eventhub.Service;

import org.example.eventhub.common.BusinessException;
import org.example.eventhub.domain.Event;
import org.example.eventhub.dto.EventCreateRequest;
import org.example.eventhub.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class EventServiceTest {

    @Autowired
    private EventService eventService;

    private EventCreateRequest buildRequest() {
        EventCreateRequest req = new EventCreateRequest();
        req.setTitle("Java 实战讲座");
        req.setCategory("讲座");
        req.setLocation("A101");
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        req.setCapacity(50);
        return req;
    }

    @Test
    void createEvent_shouldInitSlotsAndDraft() {
        Long id = eventService.createEvent(buildRequest());

        Event event = eventService.getById(id);
        assertThat(event.getAvailableSlots()).isEqualTo(50); // 名额已初始化
        assertThat(event.getCapacity()).isEqualTo(50);
        assertThat(event.getStatus()).isEqualTo("DRAFT");
        assertThat(event.getVersion()).isZero();
        assertThat(event.getCreatedAt()).isNotNull(); // 自动填充
    }

    @Test
    void createEvent_invalidTime_shouldThrow() {
        EventCreateRequest req = buildRequest();
        req.setStartTime(LocalDateTime.now().plusDays(2));
        req.setEndTime(LocalDateTime.now().plusDays(1)); // 结束早于开始

        assertThatThrownBy(() -> eventService.createEvent(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("开始时间必须早于结束时间");
    }

    @Test
    void publishEvent_shouldChangeStatus() {
        Long id = eventService.createEvent(buildRequest());
        eventService.publishEvent(id);

        Event event = eventService.getById(id);
        assertThat(event.getStatus()).isEqualTo("PUBLISHED");
        assertThat(event.getVersion()).isEqualTo(1); // 乐观锁版本 +1
    }

    @Test
    void optimisticLock_conflictOnTwoUpdates() {
        Long id = eventService.createEvent(buildRequest());

        // 模拟两个人同时拿到同一个版本号
        //
        // 坑：不能连续 getById 两次来"模拟两个人"。
        // 同一个事务里 MyBatis 一级缓存生效，两次 getById 只发一条 SELECT，
        // 并且返回【同一个对象引用】。第一次 updateById 成功后
        // MyBatis-Plus 会把该对象的 version 改成 1，于是 e2.version 也变成 1，
        // 第二次更新就不会冲突了 —— 测出来的结果是假的。
        //
        // 正确做法：给"第二个人"一份独立的旧版本快照。
        Event e1 = eventService.getById(id); // version = 0

        Event e2 = new Event();              // 独立的快照，模拟另一个人读到的旧数据
        e2.setId(id);
        e2.setVersion(0);
        e2.setTitle("新标题-2");

        e1.setTitle("新标题-1");
        assertThat(eventService.updateById(e1)).isTrue();  // 成功，库里 version -> 1

        // 此时数据库里 version=1，而 e2 仍然是 version=0，
        // SQL 变成 ... WHERE id=? AND version=0  -> 匹配 0 行 -> 返回 false
        assertThat(eventService.updateById(e2)).isFalse();
    }

    @Test
    void deleteEvent_shouldBeLogicDelete() {
        Long id = eventService.createEvent(buildRequest());
        eventService.deleteEvent(id);

        assertThat(eventService.getById(id)).isNull();
    }
}