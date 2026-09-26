package org.example.eventhub.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("event")
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String category;

    private String location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 总名额 */
    private Integer capacity;

    /** 剩余名额，不能小于 0 */
    private Integer availableSlots;

    /** DRAFT / PUBLISHED / CLOSED / CANCELLED */
    private String status;

    /** 乐观锁版本 */
    @Version
    private Integer version;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}