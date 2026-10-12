package org.example.eventhub.vo;

import lombok.Data;

@Data
public class CurrentUserVO {

    private Long id;
    private String studentNo;
    private String name;
    private String phone;
    private Integer status;
}