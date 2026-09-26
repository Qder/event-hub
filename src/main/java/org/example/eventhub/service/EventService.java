package org.example.eventhub.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.eventhub.domain.Event;
import org.example.eventhub.dto.EventCreateRequest;
import org.example.eventhub.dto.EventQueryRequest;

public interface EventService extends IService<Event> {

    /**
     * 创建活动
     * <p>
     * 规则：
     * 1. 开始时间必须早于结束时间
     * 2. 初始 availableSlots = capacity
     * 3. 初始 status = DRAFT
     * 4. 初始 version = 0
     *
     * @param request 活动创建请求
     * @return 新创建活动的主键 ID
     */
    Long createEvent(EventCreateRequest request);

    /**
     * 发布活动
     * <p>
     * 只有 DRAFT 状态的活动可以发布，成功后 status 变为 PUBLISHED。
     * 通过乐观锁（@Version）保证并发场景下状态不被覆盖。
     *
     * @param id 活动 ID
     */
    void publishEvent(Long id);

    /**
     * 删除活动（逻辑删除）
     * <p>
     * 如果活动已经有报名记录，不允许删除（Sprint 2 接入报名表后完善）。
     *
     * @param id 活动 ID
     */
    void deleteEvent(Long id);

    /**
     * 根据 ID 查询活动
     * <p>
     * 活动不存在时抛出 {@link org.example.eventhub.common.BusinessException}。
     *
     * @param id 活动 ID
     * @return 活动实体
     */
    Event getEventById(Long id);

    /**
     * 分页查询活动列表
     * <p>
     * 支持按标题关键字、分类、状态、开始时间范围动态筛选，
     * 默认按创建时间倒序排列。
     *
     * @param request 分页查询请求
     * @return 分页结果
     */
    Page<Event> pageEvents(EventQueryRequest request);
}