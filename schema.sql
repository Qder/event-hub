CREATE TABLE `student` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                           `student_no` VARCHAR(30) NOT NULL COMMENT '学号',
                           `name` VARCHAR(50) NOT NULL COMMENT '姓名',
                           `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
                           `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 0停用',
                           `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
                           `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `uk_student_student_no` (`student_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `event` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                         `title` VARCHAR(100) NOT NULL COMMENT '活动名称',
                         `category` VARCHAR(30) NOT NULL COMMENT '讲座、比赛、工作坊等',
                         `location` VARCHAR(100) NOT NULL COMMENT '举办地点',
                         `start_time` DATETIME NOT NULL COMMENT '开始时间',
                         `end_time` DATETIME NOT NULL COMMENT '结束时间',
                         `capacity` INT NOT NULL COMMENT '总名额',
                         `available_slots` INT NOT NULL COMMENT '剩余名额',
                         `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/CLOSED/CANCELLED',
                         `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
                         `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                         `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`),
                         KEY `idx_event_status_start_time` (`status`, `start_time`),
                         CONSTRAINT `chk_event_capacity` CHECK (`capacity` > 0),
                         CONSTRAINT `chk_event_available_slots` CHECK (`available_slots` >= 0 AND `available_slots` <= `capacity`),
                         CONSTRAINT `chk_event_time` CHECK (`start_time` < `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `registration` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                                `event_id` BIGINT NOT NULL COMMENT '活动ID',
                                `student_id` BIGINT NOT NULL COMMENT '学生ID',
                                `status` VARCHAR(20) NOT NULL DEFAULT 'REGISTERED' COMMENT 'REGISTERED/CANCELLED/CHECKED_IN',
                                `registered_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
                                `cancelled_at` DATETIME NULL COMMENT '取消时间',
                                `checked_in_at` DATETIME NULL COMMENT '签到时间',
                                `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_registration_event_student` (`event_id`, `student_id`),
                                -- 报名名单分页查询用：WHERE event_id=? AND status=? ORDER BY registered_at DESC
                                -- 第三列给 registered_at，MySQL 8 就能走 Backward index scan 倒扫索引，
                                -- 省掉 ORDER BY 的 filesort。原来的 (event_id, status) 是本索引的前缀，已冗余删除。
                                KEY `idx_registration_event_status_time` (`event_id`, `status`, `registered_at`),
                                KEY `idx_registration_student_status_time` (`student_id`, `status`, `registered_at`),
                                CONSTRAINT `fk_registration_event` FOREIGN KEY (`event_id`) REFERENCES `event` (`id`),
                                CONSTRAINT `fk_registration_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;