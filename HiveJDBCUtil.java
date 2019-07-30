

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import cn.com.abc.bigdata3.constant.Constants;
import cn.com.abc.bigdata3.util.cfg.Parameter;

public class HiveJDBCUtil {
	
	private final static Logger LOG = Logger.getLogger(HiveJDBCUtil.class);
	private static Connection conn;
	private static Lock lock = new ReentrantLock(); 
	
	static{
		try {
			Class.forName(Constants.hiveDriver);
			LOG.info("加载hiveJdbcDriver成功");
		} catch (ClassNotFoundException e) {
			LOG.error("加载hiveJdbcDriver失败:"+e);
		}
	}
	
	public static Connection getHiveConnection() throws SQLException{
		if(conn == null || conn.isClosed()){
			lock.lock();
			if(conn == null || conn.isClosed()){
				conn = DriverManager.getConnection(
						Parameter.getPropertie("HIVE_JDBC_URL"),
						Parameter.getPropertie("HIVE_JDBC_USER"),
						Parameter.getPropertie("HIVE_JDBC_PASSWORD"));
			}
			lock.unlock();
		}
		
		return conn;
	}
	
	public static boolean executeSql(String sql){
		Connection hiveConn = null;
		Statement stmt = null;
		try {
			hiveConn = getHiveConnection();
			stmt = hiveConn.createStatement();
			LOG.info("Executing sql: "+sql);
			stmt.executeQuery(sql);
		} catch (Exception e) {
			LOG.error(e);
			return false;
		}
		return true;
	}
	
	
	public static boolean executeSqls(List<String> sqls){
		Connection hiveConn = null;
		Statement stmt = null;
		try {
			hiveConn = getHiveConnection();
			stmt = hiveConn.createStatement();
			for(String sql : sqls){
				LOG.info("Executing sql: "+sql);
				stmt.executeQuery(sql);
			}
		} catch (Exception e) {
			LOG.error(e);
			return false;
		}
		return true;
	}
	
	/**
	 *  往hive表里添加表分区.
	 * @param hdfsBasePath 
	 * @param dbTableName
	 * @param performDateStr
	 * @param isZeroDate  创建的分区日期是否是 01 这种 日期
	 * @throws ParseException
	 */
	public static void addPartition(String hdfsBasePath, String dbTableName, String performDateStr, boolean isZeroDate){
		// 生成hive数据库连接
		Connection conn = null;
		Statement stmt = null;
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(Constants.yyyyMMdd.parse(performDateStr));
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH) + 1;
			int day = c.get(Calendar.DAY_OF_MONTH);
			
			conn = HiveJDBCUtil.getHiveConnection();
			stmt = conn.createStatement();
			
			String sql = null;
			String locationPath = null;
			
