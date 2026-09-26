package org.example.eventhub.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报名记录
 * 注意：registration 表没有 deleted 字段，不做逻辑删除
 */
@Data
@TableName("registration")
public class Registration implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long eventId;

    private Long studentId;

    /** REGISTERED / CANCELLED / CHECKED_IN */
    private String status;

    private LocalDateTime registeredAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime checkedInAt;

    /** 乐观锁版本 */
    private Integer version;
}