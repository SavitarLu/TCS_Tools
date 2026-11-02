package com.example.demo.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DB2 访问工具。
 *
 * <p>使用 HikariCP 连接池，所有查询走 {@link PreparedStatement}，调用方通过
 * {@code ?} 占位符传参，避免 SQL 注入。</p>
 */
public final class DB2Util {

    private static final Logger log = LoggerFactory.getLogger(DB2Util.class);

    /** 每个实体类的 列名 → Field 缓存，避免逐行逐列反射查找。 */
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private static final HikariDataSource DATA_SOURCE;

    static {
        String driver = DBConfig.getDriver();
        if (driver != null && !driver.isEmpty()) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                log.error("DB2 驱动加载失败: {}", e.getMessage());
            }
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DBConfig.getUrl());
        config.setUsername(DBConfig.getUser());
        config.setPassword(DBConfig.getPassword());
        if (driver != null && !driver.isEmpty()) {
            config.setDriverClassName(driver);
        }
        config.setPoolName("db2-pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10_000);
        // 启动时数据库不可用不阻止应用启动，等真正查询时再报错
        config.setInitializationFailTimeout(-1);
        DATA_SOURCE = new HikariDataSource(config);
    }

    private DB2Util() {
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    public static <T> List<T> executeQuery(String sql, Class<T> clazz, Object... params) {
        List<T> resultList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindParams(stmt, params);

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                Map<String, Field> fields = FIELD_CACHE.computeIfAbsent(clazz, DB2Util::indexFields);

                while (rs.next()) {
                    T obj = clazz.getDeclaredConstructor().newInstance();
                    for (int i = 1; i <= columnCount; i++) {
                        Field field = fields.get(metaData.getColumnName(i).toLowerCase());
                        if (field == null) {
                            continue;
                        }
                        Object value = rs.getObject(i);
                        if (value != null) {
                            value = convert(value, field.getType(), rs, i);
                        }
                        field.set(obj, value);
                    }
                    resultList.add(obj);
                }
            }
            return resultList;
        } catch (SQLException | ReflectiveOperationException e) {
            throw new DataAccessException("执行查询失败: " + e.getMessage(), e);
        }
    }

    public static int executeCountQuery(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            bindParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("执行统计查询失败: " + e.getMessage(), e);
        }
    }

    private static Map<String, Field> indexFields(Class<?> clazz) {
        Map<String, Field> map = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            map.put(field.getName().toLowerCase(), field);
        }
        return Collections.unmodifiableMap(map);
    }

    private static Object convert(Object value, Class<?> fieldType, ResultSet rs, int index) throws SQLException {
        if (fieldType == int.class || fieldType == Integer.class) {
            return rs.getInt(index);
        }
        if (fieldType == long.class || fieldType == Long.class) {
            return rs.getLong(index);
        }
        if (fieldType == double.class || fieldType == Double.class) {
            return rs.getDouble(index);
        }
        if (fieldType == boolean.class || fieldType == Boolean.class) {
            return rs.getBoolean(index);
        }
        if (fieldType == Timestamp.class) {
            return rs.getTimestamp(index);
        }
        return value;
    }

    private static void bindParams(PreparedStatement stmt, Object[] params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    /**
     * 数据库访问异常。原先静默返回空列表，"出错"和"没数据"无法区分。
     */
    public static class DataAccessException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public DataAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
