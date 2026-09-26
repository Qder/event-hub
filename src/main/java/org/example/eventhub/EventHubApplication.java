package org.example.eventhub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.eventhub.mapper") // 关键：扫描 Mapper 接口
public class EventHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventHubApplication.class, args);
        System.out.println("====== Event Hub 启动成功 ======");
    }
}