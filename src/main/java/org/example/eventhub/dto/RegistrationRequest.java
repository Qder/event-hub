package org.example.eventhub.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 报名请求
 * studentId 从 token 里取，前端不传
 */
@Data
public class RegistrationRequest {
}
