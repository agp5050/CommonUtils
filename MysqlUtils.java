

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
@Component
public class MysqlUtils {
    private static Logger LOG = LoggerFactory.getLogger(MysqlUtils.class);
    private  Datasource mysqlDataSource = null;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.url}")
    private String url;
    {
        try {
            mysqlDataSource = new Datasource().getInstance();
        } catch (Exception e) {
            LOG.error("Mysql data source init error", e);
        }
    }

    public  Map<String, Object> executeSql(String sql) {
        Connection conn = null;
        PreparedStatement stmt = null;
        Map<String, Object> results = null;
        ResultSet rs = null;
        try {
            conn = mysqlDataSource.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.execute();
            rs = stmt.executeQuery();
            // 元数据；对对象取到的结果集数据的描述
            ResultSetMetaData rsmd = rs.getMetaData();
            int cloumCount = rsmd.getColumnCount();
            if (rs.next()) { // 判断结果集是否还有数据（数据是一条记录的方式取出）
                results = new HashMap<String, Object>();
                for (int i = 1; i <= cloumCount; i++) {
                    // rsmd.getColumnName(i); // 表的字段名或字段别名
                    // rs.getObject(i); // 取到字段对应的值
                    results.put(rsmd.getColumnName(i).toLowerCase(), rs.getObject(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Executing sql {} error", sql, e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return results;
    }

    /**
     * 参数
     * @param st
     * @param objs
     */
    private static void setParams(PreparedStatement st, Object... objs) {
        // 判断是否有参数
        if (objs == null || objs.length == 0) {
            return;
        }
        int flag = 0;
        try {
            for (int i = 0; i < objs.length; i++) {
                flag = i + 1;
                Object obj = objs[i] == null ? "" : objs[i];
                // 获得参数的类型
                String paramType = obj.getClass().getName();
                if (Integer.class.getName().equals(paramType)) { // 判断是否是int类型
                    st.setInt(i + 1, (int) objs[i]);
                } else if (Double.class.getName().equals(paramType)) { // 判断是否是double类型
                    st.setDouble(i + 1, (double) objs[i]);
                } else if (String.class.getName().equals(paramType)) { // 判断是否是string类型
                    st.setString(i + 1, (String) objs[i]);
                } else {
                    st.setObject(i + 1, objs[i]);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LOG.error("Oracle QLException fial error");
        }
    }
    public  int doUpdate(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement stmt = null;
        int result = 0;
        try {
            conn = mysqlDataSource.getConnection();
            stmt = conn.prepareStatement(sql);
            setParams(stmt, params); //设置参数
        } catch (SQLException e) {
            e.printStackTrace();
            LOG.error("Oracle connect or prepareStatement init error");
        }

        try {
            // 执行sql，针对insert，delete，update，返回结果是受影响行数
            result = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            LOG.error("Oracle execute fial error");
        } finally {
            // 关闭连接
            if (stmt != null){
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }
        return result;
    }

    class Datasource {
        private  Datasource datasource;
        private  BasicDataSource ds;

        private Datasource() throws IOException, SQLException, PropertyVetoException {
            ds = new BasicDataSource();
            ds.setDriverClassName("com.mysql.jdbc.Driver");

//            String url = ResourceUtil.getString("mysql.jdbc");
//            String username = ResourceUtil.getString("mysql.username");
//            String password = ResourceUtil.getString("mysql.password");
            ds.setUrl(url);
            ds.setUsername(username);
            ds.setPassword(password);
            //the settings below are optional -- dbcp can work with defaults
            ds.setMinIdle(5);
            ds.setMaxIdle(20);
            ds.setMaxOpenPreparedStatements(180);
        }
        public  Datasource getInstance() throws IOException, SQLException, PropertyVetoException {
            if (datasource == null) {
                datasource = new Datasource();
                return datasource;
            } else {
                return datasource;
            }
        }
        public Connection getConnection() throws SQLException {
            return this.ds.getConnection();
        }
    }
}
