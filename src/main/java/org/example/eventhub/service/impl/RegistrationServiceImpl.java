package org.example.eventhub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.eventhub.common.BusinessException;
import org.example.eventhub.common.ErrorCode;
import org.example.eventhub.domain.Event;
import org.example.eventhub.domain.Registration;
import org.example.eventhub.domain.Student;
import org.example.eventhub.mapper.EventMapper;
import org.example.eventhub.mapper.RegistrationMapper;
import org.example.eventhub.mapper.StudentMapper;
import org.example.eventhub.service.RegistrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl
        extends ServiceImpl<RegistrationMapper, Registration>
        implements RegistrationService {

    private static final String STATUS_REGISTERED = "REGISTERED";
    private static final String EVENT_PUBLISHED = "PUBLISHED";
    private static final String EVENT_CANCELLED = "CANCELLED";

    private final StudentMapper studentMapper;
    private final EventMapper eventMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(Long eventId, Long studentId) {
        // 1. 检查学生
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }
        if (student.getStatus() != 1) {
            throw new BusinessException(ErrorCode.STUDENT_DISABLED);
        }

        // 2. 检查活动
        Event event = eventMapper.selectById(eventId);
        if (event == null) {
            throw new BusinessException(ErrorCode.EVENT_NOT_FOUND);
        }
        if (EVENT_CANCELLED.equals(event.getStatus())) {
            throw new BusinessException(ErrorCode.EVENT_CANCELLED);
        }
        if (!EVENT_PUBLISHED.equals(event.getStatus())) {
            throw new BusinessException(ErrorCode.EVENT_NOT_PUBLISHED);
        }
        if (event.getStartTime() != null && event.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.EVENT_ALREADY_STARTED);
        }

        // 3. 检查重复报名（包含 CANCELLED，因为唯一约束 event_id + student_id）
        long count = this.lambdaQuery()
                .eq(Registration::getEventId, eventId)
                .eq(Registration::getStudentId, studentId)
                .count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.REGISTRATION_DUPLICATED);
        }

        // 4. 原子扣减名额（核心不变量：available_slots > 0）
        int affected = eventMapper.decreaseAvailableSlots(eventId);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.EVENT_FULL);
        }

        // 5. 插入报名记录
        Registration registration = new Registration();
        registration.setEventId(eventId);
        registration.setStudentId(studentId);
        registration.setStatus(STATUS_REGISTERED);
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setVersion(0);

        boolean saved = this.save(registration);
        if (!saved) {
            // 故意抛出，验证事务回滚：名额扣减应被回滚
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "保存报名记录失败");
        }
        return registration.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long registrationId) {
        // 1. 检查报名记录是否存在
        Registration registration = this.getById(registrationId);
        if (registration == null) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND);
        }

        // 2. 原子取消：只有 REGISTERED 才能取消，防止重复取消
        int affected = baseMapper.cancelRegistration(registrationId);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.REGISTRATION_STATUS_ILLEGAL,
                    "只有 REGISTERED 状态可以取消");
        }

        // 3. 归还名额（带 available_slots < capacity 条件，防止超额归还）
        int slotsRestored = eventMapper.increaseAvailableSlots(registration.getEventId());
        if (slotsRestored == 0) {
            // 归还失败通常意味着数据不一致，抛异常让事务回滚
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "归还名额失败");
        }
    }
}