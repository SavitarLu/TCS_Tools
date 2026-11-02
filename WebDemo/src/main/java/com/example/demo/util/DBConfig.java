package com.example.demo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 数据库连接配置。
 *
 * <p>凭据不再提交到 Git，按以下优先级读取：</p>
 * <ol>
 *   <li>环境变量 {@code DB_URL / DB_USER / DB_PASSWORD / DB_DRIVER}</li>
 *   <li>系统属性 {@code db.url / db.user / db.password / db.driver}</li>
 *   <li>工作目录下的 {@code db.properties}（已加入 .gitignore）</li>
 *   <li>classpath 里的 {@code db.properties}</li>
 * </ol>
 */
public final class DBConfig {

    private static final String FILE_NAME = "db.properties";
    private static final Properties PROPERTIES = load();

    private DBConfig() {
    }

    private static Properties load() {
        Properties props = new Properties();

        File external = new File(FILE_NAME);
        if (external.exists()) {
            try (InputStream in = new FileInputStream(external)) {
                props.load(in);
                return props;
            } catch (IOException e) {
                System.err.println("读取外部 " + FILE_NAME + " 失败: " + e.getMessage());
            }
        }

        try (InputStream in = DBConfig.class.getClassLoader().getResourceAsStream(FILE_NAME)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.err.println("读取 " + FILE_NAME + " 失败: " + e.getMessage());
        }
        return props;
    }

    public static String getUrl() {
        return resolve("db.url", "DB_URL");
    }

    public static String getUser() {
        return resolve("db.user", "DB_USER");
    }

    public static String getPassword() {
        return resolve("db.password", "DB_PASSWORD");
    }

    public static String getDriver() {
        return resolve("db.driver", "DB_DRIVER");
    }

    private static String resolve(String propertyKey, String envKey) {
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim();
        }
        String fromSystem = System.getProperty(propertyKey);
        if (fromSystem != null && !fromSystem.trim().isEmpty()) {
            return fromSystem.trim();
        }
        String fromFile = PROPERTIES.getProperty(propertyKey);
        return fromFile == null ? null : fromFile.trim();
    }
}
