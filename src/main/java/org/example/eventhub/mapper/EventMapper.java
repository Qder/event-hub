package org.example.eventhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.eventhub.domain.Event;

@Mapper
public interface EventMapper extends BaseMapper<Event> {
}
