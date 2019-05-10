

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Slf4j
@Component
public class MysqlComponentUtils {
    private static List<String> metadata=null;
    @Autowired
    DataSource mysqlDataSource;
    public static void cleanAfterBatchQuery(){
        if (metadata!=null){
            metadata.clear();
            metadata=null;
        }
    }
    /**
     * @param sql
     * 返回单条结果，第一条
     * @return
     */
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
            results=new HashMap<>();
            if (rs.next()) { // 判断结果集是否还有数据（数据是一条记录的方式取出）
                for (int i = 1; i <= cloumCount; i++) {
                    // rsmd.getColumnName(i); // 表的字段名或字段别名
                    // rs.getObject(i); // 取到字段对应的值
                    results.put(rsmd.getColumnName(i).toLowerCase(), rs.getObject(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Executing sql {} error", sql, e);
        } finally {
            close(stmt);
            close(conn);
            close(rs);
        }
        return results;
    }


    /**
     * @param sql
     * 返回单条结果，多条
     * @return
     */
    public  List<Map<String, Object>> executeSqls(String sql) {
        Map<String,String> columnNameReflectMap=new HashMap<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        List<Map<String,Object>> listMap=null;

        ResultSet rs = null;
        try {
            conn = mysqlDataSource.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.execute();
            rs = stmt.executeQuery();
            // 元数据；对对象取到的结果集数据的描述

            ResultSetMetaData rsmd = rs.getMetaData();
            int cloumCount = rsmd.getColumnCount();
            for (int j=1;j<=cloumCount;++j){
                String columnName = rsmd.getColumnName(j);
                String reflectMapValue=reflectName(columnName);
                columnNameReflectMap.put(columnName,reflectMapValue);
            }

            listMap=new ArrayList<>();
            Map<String, Object> results = null;
            while (rs.next()) { // 判断结果集是否还有数据（数据是一条记录的方式取出）
                results=new HashMap<>();
                for (int i = 1; i <= cloumCount; i++) {
                    // rsmd.getColumnName(i); // 表的字段名或字段别名
                    // rs.getObject(i); // 取到字段对应的值
                    results.put(columnNameReflectMap.get(rsmd.getColumnName(i)), rs.getObject(i));
                }
                listMap.add(results);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Executing sql {} error", sql, e);
        } finally {
            close(stmt);
            close(conn);
            close(rs);
        }
        return listMap;
    }

    private String reflectName(String columnName) {
        if (!columnName.contains("_"))
            return columnName;
        String[] s = columnName.split("_");
        StringBuilder stringBuilder=new StringBuilder();
        for (String item:s){
            if (StringUtils.isEmpty(item))
                continue;
            if (item.length()==1)
                stringBuilder.append(item.toUpperCase());
            else {
                stringBuilder.append(item.substring(0,1).toUpperCase())
                        .append(item.substring(1));
            }
        }
        return stringBuilder.substring(0,1).toLowerCase()+stringBuilder.substring(1);
    }


    public static List<String> getMetadata(){
        if (metadata==null)
            throw new RuntimeException("metadata is null , this method should be invoked after executeSqlGetValues(String)");
        return MysqlUtils.metadata;
    }

    /**
     * @param sql
     * @return 返回多条row记录
     */
    public  List<List<String>> executeSqlGetValues(String sql) {
        Connection conn = null;
        PreparedStatement stmt = null;
        List<List<String>> results = null;
        ResultSet rs = null;
        try {
            conn = mysqlDataSource.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.execute();
            rs = stmt.executeQuery();
            // 元数据；对对象取到的结果集数据的描述
            ResultSetMetaData rsmd = rs.getMetaData();
            int cloumCount = rsmd.getColumnCount();
            if (metadata==null){
                metadata=new ArrayList<>();
                //最后一个column id 不要了，对方不解析
                for (int i = 1; i < cloumCount; i++) {
                    metadata.add(rsmd.getColumnName(i));
                }
            }
            results=new ArrayList<>();
            int maxId=0;
            while (rs.next()) { // 判断结果集是否还有数据（数据是所有记录的方式取出）
                List<String> rowValues= new ArrayList<>();
                //最后一个column id 不要了，对方不解析
                for (int i = 1; i < cloumCount; i++) {
                    rowValues.add(rs.getObject(i) == null ? "" : rs.getObject(i).toString() );
                }
                results.add(rowValues);
                int tmpId = rs.getInt(cloumCount);
                maxId = tmpId > maxId ? tmpId : maxId;
            }
            //每次最后一条是最大的ID。除去最后一次查询基本都是10001条数据
            results.add(Arrays.asList(maxId+""));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("!!!Executing sql {} error");
            log.error("Executing sql {} error", sql, e);
        } finally {
            close(stmt);
            close(conn);
            close(rs);
        }
        return results;
    }


    private static void close(AutoCloseable closeable){
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            closeable=null;
        }
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
            log.error("Oracle QLException fial error");
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
            log.error("Oracle connect or prepareStatement init error");
        }

        try {
            // 执行sql，针对insert，delete，update，返回结果是受影响行数
            result = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("Oracle execute fial error");
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




}
