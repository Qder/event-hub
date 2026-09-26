package org.example.eventhub.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报名名单条目（JOIN event + student）
 */
@Data
public class RegistrationDetailVO {

    private Long registrationId;
    private Long eventId;
    private String eventTitle;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String status;
    private LocalDateTime registeredAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime checkedInAt;
}