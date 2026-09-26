package org.example.eventhub.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistrationRequest {

    @NotNull(message = "学生 ID 不能为空")
    private Long studentId;
}
