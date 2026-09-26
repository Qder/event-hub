package org.example.eventhub.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 添加乐观锁插件
        // 注意：多插件场景下，乐观锁插件建议放在分页插件之前，避免 SQL 改写冲突
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 2. 添加分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);

        // 3. 设置 maxLimit（单页最大条数限制）
        // 防止恶意请求传入超大 pageSize 导致内存溢出，这里限制为 100 条
        paginationInnerInterceptor.setMaxLimit(100L);

        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        return interceptor;
    }
}