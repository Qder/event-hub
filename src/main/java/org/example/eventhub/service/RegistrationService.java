package org.example.eventhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.eventhub.domain.Event;
import org.example.eventhub.domain.Registration;

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
}
