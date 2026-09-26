package org.example.eventhub.vo;

import lombok.Data;

/**
 * 活动统计
 * 签到率 = checkedInCount / registeredCount（含取消后的净报名数）
 */
@Data
public class EventStatisticsVO {

    private Long eventId;
    private String eventTitle;
    private Integer capacity;
    private Integer availableSlots;

    /** 累计报名数（含已取消） */
    private Integer totalRegistrationCount;
    /** 当前有效报名数（REGISTERED + CHECKED_IN） */
    private Integer activeRegistrationCount;
    /** 取消数 */
    private Integer cancelledCount;
    /** 签到数 */
    private Integer checkedInCount;
    /** 签到率，保留 4 位小数 */
    private Double checkInRate;
}