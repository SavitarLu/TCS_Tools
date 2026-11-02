package com.example.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 本地配置文件用的对称加密（AES/GCM）。
 *
 * <p>密钥保存在用户目录下的独立文件中，权限尽量设为仅本人可读。
 * 这样即使 sessions_*.dat 被复制出去，里面的服务器密码也无法直接读出。</p>
 *
 * <p>注意：密钥和密文存在同一台机器上时，这只能防止"文件被顺手拿走"，
 * 挡不住能读你用户目录的攻击者。要更强保护请改用系统钥匙串。</p>
 */
public final class CryptoUtil {

    private static final String PREFIX = "enc:v1:";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;
    private static final int KEY_BYTES = 32;

    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();

    private CryptoUtil(SecretKey key) {
        this.key = key;
    }

    /**
     * 从指定路径加载密钥，不存在则生成。
     */
    public static CryptoUtil loadOrCreate(Path keyFile) throws IOException {
        byte[] keyBytes;
        if (Files.exists(keyFile)) {
            String encoded = new String(Files.readAllBytes(keyFile), StandardCharsets.UTF_8).trim();
            keyBytes = Base64.getDecoder().decode(encoded);
        } else {
            keyBytes = new byte[KEY_BYTES];
            new SecureRandom().nextBytes(keyBytes);
            Path parent = keyFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(keyFile, Base64.getEncoder().encodeToString(keyBytes).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            restrictPermissions(keyFile);
        }
        if (keyBytes.length != KEY_BYTES) {
            throw new IOException("密钥文件格式不正确: " + keyFile);
        }
        return new CryptoUtil(new SecretKeySpec(keyBytes, "AES"));
    }

    private static void restrictPermissions(Path keyFile) {
        try {
            Files.setPosixFilePermissions(keyFile,
                    java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException | IOException ignored) {
            // Windows 等不支持 POSIX 权限，忽略
        }
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    /**
     * 加密明文；null 原样返回。
     */
    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("加密失败", e);
        }
    }

    /**
     * 解密。不是本工具加密的旧数据（没有 enc:v1: 前缀）会原样返回，
     * 这样老版本的明文 sessions_*.dat 仍然能读。
     *
     * @return 明文；密文损坏时返回 null
     */
    public String decrypt(String stored) {
        if (!isEncrypted(stored)) {
            return stored;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key,
                    new GCMParameterSpec(TAG_BITS, payload, 0, IV_LENGTH));
            byte[] plain = cipher.doFinal(payload, IV_LENGTH, payload.length - IV_LENGTH);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return null;
        }
    }
}
