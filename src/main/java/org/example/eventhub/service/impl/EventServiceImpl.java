package org.example.eventhub.service.impl;

import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.eventhub.common.BusinessException;
import org.example.eventhub.common.ErrorCode;
import org.example.eventhub.domain.Event;
import org.example.eventhub.domain.Student;
import org.example.eventhub.dto.EventCreateRequest;
import org.example.eventhub.dto.EventQueryRequest;
import org.example.eventhub.mapper.EventMapper;
import org.example.eventhub.mapper.StudentMapper;
import org.example.eventhub.service.EventService;
import org.example.eventhub.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class EventServiceImpl extends ServiceImpl<EventMapper, Event> implements EventService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createEvent(EventCreateRequest request) {
        //检查开始时间早于结束时间
        if(!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "开始时间必须早于结束时间");
        }

        // 2. 组装实体
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setCategory(request.getCategory());
        event.setLocation(request.getLocation());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setCapacity(request.getCapacity());
        // 核心：available_slots 初始 = capacity
        event.setAvailableSlots(request.getCapacity());
        event.setStatus(STATUS_DRAFT);
        event.setVersion(0);

        // 3. 保存
        boolean saved = this.save(event);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "保存活动失败");
        }

        return event.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishEvent(Long id) {
        // 1. 检查活动是否存在
        Event existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.EVENT_NOT_FOUND);
        }

        // 2. 检查状态是否为 DRAFT
        if (!STATUS_DRAFT.equals(existing.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT,"只有 DRAFT 状态的活动可以发布");
        }

        // 3. 更新状态为 PUBLISHED，使用乐观锁
        existing.setStatus(STATUS_PUBLISHED);
        boolean updated = this.updateById(existing);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "活动已被其他人修改，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEvent(Long id) {
        //检查活动是否存在
        Event existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.EVENT_NOT_FOUND);
        }

        //检查活动是否已发布
        if (STATUS_PUBLISHED.equals(existing.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "已发布的活动不能删除");
        }
        // 逻辑删除：@TableLogic 会自动把 SQL 从 DELETE 改成 UPDATE deleted=1
        this.removeById(id);
    }

    @Override
    public Event getEventById(Long id) {
        Event event = this.getById(id);
        if (event == null) {
            throw new BusinessException(ErrorCode.EVENT_NOT_FOUND);
        }
        return event;
    }

    @Override
    public Page<Event> pageEvents(EventQueryRequest request) {
        // 1. 校验分页参数
        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        if (pageSize > 100) {
            pageSize = 100;
        }

        //构造条件
        LambdaQueryWrapper<Event> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(request.getTitle()), Event::getTitle, request.getTitle());
        wrapper.eq(StringUtils.hasText(request.getCategory()), Event::getCategory, request.getCategory());
        wrapper.eq(StringUtils.hasText(request.getStatus()), Event::getStatus, request.getStatus());
        wrapper.ge(request.getStartTimeFrom() != null, Event::getStartTime, request.getStartTimeFrom());
        wrapper.le(request.getStartTimeTo() != null, Event::getStartTime, request.getStartTimeTo());
        wrapper.orderByDesc(Event::getCreatedAt);

        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }
}
