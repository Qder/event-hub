package org.example.eventhub.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.eventhub.common.BusinessException;
import org.example.eventhub.common.ErrorCode;
import org.example.eventhub.domain.Event;
import org.example.eventhub.domain.Registration;
import org.example.eventhub.domain.Student;
import org.example.eventhub.dto.RegistrationQueryRequest;
import org.example.eventhub.mapper.EventMapper;
import org.example.eventhub.mapper.RegistrationMapper;
import org.example.eventhub.mapper.StudentMapper;
import org.example.eventhub.service.RegistrationService;
import org.example.eventhub.vo.EventStatisticsVO;
import org.example.eventhub.vo.RegistrationDetailVO;
import org.example.eventhub.vo.StudentRegistrationVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    public void cancel(Long registrationId, Long currentUserId) {
        // 1. 检查报名记录
        Registration registration = this.getById(registrationId);
        if (registration == null) {
            throw new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND);
        }

        // ★ 新增：只能取消自己的报名
        if (!registration.getStudentId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "只能取消自己的报名");
        }

        // 2. 原子取消
        int affected = baseMapper.cancelRegistration(registrationId);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.REGISTRATION_STATUS_ILLEGAL,
                    "只有 REGISTERED 状态可以取消");
        }

        // 3. 归还名额
        int slotsRestored = eventMapper.increaseAvailableSlots(registration.getEventId());
        if (slotsRestored == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "归还名额失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void checkIn(Long registrationId) {
        //检查报名记录
        Registration registration = this.getById(registrationId);
        if (registration == null) {

            throw new BusinessException(ErrorCode.REGISTRATION_NOT_FOUND);
        }

        //检查状态是否是可以报名状态
        if(!STATUS_REGISTERED.equals(registration.getStatus()))
        {
            throw new BusinessException(ErrorCode.REGISTRATION_STATUS_ILLEGAL, "只有 REGISTERED 状态可以签到");
        }

        //检查时间是否符合开始前30分钟，后分钟
        Event event = eventMapper.selectById(registration.getEventId());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = event.getStartTime().minusMinutes(30);
        LocalDateTime windowEnd = event.getStartTime().plusMinutes(60);
        if (now.isBefore(windowStart) || now.isAfter(windowEnd)) {
            throw new BusinessException(ErrorCode.CHECK_IN_TIME_INVALID);
        }

        if (event == null) {
            throw new BusinessException(ErrorCode.EVENT_NOT_FOUND);
        }

        //原子更新签到状态
        int affected = baseMapper.checkInRegistration(registrationId);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "签到失败，可能已被其他人签到");
        }
    }

    @Override
    public IPage<RegistrationDetailVO> pageRegistrationDetails(Long eventId, RegistrationQueryRequest request) {
        // 先确认活动存在
        if (eventMapper.selectById(eventId) == null) {
            throw new BusinessException(ErrorCode.EVENT_NOT_FOUND);
        }
        Page<RegistrationDetailVO> page = new Page<>(
                request.getPageNum() == null ? 1 : request.getPageNum(),
                request.getPageSize() == null ? 10 : Math.min(request.getPageSize(), 100));
        return baseMapper.selectRegistrationDetails(page, eventId, request);
    }

    @Override
    public List<StudentRegistrationVO> getStudentRegistrationHistory(Long studentId) {
        if (studentMapper.selectById(studentId) == null) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }
        return baseMapper.selectStudentRegistrationHistory(studentId);
    }

    @Override
    public EventStatisticsVO getEventStatistics(Long eventId) {
        EventStatisticsVO vo = baseMapper.selectEventStatistics(eventId);
        if (vo == null) {
            throw new BusinessException(ErrorCode.EVENT_NOT_FOUND);
        }
        return vo;
    }
}
