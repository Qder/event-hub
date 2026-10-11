package org.example.eventhub.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /** JWT token */
    private String token;

    /** 学生 ID */
    private Long studentId;

    /** 学号 */
    private String studentNo;

    /** 姓名 */
    private String name;
}