# Campus Event Hub

校园活动报名与签到系统。Spring Boot 3 + MyBatis-Plus + MyBatis XML + MySQL 8。

## 技术栈

| 分类   | 选择                       |
| ------ | -------------------------- |
| JDK    | 17                         |
| Web    | Spring Boot 3.3.4          |
| ORM    | MyBatis-Plus 3.5.7         |
| SQL    | MyBatis XML                |
| 数据库 | MySQL 8                    |
| 测试   | JUnit 5 + Spring Boot Test |

## 功能概览

- 学生管理：创建、更新、逻辑删除、分页查询
- 活动管理：创建、发布、逻辑删除、分页查询、乐观锁更新
- 报名管理：报名、取消、签到，保证名额不超卖
- 报表查询：报名名单分页、学生报名历史、活动统计

## 快速开始

### 1. 准备数据库

```sql
CREATE DATABASE event_hub CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

执行 `src/main/resources/schema.sql` 建表。

### 2. 配置数据库连接

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/event_hub?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  mapper-locations: classpath*:/mapper/**/*.xml
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 3. 启动

```bash
./mvnw.cmd spring-boot:run
```

### 4. 运行测试

```bash
./mvnw.cmd test
```

## 接口清单

### 学生

| 方法   | 路径                             | 说明         |
| ------ | -------------------------------- | ------------ |
| POST   | /api/students                    | 创建学生     |
| GET    | /api/students                    | 分页查询学生 |
| GET    | /api/students/{id}               | 查询学生详情 |
| PUT    | /api/students/{id}               | 更新学生     |
| DELETE | /api/students/{id}               | 逻辑删除学生 |
| GET    | /api/students/{id}/registrations | 学生报名历史 |

### 活动

| 方法   | 路径                           | 说明         |
| ------ | ------------------------------ | ------------ |
| POST   | /api/events                    | 创建活动     |
| GET    | /api/events                    | 分页查询活动 |
| GET    | /api/events/{id}               | 查询活动详情 |
| POST   | /api/events/{id}/publish       | 发布活动     |
| DELETE | /api/events/{id}               | 逻辑删除活动 |
| GET    | /api/events/{id}/registrations | 报名名单分页 |
| GET    | /api/events/{id}/statistics    | 活动统计     |

### 报名

| 方法 | 路径                             | 说明     |
| ---- | -------------------------------- | -------- |
| POST | /api/events/{id}/registrations   | 学生报名 |
| POST | /api/registrations/{id}/cancel   | 取消报名 |
| POST | /api/registrations/{id}/check-in | 签到     |

## 接口示例

### 创建学生

```bash
curl -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{"studentNo":"T2026001","name":"张三","phone":"13800000000"}'
```

响应：

```json
{ "code": 0, "message": "success", "data": 1 }
```

### 创建活动

```bash
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "title":"Java 实战讲座",
    "category":"讲座",
    "location":"A101",
    "startTime":"2026-10-01T14:00:00",
    "endTime":"2026-10-01T16:00:00",
    "capacity":50
  }'
```

### 发布活动

```bash
curl -X POST http://localhost:8080/api/events/1/publish
```

### 报名

```bash
curl -X POST http://localhost:8080/api/events/1/registrations \
  -H "Content-Type: application/json" \
  -d '{"studentId":1}'
```

### 报名名单（分页 + 动态筛选）

```bash
curl "http://localhost:8080/api/events/1/registrations?pageNum=1&pageSize=10&status=REGISTERED"
```

### 活动统计

```bash
curl http://localhost:8080/api/events/1/statistics
```

响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "eventId": 1,
    "eventTitle": "Java 实战讲座",
    "capacity": 50,
    "availableSlots": 47,
    "totalRegistrationCount": 3,
    "activeRegistrationCount": 3,
    "cancelledCount": 0,
    "checkedInCount": 1,
    "checkInRate": 0.3333
  }
}
```

### 取消报名

```bash
curl -X POST http://localhost:8080/api/registrations/1/cancel
```

### 签到

```bash
curl -X POST http://localhost:8080/api/registrations/1/check-in
```

## 错误码

| code | 含义                   |
| ---- | ---------------------- |
| 0    | 成功                   |
| 400  | 参数校验失败           |
| 404  | 资源不存在             |
| 409  | 业务冲突               |
| 500  | 服务器内部错误         |
| 1001 | 学生不存在             |
| 1002 | 学生已被停用           |
| 1003 | 学号已存在             |
| 2001 | 活动不存在             |
| 2002 | 活动未发布             |
| 2003 | 活动已取消             |
| 2004 | 活动已开始             |
| 2005 | 活动名额已满           |
| 2006 | 活动已有报名，无法删除 |
| 3001 | 重复报名               |
| 3002 | 报名记录不存在         |
| 3003 | 报名状态不允许该操作   |
| 3004 | 不在签到时间窗口内     |
| 3005 | 重复签到               |

## 分层设计

| 层         | 职责                                                |
| ---------- | --------------------------------------------------- |
| Controller | HTTP 请求、参数校验、调用 Service、封装 ApiResponse |
| Service    | 业务规则与事务边界                                  |
| Mapper     | 数据访问，单表用 MyBatis-Plus，多表/聚合用 XML      |
| DTO        | 前端入参                                            |
| VO         | 跨表查询与聚合查询的出参                            |
| Entity     | 数据库表                                            |

**Controller 不直接调用 Mapper，业务规则不写在 Controller。**

## 并发控制

### 乐观锁

`Event` 实体上的 `@Version` 字段 + `OptimisticLockerInnerInterceptor` 插件。

