package org.example.eventhub.common;

import lombok.Getter;

/**
 * 统一错误码
 * 编号规则：
 *   0       成功
 *   4xx     通用错误（参数、找不到、冲突、服务器）
 *   1xxx    学生相关
 *   2xxx    活动相关
 *   3xxx    报名相关
 */
@Getter
public enum ErrorCode {

    // ---------- 通用 ----------
    SUCCESS(0, "success"),
    PARAM_ERROR(400, "参数校验失败"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "业务冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ---------- 学生 1xxx ----------
    STUDENT_NOT_FOUND(1001, "学生不存在"),
    STUDENT_DISABLED(1002, "学生已被停用"),
    STUDENT_NO_DUPLICATED(1003, "学号已存在"),

    // ---------- 活动 2xxx ----------
    EVENT_NOT_FOUND(2001, "活动不存在"),
    EVENT_NOT_PUBLISHED(2002, "活动未发布"),
    EVENT_CANCELLED(2003, "活动已取消"),
    EVENT_ALREADY_STARTED(2004, "活动已开始"),
    EVENT_FULL(2005, "活动名额已满"),
    EVENT_HAS_REGISTRATION(2006, "活动已有报名，无法删除"),

    // ---------- 报名 3xxx ----------
    REGISTRATION_DUPLICATED(3001, "重复报名"),
    REGISTRATION_NOT_FOUND(3002, "报名记录不存在"),
    REGISTRATION_STATUS_ILLEGAL(3003, "报名状态不允许该操作"),
    CHECK_IN_TIME_INVALID(3004, "不在签到时间窗口内"),
    CHECK_IN_DUPLICATED(3005, "重复签到");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}