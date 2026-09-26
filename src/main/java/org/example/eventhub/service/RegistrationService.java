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
     * <p>
     * 必须同时满足：
     * 1. 学生存在且状态正常
     * 2. 活动存在、已发布、未开始、未取消
     * 3. available_slots > 0
     * 4. 学生尚未报名该活动
     *
     * @param eventId   活动 ID
     * @param studentId 学生 ID
     * @return 报名记录 ID
     */
    Long register(Long eventId, Long studentId);

    /**
     * 取消报名
     * <p>
     * 只有 REGISTERED 状态能取消，成功后归还一个名额。
     * 重复取消必须失败，不能多归还名额。
     *
     * @param registrationId 报名记录 ID
     */
    void cancel(Long registrationId);

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
