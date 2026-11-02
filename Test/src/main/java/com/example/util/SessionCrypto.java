package com.example.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 会话密码加解密的全局入口。
 *
 * <p>密钥默认放在 {@code ~/.aixlogfinder/session.key}，可用系统属性
 * {@code aixlogfinder.keyFile} 覆盖（测试时用）。初始化失败时退化为明文，
 * 保证程序不会因为加密不可用而无法启动。</p>
 */
public final class SessionCrypto {

    private static final String KEY_FILE_PROPERTY = "aixlogfinder.keyFile";

    private static CryptoUtil crypto;
    private static boolean initialized;

    private SessionCrypto() {
    }

    private static synchronized CryptoUtil get() {
        if (!initialized) {
            initialized = true;
            String override = System.getProperty(KEY_FILE_PROPERTY);
            Path keyFile = override != null
                    ? Paths.get(override)
                    : Paths.get(System.getProperty("user.home"), ".aixlogfinder", "session.key");
            try {
                crypto = CryptoUtil.loadOrCreate(keyFile);
            } catch (IOException e) {
                System.err.println("无法初始化会话加密，密码将以明文保存: " + e.getMessage());
                crypto = null;
            }
        }
        return crypto;
    }

    public static String encrypt(String plainText) {
        CryptoUtil util = get();
        return util == null ? plainText : util.encrypt(plainText);
    }

    /**
     * 解密；老版本写入的明文会原样返回。密文损坏时返回空串。
     */
    public static String decrypt(String stored) {
        CryptoUtil util = get();
        if (util == null) {
            return stored;
        }
        String plain = util.decrypt(stored);
        return plain == null ? "" : plain;
    }
}
