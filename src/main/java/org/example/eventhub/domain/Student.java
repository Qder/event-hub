package org.example.eventhub.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;



@Data
@TableName("student")
public class Student implements Serializable {
    @TableId(type= IdType.AUTO)
    private Long id;
    private String name;
    private Integer age;
    private String studentNo;
    private String phone;
    private int status;
    @TableLogic
    private int deleted;

    public Student() {
        this.id = id;
        this.name = name;
        this.age = age;
        this.studentNo = studentNo;
        this.phone = phone;
        this.status = status;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;

}
