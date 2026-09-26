package org.example.eventhub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

/**
 * 诊断：查清两件事
 * 1. Spring 实际读到的 datasource.password 是什么
 * 2. YAML 里 "#" 前不带空格时，注释会不会被吃进值里
 */
@SpringBootTest
class ConfigDiagTest {

    @Autowired
    private Environment env;

    /**
     * DataSourceProperties 是 @ConfigurationProperties 绑定的结果 ——
     * 也就是 Hikari 真正拿到的那些值。它和 env.getProperty() 的解析策略不同。
     */
    @Autowired
    private org.springframework.boot.autoconfigure.jdbc.DataSourceProperties dataSourceProperties;

    @Test
    void printWhatHikariActuallyGets() {
        System.out.println("### --- Hikari 实际拿到的值（@ConfigurationProperties 绑定结果）---");
        System.out.println("### url      = " + dataSourceProperties.getUrl());
        System.out.println("### username = [" + dataSourceProperties.getUsername() + "]");
        String pwd = dataSourceProperties.getPassword();
        System.out.println("### password = [" + pwd + "]");
        System.out.println("### password 长度 = " + (pwd == null ? -1 : pwd.length()));
    }

    @Test
    void printDatasourceConfig() {
        System.out.println("### --- Spring 实际解析结果 ---");
        try {
            String url = env.getProperty("spring.datasource.url");
            String username = env.getProperty("spring.datasource.username");
            String password = env.getProperty("spring.datasource.password");
            System.out.println("### url      = " + url);
            System.out.println("### username = [" + username + "]");
            System.out.println("### password = [" + password + "]");
            System.out.println("### password 长度 = " + (password == null ? -1 : password.length()));
            System.out.println("### password 是否等于 123456 = " + "123456".equals(password));
            System.out.println("### password 是否含 ${ = " + (password != null && password.contains("${")));
        } catch (Exception e) {
            System.out.println("### 读取失败: " + e.getClass().getSimpleName() + " : " + e.getMessage());
        }

        System.out.println("### --- YAML 注释规则实验（SnakeYAML 直接解析）---");
        Yaml yaml = new Yaml();
        String src = "noSpace: 123456# 这是注释\n"
                + "withSpace: 123456 # 这是注释\n";
        Map<String, Object> parsed = yaml.load(src);
        System.out.println("### 无空格写法  noSpace   = [" + parsed.get("noSpace") + "]");
        System.out.println("### 有空格写法  withSpace = [" + parsed.get("withSpace") + "]");
    }
}
