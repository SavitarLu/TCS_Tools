package DB2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
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
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DB2 访问工具。
 *
 * <p>连接信息不再硬编码在源码里，按以下优先级读取：</p>
 * <ol>
 *   <li>环境变量 {@code DB2_URL / DB2_USER / DB2_PASSWORD / DB2_DRIVER}</li>
 *   <li>系统属性 {@code db.url / db.user / db.password / db.driver}</li>
 *   <li>工作目录下的 {@code db.properties}</li>
 *   <li>classpath 里的 {@code db.properties}</li>
 * </ol>
 */
public final class DB2Util {

    private static final String CONFIG_FILE = "db.properties";
    private static final String DRIVER_DEFAULT = "com.ibm.db2.jcc.DB2Driver";

    /** 每个类的列名 → Field 映射，避免每行每列都做反射查找。 */
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;
    private static final String DRIVER;

    static {
        Properties props = loadProperties();
        URL = resolve(props, "db.url", "DB2_URL");
        USER = resolve(props, "db.user", "DB2_USER");
        PASSWORD = resolve(props, "db.password", "DB2_PASSWORD");
        DRIVER = resolve(props, "db.driver", "DB2_DRIVER") != null
                ? resolve(props, "db.driver", "DB2_DRIVER")
                : DRIVER_DEFAULT;

        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("DB2 驱动加载失败: " + e.getMessage());
        }
    }

    private DB2Util() {
    }

    private static Properties loadProperties() {
        Properties props = new Properties();

        File external = new File(CONFIG_FILE);
        if (external.exists()) {
            try (InputStream in = new FileInputStream(external)) {
                props.load(in);
                return props;
            } catch (IOException e) {
                System.err.println("读取 " + CONFIG_FILE + " 失败: " + e.getMessage());
            }
        }

        try (InputStream in = DB2Util.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.err.println("读取 classpath " + CONFIG_FILE + " 失败: " + e.getMessage());
        }
        return props;
    }

    private static String resolve(Properties props, String propertyKey, String envKey) {
        String fromEnv = System.getenv(envKey);
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim();
        }
        String fromSystem = System.getProperty(propertyKey);
        if (fromSystem != null && !fromSystem.trim().isEmpty()) {
            return fromSystem.trim();
        }
        String fromFile = props.getProperty(propertyKey);
        return fromFile == null ? null : fromFile.trim();
    }

    /**
     * 获取数据库连接。
     *
     * @throws SQLException 连接失败或未配置连接信息
     */
    public static Connection getConnection() throws SQLException {
        if (URL == null || URL.isEmpty()) {
            throw new SQLException("未配置数据库连接，请设置环境变量 DB2_URL 或提供 db.properties");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 关闭数据库资源，忽略关闭异常。
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("关闭数据库资源失败: " + e.getMessage());
        }
    }

    /**
     * 执行查询并把结果集映射到实体类。
     *
     * @param sql    带 {@code ?} 占位符的 SQL
     * @param clazz  实体类
     * @param params 占位符参数
     * @throws DataAccessException 数据库访问失败
     */
    public static <T> List<T> executeQuery(String sql, Class<T> clazz, Object... params) {
        List<T> resultList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            bindParams(stmt, params);
            rs = stmt.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            Map<String, Field> fields = FIELD_CACHE.computeIfAbsent(clazz, DB2Util::indexFields);

            while (rs.next()) {
                T obj = clazz.getDeclaredConstructor().newInstance();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i).toLowerCase();
                    Field field = fields.get(columnName);
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
            return resultList;
        } catch (SQLException | ReflectiveOperationException e) {
            throw new DataAccessException("执行查询失败: " + e.getMessage(), e);
        } finally {
            close(conn, stmt, rs);
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
     * 数据库访问异常。原先静默返回空列表，导致"查库出错"和"没有数据"无法区分。
     */
    public static class DataAccessException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public DataAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