			String[] dbTableArray = dbTableName.split("\\.");
			if(dbTableArray.length == 2){
				String tableName = dbTableArray[1];
				
				if(isZeroDate){
					locationPath = hdfsBasePath+"/"+tableName 
							+ "/year="+ year 
							+ "/month="+ (month< 10 ? "0"+month : month)
							+ "/day="+ (day< 10 ? "0"+day : day);
				}else{
					locationPath = hdfsBasePath+"/"+tableName 
							+ "/year="+ year 
							+ "/month="+ month
							+ "/day="+ day;
				}
				
				//模板sql
				String templateSql = "ALTER TABLE #DBTABLENAME# ADD IF NOT EXISTS PARTITION("
						+ "year=" + year
						+ ",month=" + month
						+ ",day=" + day
						+ ") LOCATION '#LOCATIONPATH#' ";
				
				String dropPartitionSql = "alter table "+dbTableName+" drop partition(year="+year+",month="+month+",day="+day+")";
				LOG.info("drop partition sql: " + dropPartitionSql);
				
				sql = templateSql.replace("#DBTABLENAME#", dbTableName).replace("#LOCATIONPATH#", locationPath);
				LOG.info("add partition sql: " + sql);
				
				stmt.execute(dropPartitionSql);
				stmt.execute(sql);
				LOG.info("add partition ok.");
				
			}else{
				LOG.warn("add partition args error.");
				throw new RuntimeException("add partition args error.");
			}
				
		} catch (Exception e) {
			LOG.error("add partition error." + e);
			throw new RuntimeException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOG.error(e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.error(e);
				}
			}
		}
	}
	
	/**
	 *  hive表里删除表分区.
	 * @param dbTableName
	 * @param performDateStr
	 */
	public static void dropPartition(String dbTableName, String performDateStr){
		// 生成hive数据库连接
		Connection conn = null;
		Statement stmt = null;
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(Constants.yyyyMMdd.parse(performDateStr));
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH) + 1;
			int day = c.get(Calendar.DAY_OF_MONTH);
			
			conn = HiveJDBCUtil.getHiveConnection();
			stmt = conn.createStatement();
			
			//alter table 9f_database.delegation_payment drop partition(year=2017,month=1,day=10)
			String sql = null;
			String[] dbTableArray = dbTableName.split("\\.");
			if(dbTableArray.length == 2){
				//模板sql
				String templateSql = "ALTER TABLE #DBTABLENAME# DROP PARTITION("
						+ "year=" + year
						+ ",month=" + month
						+ ",day=" + day
						+ ")";
				
				sql = templateSql.replace("#DBTABLENAME#", dbTableName);
				LOG.info("drop partition sql: " + sql);
				
				stmt.execute(sql);
				LOG.info("drop partition ok.");
				
			}else{
				LOG.warn("drop partition args error.");
				throw new RuntimeException("drop partition args error.");
			}
				
		} catch (Exception e) {
			LOG.error("drop partition error." + e);
			throw new RuntimeException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOG.error(e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.error(e);
				}
			}
		}
	}
	
	/**
	 *  hive表里删除表分区.
	 * @param dbTableName
	 * @param performDateStr
	 */
	public static void dropMonthPartition(String dbTableName, String performDateStr){
		// 生成hive数据库连接
		Connection conn = null;
		Statement stmt = null;
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(Constants.yyyyMMdd.parse(performDateStr));
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH) + 1;
			//int day = c.get(Calendar.DAY_OF_MONTH);
			
			conn = HiveJDBCUtil.getHiveConnection();
			stmt = conn.createStatement();
			
			//alter table 9f_database.delegation_payment drop partition(year=2017,month=1)
			String sql = null;
			String[] dbTableArray = dbTableName.split("\\.");
			if(dbTableArray.length == 2){
				//模板sql
				String templateSql = "ALTER TABLE #DBTABLENAME# DROP PARTITION("
						+ "year=" + year
						+ ",month=" + month
						+ ")";
				
				sql = templateSql.replace("#DBTABLENAME#", dbTableName);
				LOG.info("drop partition sql: " + sql);
				
				stmt.execute(sql);
				LOG.info("drop partition ok.");
				
			}else{
				LOG.warn("drop partition args error.");
				throw new RuntimeException("drop partition args error.");
			}
				
		} catch (Exception e) {
			LOG.error("drop partition error." + e);
			throw new RuntimeException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOG.error(e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.error(e);
				}
			}
		}
	}
	
	/**
	 *  往hive表里添加表 到月的 分区.
	 * @param hdfsBasePath 
	 * @param dbTableName
	 * @param performDateStr
	 * @param isZeroDate  创建的分区日期是否是 01 这种 日期
	 * @throws ParseException
	 */
	public static void addMonthPartition(String hdfsBasePath, String dbTableName, String performDateStr, boolean isZeroDate){
		// 生成hive数据库连接
		Connection conn = null;
		Statement stmt = null;
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(Constants.yyyyMMdd.parse(performDateStr));
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH) + 1;
			//int day = c.get(Calendar.DAY_OF_MONTH);
			
			conn = HiveJDBCUtil.getHiveConnection();
			stmt = conn.createStatement();
			
			String sql = null;
			String locationPath = null;
			
			String[] dbTableArray = dbTableName.split("\\.");
			if(dbTableArray.length == 2){
				String tableName = dbTableArray[1];
				
				if(isZeroDate){
					locationPath = hdfsBasePath+"/"+tableName 
							+ "/year="+ year 
							+ "/month="+ (month< 10 ? "0"+month : month);
				}else{
					locationPath = hdfsBasePath+"/"+tableName 
							+ "/year="+ year 
							+ "/month="+ month;
				}
				
				//模板sql
				String templateSql = "ALTER TABLE #DBTABLENAME# ADD IF NOT EXISTS PARTITION("
						+ "year=" + year
						+ ",month=" + month
						+ ") LOCATION '#LOCATIONPATH#' ";
				
				String dropPartitionSql = "alter table "+dbTableName+" drop partition(year="+year+",month="+month+")";
				LOG.info("drop partition sql: " + dropPartitionSql);
				
				sql = templateSql.replace("#DBTABLENAME#", dbTableName).replace("#LOCATIONPATH#", locationPath);
				LOG.info("add partition sql: " + sql);
				
				stmt.execute(dropPartitionSql);
				stmt.execute(sql);
				LOG.info("add partition ok.");
				
			}else{
				LOG.warn("add partition args error.");
				throw new RuntimeException("add partition args error.");
			}
				
		} catch (Exception e) {
			LOG.error("add partition error." + e);
			throw new RuntimeException(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOG.error(e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.error(e);
				}
			}
		}
	}
	
}
