package org.example.eventhub.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventCreateRequest {

    @NotBlank(message = "活动标题不能为空")
    @Size(max = 100, message = "活动标题长度不能超过 100")
    private String title;

    @NotBlank(message = "活动分类不能为空")
    @Size(max = 30, message = "活动分类长度不能超过 30")
    private String category;

    @NotBlank(message = "活动地点不能为空")
    @Size(max = 100, message = "活动地点长度不能超过 100")
    private String location;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @NotNull(message = "容量不能为空")
    @Min(value = 1, message = "容量必须大于 0")
    private Integer capacity;
}