package org.example.eventhub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StudentUpdateRequest {
    @NotNull(message = "学生姓名不能为空")
    @Size(max = 50, message = "学生姓名长度不能超过50个字符")
    private String name;

    @NotNull(message = "电话号码不能为空")
    @Size(max = 20, message = "电话号码长度不能超过20个字符")
    private String phone;

    @NotNull(message = "状态不能为空")
    // 删掉 @Size，改成：
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