用于：发布活动、更新活动。
机制：`UPDATE ... SET version = version + 1 WHERE id = ? AND version = ?`。
版本不一致时影响行数为 0，调用方抛业务冲突异常。

### 原子更新（核心）

报名扣名额：

```sql
UPDATE event
SET available_slots = available_slots - 1, version = version + 1
WHERE id = ?
  AND available_slots > 0
  AND deleted = 0
```

把"判断名额 > 0 + 扣减"合并为一条 SQL，由数据库保证原子性。
影响行数为 0 时抛"名额已满"。

取消报名：

```sql
UPDATE registration
SET status = 'CANCELLED', cancelled_at = NOW(), version = version + 1
WHERE id = ? AND status = 'REGISTERED'
```

只有 REGISTERED 状态能取消，重复取消影响行数为 0，不会多归还名额。

### 唯一约束兜底

- `student(student_no)` 唯一
- `registration(event_id, student_id)` 唯一，防止同一学生重复报名

### 事务

`register` 和 `cancel` 都标注 `@Transactional(rollbackFor = Exception.class)`。
扣名额和写报名记录在同一事务内，任一步失败全部回滚。

## 索引

- `student(student_no)` 唯一
- `registration(event_id, student_id)` 唯一
- `registration(event_id, status)` 复合索引
- `registration(student_id, status, registered_at)` 复合索引
- `event(status, start_time)` 复合索引

## 索引验证

对报名名单分页 SQL 执行 EXPLAIN：

```sql
EXPLAIN SELECT
    r.id, r.event_id, e.title, r.student_id,
    s.name, s.student_no, r.status, r.registered_at
FROM registration r
INNER JOIN student s ON s.id = r.student_id AND s.deleted = 0
INNER JOIN event   e ON e.id = r.event_id   AND e.deleted = 0
WHERE r.event_id = 127
  AND r.status = 'REGISTERED'
ORDER BY r.registered_at DESC;
```

执行计划：

| table | type   | key                           | key_len | rows | Extra                                      |
| ----- | ------ | ----------------------------- | ------- | ---- | ------------------------------------------ |
| e     | const  | PRIMARY                       | 8       | 1    | NULL                                       |
| r     | range  | idx_registration_event_status | 90      | 1    | Using index condition; Backward index scan |
| s     | eq_ref | PRIMARY                       | 8       | 1    | Using where                                |

结论：

- `event` 走主键 `const`，最优。
- `registration` 命中 `(event_id, status)` 复合索引，`key_len = 90` 说明两个字段都用上。
- `student` 走主键 `eq_ref`，最优。
- 无 `Using filesort`、无 `Using temporary`。

数据量增长到十万级时，可考虑扩展索引为 `(event_id, status, registered_at)` 以覆盖 ORDER BY，避免 filesort。

## 复盘

### 1. 报名为什么要放在 Service，而不是 Controller 或 Mapper？

报名涉及"检查学生 → 检查活动 → 检查重复 → 扣名额 → 写报名记录"五步，必须原子执行。
Controller 只负责 HTTP，不该管事务；Mapper 一次只能执行一条 SQL，做不到多步事务。
Service 是事务边界，`@Transactional` 加在 Service 方法上，任一步失败全部回滚。

### 2. BaseMapper、IService、Wrapper 各负责什么？什么时候不用它们？

| 组件               | 场景                                   |
| ------------------ | -------------------------------------- |
| BaseMapper         | 单表 CRUD 的 SQL 封装                  |
| IService           | 在 Mapper 之上的批量方法与链式查询入口 |
| LambdaQueryWrapper | 单表动态条件、排序、分页               |

不用它们的场景：JOIN、聚合统计、复杂投影。用 XML 更清晰。

### 3. 手写 XML 为什么不会自动加逻辑删除条件？

MyBatis-Plus 的 `@TableLogic` 只对它自动生成的 SQL 生效。
手写 XML 时，MyBatis 直接把 SQL 发给数据库，`@TableLogic` 不会被触发。
所以手写查询必须显式加 `deleted = 0`。

### 4. 乐观锁与 available_slots > 0 条件更新分别防住了什么？

- 乐观锁（`@Version`）防的是**丢失更新**：两个请求读到同一份数据，后写覆盖先写。
- 条件更新（`available_slots > 0`）防的是**超卖**：两个请求都读到名额 > 0，同时扣减导致负数。

乐观锁检查"读出来之后数据有没有被改过"，条件更新检查"当前值是否满足业务条件"。两者可以独立使用，也可以配合。

### 5. 为什么多表分页与统计更适合 XML？

1. `LambdaQueryWrapper` 只针对单表，生成的 SQL 里没有 JOIN。
2. `COUNT`、`SUM`、`CASE WHEN` 等聚合函数无法用 Wrapper 表达。
3. 多表查询的字段映射需要 `AS` 别名，Wrapper 层控制不了。
4. 动态 SQL 用 `<if>`、`<choose>` 更灵活。

不是"XML 比 Wrapper 好"，而是这些查询 Wrapper 做不到。

### 6. @Transactional 在什么情况下不会生效？怎样写测试验证回滚？

不会生效的常见情况：

- 同类内部方法调用 `this.method()`，不走 Spring 代理。
- 方法不是 public。
- 异常被 `try-catch` 吞掉，没有 `throw`。
- 异常类型不匹配（默认只对 RuntimeException 回滚，用 `rollbackFor = Exception.class` 覆盖）。
- 数据库引擎不支持事务（如 MyISAM）。

验证回滚的测试思路：
在测试里人为制造"第二步失败"，断言第一步的写操作已经回滚。
例如用 `@MockBean` 替换 `RegistrationMapper`，让 `insert` 抛异常，
然后断言 `event.available_slots` 没有被扣减。
