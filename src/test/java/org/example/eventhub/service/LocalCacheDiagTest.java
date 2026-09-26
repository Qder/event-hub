package org.example.eventhub.service;

import org.example.eventhub.domain.Event;
import org.example.eventhub.dto.EventCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 诊断：MyBatis 一级缓存在同一个事务内返回的是不是同一个对象引用
 */
@SpringBootTest
@Transactional
class LocalCacheDiagTest {

    @Autowired
    private EventService eventService;

    @Test
    void diag_twoGetById() {
        EventCreateRequest req = new EventCreateRequest();
        req.setTitle("diag");
        req.setCategory("讲座");
        req.setLocation("A101");
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        req.setCapacity(10);
        Long id = eventService.createEvent(req);

        Event a = eventService.getById(id);
        Event b = eventService.getById(id);

        System.out.println("### a == b ?  " + (a == b));
        System.out.println("### a.hashCode = " + System.identityHashCode(a));
        System.out.println("### b.hashCode = " + System.identityHashCode(b));
        System.out.println("### a.version = " + a.getVersion() + " , b.version = " + b.getVersion());

        // 改 a，看 b 是否跟着变
        a.setTitle("被 a 改掉了");
        System.out.println("### 改 a 之后 b.title = " + b.getTitle());
        System.out.println("### 这证明两个变量" + (a == b ? "是同一个对象" : "是两个不同对象"));
    }
}
