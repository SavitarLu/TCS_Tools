package com.example.demo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * TCS 服务器 SSH 配置。
 *
 * <p>密码不再写死在代码里，按以下优先级读取：</p>
 * <ol>
 *   <li>环境变量 {@code TCS_SSH_SERVER / TCS_SSH_USER / TCS_SSH_PASSWORD / TCS_SSH_FILE}</li>
 *   <li>系统属性 {@code ssh.server / ssh.user / ssh.password / ssh.filePath}</li>
 *   <li>工作目录下的 {@code ssh.properties}（已加入 .gitignore）</li>
 * </ol>
 */
public final class SshConfig {

    private static final String FILE_NAME = "ssh.properties";
    private static final Properties PROPERTIES = load();

    private SshConfig() {
    }

    private static Properties load() {
        Properties props = new Properties();
        File external = new File(FILE_NAME);
        if (external.exists()) {
            try (InputStream in = new FileInputStream(external)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("读取 " + FILE_NAME + " 失败: " + e.getMessage());
            }
        }
        return props;
    }

    public static String getServer() {
        return resolve("ssh.server", "TCS_SSH_SERVER", "10.196.60.107");
    }

    public static String getUser() {
        return resolve("ssh.user", "TCS_SSH_USER", "ls2wtcs");
    }

    /**
     * @return 密码；未配置时返回 null，调用方应提示管理员配置
     */
    public static String getPassword() {
        return resolve("ssh.password", "TCS_SSH_PASSWORD", null);
    }

    public static String getFilePath() {
        return resolve("ssh.filePath", "TCS_SSH_FILE", "/home/ls2wtcs/tcsx/cmd/tcsxList.txt");
    }

    private static String resolve(String propertyKey, String envKey, String defaultValue) {
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim();
        }
        String fromSystem = System.getProperty(propertyKey);
        if (fromSystem != null && !fromSystem.trim().isEmpty()) {
            return fromSystem.trim();
        }
        String fromFile = PROPERTIES.getProperty(propertyKey);
        if (fromFile != null && !fromFile.trim().isEmpty()) {
            return fromFile.trim();
        }
        return defaultValue;
    }
}
