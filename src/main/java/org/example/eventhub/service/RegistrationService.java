package org.example.eventhub.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.eventhub.domain.Registration;
import org.example.eventhub.dto.RegistrationQueryRequest;
import org.example.eventhub.vo.EventStatisticsVO;
import org.example.eventhub.vo.RegistrationDetailVO;
import org.example.eventhub.vo.StudentRegistrationVO;

import java.util.List;

public interface RegistrationService extends IService<Registration> {
    /**
     * 学生报名活动
     * @param eventId 活动 ID
     * @param studentId 当前登录学生 ID
     */
    Long register(Long eventId, Long studentId);

    /**
     * 取消报名
     * <p>
     * 只允许取消自己的报名记录。
     * @param registrationId 报名记录 ID
     * @param currentUserId 当前登录学生 ID
     */
    void cancel(Long registrationId, Long currentUserId);

    /**
     * 学生签到
     * <p>
     * 只有 REGISTERED 状态可以签到。
     * 签到时间窗口：活动开始前 30 分钟到开始后 60 分钟。
     * 成功后状态变为 CHECKED_IN 并记录签到时间。
     * 重复签到返回业务失败。
     *
     * @param registrationId 报名记录 ID
     */
    void checkIn(Long registrationId);

    IPage<RegistrationDetailVO> pageRegistrationDetails(Long eventId, RegistrationQueryRequest request);

    List<StudentRegistrationVO> getStudentRegistrationHistory(Long studentId);

    EventStatisticsVO getEventStatistics(Long eventId);
}
