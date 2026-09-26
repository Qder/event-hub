package org.example.eventhub.dto;

import lombok.Data;

@Data
public class RegistrationQueryRequest {

    /** 关键字：匹配学生姓名或学号 */
    private String keyword;

    /** 报名状态：REGISTERED / CANCELLED / CHECKED_IN */
    private String status;

    /** 签到状态：true 只看已签到，false 只看未签到，null 不限 */
    private Boolean checkedIn;

    private Integer pageNum = 1;
    private Integer pageSize = 10;
}