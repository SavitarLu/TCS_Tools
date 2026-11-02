package com.example.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

/**
 * 用指定程序打开本地文件，处理 macOS / Windows / Linux 差异。
 */
public final class FileOpener {

    private FileOpener() {
    }

    /**
     * @param filePath 本地文件路径
     * @param editor   编辑器名称，与界面下拉框选项一致
     * @throws IOException 打开失败或该编辑器不支持当前系统
     */
    public static void open(String filePath, String editor) throws IOException {
        File file = new File(filePath);

        if ("系统默认".equals(editor)) {
            openWithSystemDefault(file);
        } else if ("VS Code".equals(editor)) {
            openWithEditor(file, "Visual Studio Code", "code");
        } else if ("Sublime Text".equals(editor)) {
            openWithEditor(file, "Sublime Text", "subl");
        } else if ("记事本".equals(editor)) {
            if (!isWindows()) {
                throw new IOException("记事本仅适用于 Windows 系统");
            }
            start(new String[]{"notepad", file.getAbsolutePath()});
        } else {
            throw new IOException("未知编辑器: " + editor);
        }
    }

    /**
     * 用用户选定的可执行程序 / 应用包打开文件。
     */
    public static void openWithApplication(File application, File file) throws IOException {
        if (isMac()) {
            start(new String[]{"open", "-a", application.getAbsolutePath(), file.getAbsolutePath()});
        } else {
            start(new String[]{application.getAbsolutePath(), file.getAbsolutePath()});
        }
    }

    private static void openWithSystemDefault(File file) throws IOException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(file);
        } else if (isMac()) {
            start(new String[]{"open", file.getAbsolutePath()});
        } else {
            start(new String[]{"xdg-open", file.getAbsolutePath()});
        }
    }

    private static void openWithEditor(File file, String macAppName, String commandName) throws IOException {
        if (isMac()) {
            start(new String[]{"open", "-a", macAppName, file.getAbsolutePath()});
        } else {
            start(new String[]{commandName, file.getAbsolutePath()});
        }
    }

    private static void start(String[] command) throws IOException {
        try {
            new ProcessBuilder(command).start();
        } catch (IOException e) {
            throw new IOException("无法启动 " + command[0] + "：" + e.getMessage());
        }
    }

    public static boolean isMac() {
        return System.getProperty("os.name", "").toLowerCase().contains("mac");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
