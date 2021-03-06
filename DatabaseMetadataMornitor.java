
import com.alibaba.fastjson.JSON;
import util.HttpClientUtils;
import util.MysqlUtils;

import javax.servlet.ServletException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class DatabaseMetadataMornitor {
    Connection conn = null;
    Statement st = null;
    public static Map<String, Map<String,String>> tableColumns=new HashMap<>();
    public static final String tableStyle="<table border=\"1\" cellspacing=\"0\" ><div>";
    //获取conn
    public void init() throws ServletException {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = java.sql.DriverManager.getConnection("jdbc:mysql://192.168.55.225:3306/bd_aidp_db?useSSL=false", "dbp_rw", "dbp123");
            System.out.println(conn.getCatalog());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Map<String, Map<String,String>> doGet() {
        Map<String, Map<String,String>> tmpTableColumns=new HashMap<>();
        try {
            this.conn=MysqlUtils.getConnection();
            DatabaseMetaData dbMetaData = conn.getMetaData();
            ResultSet rs = dbMetaData.getTables(null, null, null,new String[] { "TABLE" });
            while (rs.next()) {// ///TABLE_TYPE/REMARKS
                String tableName = rs.getString("TABLE_NAME");
                String sql = "select * from " + tableName;
                try {
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs2 = ps.executeQuery();
                    ResultSetMetaData meta = rs2.getMetaData();
                    int columeCount = meta.getColumnCount();
                    Map<String,String> columnNameType = new HashMap();
                    for (int i = 1; i < columeCount + 1; i++) {
                        String columnName = meta.getColumnName(i);
                        String columnTypeName = meta.getColumnTypeName(i);
                        columnNameType.put(columnName,columnTypeName);
                    }
                    tmpTableColumns.put(tableName,columnNameType);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(JSON.toJSONString(tmpTableColumns));
        return tmpTableColumns;

    }

    public String doCompare(Map<String, Map<String,String>> lastMap,Map<String, Map<String,String>> newMap){
        //check if tables altered!!
        StringBuffer context=new StringBuffer();

        Set<String> oldTables=new HashSet();
        oldTables.addAll(lastMap.keySet());

        Set<String> newTables=new HashSet();
        newTables.addAll(newMap.keySet());

        oldTables.removeAll(newTables);
        //是否删除了表
        context.append(tableStyle);
        if (!oldTables.isEmpty()){
            System.out.println("oldTables"+JSON.toJSONString(oldTables));
            context.append("<tr><th>距上次扫描被删除的表格</th></tr>");
            for (String item:oldTables){
                context.append("<tr><td align=\"center\">");
                context.append(item);
                context.append("</td></tr>");
            }
        }
        //是否新建了表
       newTables.removeAll(lastMap.keySet());
        if (!newTables.isEmpty()){
            System.out.println("newTables"+JSON.toJSONString(newTables));
            context.append("<tr><th>距上次扫描新增的表格</th></tr>");
            for (String item:newTables){
                context.append("<tr><td align=\"center\">");
                context.append(item);
                context.append("</td></tr>");
            }
        }

        //check if column altered !!

        Set<String> keySet = lastMap.keySet();
        keySet.retainAll(newMap.keySet());
        for (String item:keySet){
            Map<String,String> oldColumnTypes = lastMap.get(item);
            Map<String,String> newColumnTypes = newMap.get(item);
           if (Objects.equals(oldColumnTypes,newColumnTypes))
               continue;
           else{
               compareColumns(oldColumnTypes,newColumnTypes,context,item);
           }
        }



        return context.toString();
    }

    private void compareColumns(Map<String, String> oldColumnTypes, Map<String, String> newColumnTypes, StringBuffer context,String tableName) {
        Set<String> keySetOld = new HashSet<>();
        keySetOld.addAll(oldColumnTypes.keySet());
        keySetOld.removeAll(newColumnTypes.keySet());
        context.append("<tr><th>距上次扫描表");
        context.append(tableName);
        context.append("变更字段信息</th></tr>");
        if (!keySetOld.isEmpty()){
            context.append("<tr><th>被删除的字段</th></tr>");
            for (String item:keySetOld){
                context.append("<tr><td align=\"center\">");
                context.append(item);
                context.append("</td></tr>");
            }
        }
        Set<String> keySetNew = new HashSet<>();
        keySetNew.addAll(newColumnTypes.keySet());
        keySetNew.removeAll(oldColumnTypes.keySet());
        if (!keySetNew.isEmpty()){
            context.append("<tr><th>新增的字段</th></tr>");
            for (String item:keySetNew){
                context.append("<tr><td align=\"center\">");
                context.append(item);
                context.append("</td></tr>");
            }
        }

        //共同的字段，但是type变更了

       Set<String> tmpSet=new HashSet<>();
        tmpSet.addAll(oldColumnTypes.keySet());
        tmpSet.retainAll(newColumnTypes.keySet());
        StringBuffer stringBufferTmp=new StringBuffer("<tr><th>变更的字段</th><th>旧类型</th><th>新类型</th></tr>");
        boolean exists=false;
        for (String item:tmpSet){
            String oldType = oldColumnTypes.get(item);
            String newType = newColumnTypes.get(item);
            //|| ("Double".equals(oldType) && "BigDecimal".equals(newType)) || ("Double".equals(newType) && "BigDecimal".equals(oldType))  Mapper强制转换后需要filter这个。
            if (Objects.equals(oldType,newType) )
                continue;
            else {
                exists=true;
                stringBufferTmp.append("<tr><td align=\"center\">");
                stringBufferTmp.append(item);
                stringBufferTmp.append("</td>");
                stringBufferTmp.append("<td align=\"center\">");
                stringBufferTmp.append(oldType);
                stringBufferTmp.append("</td>");
                stringBufferTmp.append("<td align=\"center\">");
                stringBufferTmp.append(newType);
                stringBufferTmp.append("</td></tr>");

            }
        }
        if (exists){
            context.append(stringBufferTmp);
        }




    }


    public static void main(String[] args) {
        try {
            DatabaseMetadataMornitor databaseMetadataMornitor = new DatabaseMetadataMornitor();
            databaseMetadataMornitor.tableColumns=databaseMetadataMornitor.doGet();
            while (true){
                Thread.sleep(20000);
                Map<String, Map<String, String>> stringMapMap = databaseMetadataMornitor.doGet();
                String s = databaseMetadataMornitor.doCompare(databaseMetadataMornitor.tableColumns, stringMapMap);
                if (!s.equals(tableStyle)){
                    String to="aaaa@9abc.com.cn";
                    String subject="Mysql监控";
                    String url="http://127.0.0.1:9025/com/abc/ai/sendmail?to=%s&subject=%s&context=%s";
                    String newS = URLEncoder.encode(s, "utf-8");
                    String newUrl = String.format(url, to, subject, newS);
                    System.out.println(newUrl);
                    HttpClientUtils.doGet(newUrl);

                }
                System.out.println(s);
                DatabaseMetadataMornitor.tableColumns=stringMapMap;

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
