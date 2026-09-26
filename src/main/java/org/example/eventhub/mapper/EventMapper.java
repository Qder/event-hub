package org.example.eventhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.example.eventhub.domain.Event;

@Mapper
public interface EventMapper extends BaseMapper<Event> {
    /**
     * 原子扣减名额：只有当 available_slots > 0 时才扣减
     * 返回受影响行数，0 表示名额已满或并发冲突
     */
    @Update("UPDATE event SET available_slots = available_slots - 1, version = version + 1 " +
            "WHERE id = #{eventId} AND available_slots > 0 AND deleted = 0")
    int decreaseAvailableSlots(@Param("eventId") Long eventId);

    /**
     * 原子归还名额：只有当 available_slots < capacity 时才归还
     * 返回受影响行数，0 表示无需归还或并发冲突
     */
    @Update("UPDATE event SET available_slots = available_slots + 1, version = version + 1 " +
            "WHERE id = #{eventId} AND available_slots < capacity AND deleted = 0")
    int increaseAvailableSlots(@Param("eventId") Long eventId);
}
