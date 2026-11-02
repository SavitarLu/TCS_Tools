package com.example.util;

/**
 * Shell 参数转义工具。
 *
 * <p>所有拼接到远程 shell 命令里的外部输入（TX ID、机台 ID、grep 条件等）
 * 都必须经过这里转义，否则用户输入 {@code $(...) } 或反引号会在远端执行。</p>
 */
public final class ShellEscape {

    private ShellEscape() {
    }

    /**
     * 用单引号包裹参数，并把内部单引号转义为 {@code '\''}。
     *
     * <p>单引号内部 shell 不做任何展开，因此 $、反引号、分号等都不会生效。</p>
     *
     * @param arg 原始参数，允许为 null
     * @return 可以安全拼进命令行的字符串
     */
    public static String quote(String arg) {
        if (arg == null) {
            return "''";
        }
        return "'" + arg.replace("'", "'\\''") + "'";
    }
}
