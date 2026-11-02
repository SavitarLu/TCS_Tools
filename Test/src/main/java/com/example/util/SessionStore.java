package com.example.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 本地配置文件读写：会话列表（二进制序列化）和下载路径（纯文本）。
 *
 * <p>会话密码在序列化时由 {@link SessionCrypto} 加密，见
 * {@code AixLogFinder.SessionConfig#writeObject}。</p>
 */
public final class SessionStore {

    private SessionStore() {
    }

    /**
     * 读取序列化对象；文件不存在时返回 null。
     */
    @SuppressWarnings("unchecked")
    public static <T> T loadObject(File file) throws IOException, ClassNotFoundException {
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (T) ois.readObject();
        }
    }

    public static void saveObject(File file, Object value) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(value);
        }
    }

    /**
     * 读取下载路径；文件不存在、为空或读取失败时返回 fallback。
     */
    public static String loadDownloadPath(File file, String fallback) {
        if (!file.exists()) {
            return fallback;
        }
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            if (line == null || line.trim().isEmpty()) {
                return fallback;
            }
            return line.trim();
        } catch (IOException e) {
            return fallback;
        }
    }

    public static void saveDownloadPath(File file, String path) throws IOException {
        File parent = file.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("无法创建目录: " + parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(path);
        }
    }
}
