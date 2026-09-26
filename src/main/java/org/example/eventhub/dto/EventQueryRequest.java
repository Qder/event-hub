package org.example.eventhub.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventQueryRequest {

    /** 标题关键字 */
    private String title;

    /** 分类 */
    private String category;

    /** 状态：DRAFT / PUBLISHED / CLOSED / CANCELLED */
    private String status;

    /** 开始时间范围-起点 */
    private LocalDateTime startTimeFrom;

    /** 开始时间范围-终点 */
    private LocalDateTime startTimeTo;

    private Integer pageNum = 1;
    private Integer pageSize = 10;
}