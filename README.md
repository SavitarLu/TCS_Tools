# TCS_Tools

TCS（Transmission Control System）现场工具集，包含两个独立模块：

| 模块 | 类型 | 说明 |
| --- | --- | --- |
| `Test/` | Java Swing 桌面程序 | Aix/TCS 服务器日志查找工具：SSH 登录、按关键字/时间检索 applog、下载并本地打开 |
| `WebDemo/` | Spring Boot Web 应用 | 机台通信调试（TCP 代理、WebSocket 实时回显）与 DB2 历史报表（告警、事件） |

---

## 一、桌面工具 Test（AixLogFinder）

### 构建

```bash
cd Test
mvn clean package
```

产物：`Test/target/FindLog.jar`（含全部依赖，可直接双击或 `java -jar` 运行）。

```bash
java -jar target/FindLog.jar
```

要求 JDK 8 及以上（编译目标为 Java 8）。

### 功能

- 多服务器会话配置（主机 / 用户 / 密码 / 搜索目录），保存在本机
- 支持普通模式与 MES 模式；MES 模式默认同时搜索
  `/home/<user>/mm/applog` 和 `/home/<user>/wfview_app/mm/applog`
- 按关键字、TX ID、时间范围查找日志
- 双击结果下载到本地并用指定编辑器打开（系统默认 / VS Code / Sublime Text / 记事本 / 自选程序）
- TCS 配置查询：根据机台号从 DB2 读取 EQP、PORT 信息

### 本地数据与密码保护

| 文件 / 目录 | 内容 |
| --- | --- |
| `~/.aixlogfinder/session.key` | AES-GCM 密钥（首次运行自动生成，权限 `rw-------`） |
| `sessions_*.dat` | 会话列表，密码字段加密存储 |

可用 `-Daixlogfinder.keyFile=/path/to/key` 指定密钥位置。旧版本留下的明文会话文件仍可读取，
下次保存时会自动转成加密格式。

> 密钥与密文在同一台机器上，只能防止配置文件被顺手复制走，无法抵御能读取你用户目录的攻击者。

---

## 二、Web 应用 WebDemo

### 构建与运行

```bash
cd WebDemo
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

默认端口 8080，访问 `http://localhost:8080/login`。

### 配置

数据库与 SSH 密码都不再写在代码里，按以下优先级读取：

1. 环境变量
2. JVM 系统属性
3. 工作目录下的配置文件
4. classpath 下的 `db.properties`（仅数据库）

| 用途 | 环境变量 | 系统属性 | 配置文件 |
| --- | --- | --- | --- |
| 数据库连接 | `DB_URL` `DB_USER` `DB_PASSWORD` `DB_DRIVER` | `db.url` `db.user` `db.password` `db.driver` | `db.properties` |
| TCS 服务器 SSH | `TCS_SSH_SERVER` `TCS_SSH_USER` `TCS_SSH_PASSWORD` `TCS_SSH_FILE` | `ssh.server` `ssh.user` `ssh.password` `ssh.filePath` | `ssh.properties` |

`db.properties`、`ssh.properties` 已加入 `.gitignore`，不要提交真实口令。
配置模板见 `src/main/resources/db.properties.example`、`ssh.properties.example`。

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties   # 填写真实值
cp ssh.properties.example ssh.properties                                      # 填写真实值
```

### 页面

| 路径 | 说明 |
| --- | --- |
| `/login`、`/logout` | 登录 / 退出（登录状态存于 HttpSession，未登录访问其它页面会被重定向到登录页） |
| `/report` | 报表入口 |
| `/report/decsalrm-his` | 告警历史报表，支持机台号、告警号、时间范围过滤与分页 |
| `/socket` | TCP 调试客户端：通过 WebSocket 连到远端机台端口，实时收发报文 |

### 实现要点

- 数据库访问使用 HikariCP 连接池，全部走 `PreparedStatement` 占位符，杜绝 SQL 拼接注入
- WebSocket 握手携带 HttpSession，未登录的 CONNECT 帧会被拒绝
- TCP 代理按 STOMP 会话隔离，每个浏览器各自一条连接，消息只推送到 `/topic/messages/{sessionId}`，会话断开会自动释放连接
- 桌面端执行的远端 shell 命令参数统一用单引号转义（`ShellEscape`）

---

## 三、目录结构

```
TCS_Tools/
├── Test/                      # 桌面日志查找工具
│   ├── pom.xml
│   └── src/main/java/
│       ├── com/example/AixLogFinder.java
│       ├── com/example/util/   # ShellEscape / CryptoUtil / SessionCrypto / SessionStore / FileOpener
│       └── DB2/DB2Util.java
└── WebDemo/                   # Spring Boot Web 应用
    ├── pom.xml
    └── src/main/
        ├── java/com/example/demo/
        │   ├── config/         # 登录拦截器、WebSocket 配置
        │   ├── controller/     # 登录、报表、TCP 代理
        │   ├── data/           # 实体类
        │   ├── service/        # TcpProxyService
        │   └── util/           # DB2Util / DBConfig / SshConfig
        └── resources/
            ├── db.properties.example
            └── templates/      # Thymeleaf 页面
```

---

## 四、测试

```bash
cd Test && mvn test            # ShellEscape / CryptoUtil / SessionStore 单元测试
cd WebDemo && mvn test         # 目前无测试用例
```

## 五、注意事项

- 工具使用 `StrictHostKeyChecking=no`，不会校验服务器指纹，仅建议在受信任的内网使用。
- DB2 中的用户密码目前按明文比较；如需更强安全性，建议改为存储加盐哈希并在登录时校验哈希值。
- 仓库中不得提交 `.dat`、`*.jar`、`target/`、真实 `db.properties` / `ssh.properties`。
