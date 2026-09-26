package org.example.eventhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

//插入学生的信息的约束
@Data
public class StudentCreateRequest {

    @NotBlank(message = "姓名不能为空")
    @Size(max=50, message = "姓名长度不能超过50个字符")
    private String name;

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    private String studentNo;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull(message = "电话号码不能为空")
    @Size(max=20, message = "电话号码不能超过20个字符")
    private String phone;
    // Getters and Setters
}
