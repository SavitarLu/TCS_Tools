package DB2;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

public class DB2Util {
    // 数据库连接 URL，需要根据实际情况修改主机名、端口、数据库名
    private static final String URL = "jdbc:db2://10.196.60.100:20002/WVMMDB";
    // 数据库用户名
    private static final String USER = "ls2wprd";
    // 数据库密码
    private static final String PASSWORD = "ls2wprd";
    // DB2 JDBC 驱动类名
    private static final String DRIVER = "com.ibm.db2.jcc.DB2Driver";

    // 静态代码块，加载驱动程序
    static {
        try {
            Class.forName(DRIVER);
            //System.out.println("DB2 驱动加载成功");
        } catch (ClassNotFoundException e) {
            System.err.println("DB2 驱动加载失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据库连接
     * @return 数据库连接对象
     * @throws SQLException 如果获取连接失败
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 关闭数据库资源
     * @param conn 数据库连接
     * @param stmt Statement 对象
     * @param rs 结果集
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
     * 泛型查询方法，将结果集映射到指定类型的实体类
     * @param sql 查询 SQL 语句
     * @param clazz 实体类的 Class 对象
     * @param <T> 实体类类型
     * @return 实体类对象列表
     */
    public static <T> List<T> executeQuery(String sql, Class<T> clazz) {
        List<T> resultList = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                T obj = clazz.getDeclaredConstructor().newInstance();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i).toLowerCase(); // 全部转为小写
                    Object value = rs.getObject(i);

                    try {
                        Field field = clazz.getDeclaredField(columnName);
                        field.setAccessible(true);

                        // 处理特殊类型转换
                        if (value != null) {
                            Class<?> fieldType = field.getType();

                            if (fieldType == int.class || fieldType == Integer.class) {
                                value = rs.getInt(i);
                            } else if (fieldType == long.class || fieldType == Long.class) {
                                value = rs.getLong(i);
                            } else if (fieldType == double.class || fieldType == Double.class) {
                                value = rs.getDouble(i);
                            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                                value = rs.getBoolean(i);
                            } else if (fieldType == Timestamp.class) {
                                value = rs.getTimestamp(i);
                            }
                        }

                        field.set(obj, value);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        System.err.println("字段映射失败: " + columnName + " (" + e.getMessage() + ")");
                    }
                }
                resultList.add(obj);
            }
        } catch (SQLException | ReflectiveOperationException e) {
            System.err.println("执行查询失败: " + e.getMessage());
        } finally {
            close(conn, stmt, rs);
        }

        return resultList;
    }


}