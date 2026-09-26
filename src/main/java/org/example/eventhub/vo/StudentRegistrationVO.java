package org.example.eventhub.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生报名历史条目（JOIN event）
 */
@Data
public class StudentRegistrationVO {

    private Long registrationId;
    private Long eventId;
    private String eventTitle;
    private String category;
    private String location;
    private LocalDateTime eventStartTime;
    private LocalDateTime eventEndTime;
    private String status;
    private LocalDateTime registeredAt;
}