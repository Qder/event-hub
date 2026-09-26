package org.example.eventhub.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import java.time.LocalDateTime;

public class Event {
    @TableId(type= IdType.AUTO)
    Long id;
    String title;
    String category;
    String location;
    LocalDateTime startTime;
    LocalDateTime endTime;
    int capacity;
    int availableSlots;
    String status;
    int version;
    @TableLogic
    int deleted;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public Event() {
    }
}
