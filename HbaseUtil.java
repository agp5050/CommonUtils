
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.RegionSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HbaseUtil {
	private static Logger LOG = LoggerFactory.getLogger(HbaseUtil.class);
	private static Configuration conf = null;
	private static Connection conn = null;
	private static ReadWriteLock rwlock = new ReentrantReadWriteLock();
	private static Lock wlock = rwlock.writeLock();

	public static Connection getConnection() {
		if (conn == null || conn.isClosed()) {
			try {
				wlock.lock();
				if (conn == null || conn.isClosed()) {
					conf = getHBaseConfig();
					conn = ConnectionFactory.createConnection(conf);
					LOG.info("HBase connection is created:::");
				}
			} catch (IOException e) {
				LOG.error("HBase create conn error...\n{}", e);
			} finally {
				wlock.unlock();
			}

		}
		return conn;
	}

	public static Configuration getHBaseConfig() {
		if (conf == null) {
			try {
				wlock.lock();
				if (conf == null) {
					conf = HBaseConfiguration.create();
//					conf.set("hbase.zookeeper.quorum", StreamingJob.hbaseZookeeper);
					conf.set("hbase.zookeeper.quorum", ResourceUtil.getString("hbase.zookeeper.quorum.online"));

				}
			} catch (Exception e) {
				LOG.error("Getting config error:", e);
			} finally {
				wlock.unlock();
			}
		}
		return conf;
	}

	public static boolean closeConnection() {
		if (conn != null && !conn.isClosed()) {
			try {
				conn.close();
				LOG.info("hbase connection close success:::");
				return true;//
			} catch (IOException e) {
				LOG.error("hbase connection close error:::", e);
				return false;
			}
		}
		return true;
	}

	/**
	 * synchronized can only solve concurrency at thread level, in distributed
	 * system multiple processes unavoidably will face concurrent operation, to
	 * avoid multiple processed concurrency you have to implement distributed
	 * lock via zookeeper e.g.
	 *
	 * @param tableName
	 * @param family
	 */
	synchronized public static void createTable(String tableName, String family) {
		try {
			Admin admin = getConnection().getAdmin();
			if (!admin.tableExists(TableName.valueOf(tableName))) {
				HTableDescriptor tableDescriptor = new HTableDescriptor(
						TableName.valueOf(tableName));
				HColumnDescriptor columnDescriptor = new HColumnDescriptor(
						family);
				columnDescriptor
						.setCompressionType(Compression.Algorithm.SNAPPY);
				tableDescriptor.addFamily(columnDescriptor);

				byte[][] splits = new RegionSplitter.HexStringSplit()
						.split(3);

				admin.createTable(tableDescriptor, splits);
			}
		} catch (Exception e) {
			LOG.error("Creating table {} error ", tableName, e);
			throw new GlobalCustomException(GlobalConf.ErrorType.UNKNOW_ERROR,
					e);
		}
	}

	public static void insertOrUpdate(String tableName, String rowKey,
									  String family, Map<String, String> data) {
		Connection conn = getConnection();
		Table table = null;
		try {
			table = conn.getTable(TableName.valueOf(tableName));
			Put put = new Put(Bytes.toBytes(rowKey));
			for (String key : data.keySet()) {
				String value = data.get(key);
				if (value == null) {
					continue;
				} else {
					put.addColumn(Bytes.toBytes(family), Bytes.toBytes(key),
							Bytes.toBytes(value));
				}
			}
			if (put.size() > 0) {
				table.put(put);
				LOG.debug("Table {} insert or update with rowkey {} succeeded",
						tableName, rowKey);
			}
		} catch (TableNotFoundException | RetriesExhaustedWithDetailsException e) {
			// If table not exists, create it
			LOG.warn("Table {} not found, it will be created soon.", tableName);
			createTable(tableName, family);
			// run the method again
			insertOrUpdate(tableName, rowKey, family, data);
		} catch (Exception e) {
			LOG.error("Insert or update data error, table: {} ,pk :{}",
					tableName, rowKey);
			throw new GlobalCustomException(GlobalConf.ErrorType.UNKNOW_ERROR,
					e);
		} finally {
			if (table != null) {
				try {
					table.close();
				} catch (IOException e) {
					LOG.error("Closing table {} error ", tableName, e);
					throw new GlobalCustomException(
							GlobalConf.ErrorType.UNKNOW_ERROR, e);
				}
			}
		}
//		System.out.println("insertOrUpdate is successfull");
	}

	public static void deleteRowByRowkey(String tableName, String rowKey) {
		Connection conn = getConnection();
		Table table = null;
		try {
			table = conn.getTable(TableName.valueOf(tableName));
			Delete del = new Delete(Bytes.toBytes(rowKey));
			table.delete(del);
			LOG.debug("Table {} delete with rowkey {} succeeded", tableName,
					rowKey);

		} catch (TableNotFoundException | RetriesExhaustedWithDetailsException e) {
			// If table not exists, create it
			LOG.warn("Table {} not found, it will be created soon.", tableName);
			createTable(tableName,"f");
			// run the method again
			deleteRowByRowkey(tableName, rowKey);
		} catch (Exception e) {
			LOG.error("Deleting data error, table: {} ,pk :{}", tableName,
					rowKey);
			throw new GlobalCustomException(GlobalConf.ErrorType.UNKNOW_ERROR,
					e);
		} finally {
			if (table != null) {
				try {
					table.close();
				} catch (Exception e) {
					LOG.error("Closing table {} error ", tableName, e);
					throw new GlobalCustomException(
							GlobalConf.ErrorType.UNKNOW_ERROR, e);
				}
			}
		}

	}

	/**
	 * 根据rowkey前缀取数据
	 * @param tableName
	 * @param rowKeyLike
	 * @param arr	List<String>,String格式：family,qualifier
	 * @return
	 */
	public static List<Result> getRowsByPrefix(String tableName,
                                               String rowKeyLike, List<String> arr) {
		// TODO Auto-generated method stub
		Connection conn = getConnection();
		Table table = null;
		List<Result> list = new ArrayList<Result>();
		try {
			table = conn.getTable(TableName.valueOf(tableName));
			PrefixFilter filter = new PrefixFilter(rowKeyLike.getBytes());
			Scan scan = new Scan();
			scan.setFilter(filter);
			for (String v : arr) {
				String[] s = v.split(",");
				scan.addColumn(Bytes.toBytes(s[0]), Bytes.toBytes(s[1]));
			}

			ResultScanner scanner = table.getScanner(scan);
			for (Result result : scanner) {
				list.add(result);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				table.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	/**
	 * 根据rowkey前缀取数据,family默认为f
	 * @param tableName
	 * @param rowKeyLike
	 * @param qualifierList	List<String>,String格式：qualifier
	 * @return
	 */
	public static List<Map<String,String>> getRowsByPrefixFamilyF(String tableName,
											   String rowKeyLike, List<String> qualifierList) {
		// TODO Auto-generated method stub
		List<Map<String,String>> resultList = new ArrayList<>();
		Connection conn = getConnection();
		Table table = null;
//		List<Result> list = new ArrayList<Result>();
		try {
			table = conn.getTable(TableName.valueOf(tableName));
			PrefixFilter filter = new PrefixFilter(rowKeyLike.getBytes());
			Scan scan = new Scan();
			scan.setFilter(filter);
			for (String v : qualifierList) {
//				String[] s = v.split(",");
				scan.addColumn(Bytes.toBytes("f"), Bytes.toBytes(v));
			}

			ResultScanner scanner = table.getScanner(scan);
			for (Result result : scanner) {
				Map<String,String> resultMap = new HashMap<>();
				Cell[] cells = result.rawCells();

				for (Cell cell : cells) {
					resultMap.put(Bytes.toString(CellUtil.cloneQualifier(cell))
							, Bytes.toString(CellUtil.cloneValue(cell)));
				}
				LOG.info("add rowKey begin");
				resultMap.put("rowKey",Bytes.toString(result.getRow()));
				LOG.info("add rowKey end");
				resultList.add(resultMap);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				table.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultList;
	}



	@SuppressWarnings("deprecation")
	public static Map<String, String> getRowsByRowKey(String tableName,
													  String rowKey) {
		// TODO Auto-generated method stub
		Connection conn = getConnection();
		Table table = null;
		Map<String, String> map = new HashMap<String, String>();
		try {
			table = conn.getTable(TableName.valueOf(tableName));
			Get get = new Get(rowKey.getBytes());// 根据rowkey查询
			Result r = table.get(get);
			// System.out.println("获得到rowkey:" + new String(r.getRow()));

			Cell[] cells = r.rawCells();

			for (Cell cell : cells) {
				//错误写法，会乱码
				map.put(Bytes.toString(CellUtil.cloneQualifier(cell))
						, Bytes.toString(CellUtil.cloneValue(cell)));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				table.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return map;
	}

	public static Map<String, Map<String, String>> getDataBatch(String tableName, List<String> rowKeyList) {
		Map<String, Map<String, String>> result = new HashMap<>();
		try {
			result = gets(tableName,rowKeyList,"f",new ArrayList<>());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}




	/**
	 * 通过rowKey前缀集合获取数据，Map中包含rowKey，key=rowKey
	 * @param tableName
	 * @param rowKeyPrefList
	 * @param qualifierList
	 * @return
	 */
	public static List<Map<String, String>> getByRowKeyPrefixBatch(String tableName, List<String> rowKeyPrefList, List<String> qualifierList) {
		List<Map<String, String>> result = new ArrayList<>();
		for(String rowKeyPre : rowKeyPrefList){
			Collection<HashMap<String, String>> tmp = null;
			try {
				tmp = scan(tableName, rowKeyPre, "f", qualifierList).values();
				for(HashMap<String, String> map : tmp){
                    result.add(map);
                }
				result.addAll(tmp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static HashMap<String, HashMap<String, String>> scan(String tableName, String prefix, String family, List<String> qualifiers) throws IOException {
		HashMap<String, HashMap<String, String>> maps = new HashMap<>();
		Connection conn = getConnection();
		Table table = null;
		try {
			table = conn.getTable(TableName.valueOf(tableName));
			/**设置操作超时时间，防止线程被阻塞*/
//            System.out.println("opTime="+table.getOperationTimeout());
            table.setOperationTimeout(10000);
//            System.out.println("opTime="+table.getOperationTimeout());
			Scan scan = new Scan();
			if (qualifiers != null && qualifiers.size() > 0) {
				for (String qualifier :
						qualifiers) {
					scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier));
				}
			} else {
				scan.addFamily(Bytes.toBytes(family));
			}
//			Thread.sleep(6000);
			scan.setRowPrefixFilter(Bytes.toBytes(prefix));
			try (ResultScanner resultScanner = table.getScanner(scan)) {
				Result rs = resultScanner.next();
				for (; rs != null; rs = resultScanner.next()) {
					HashMap<String, String> map = new HashMap<>();
					String key = null;
					for (Cell cell :
							rs.rawCells()) {
						map.put(Bytes.toString(CellUtil.cloneQualifier(cell))
								, Bytes.toString(CellUtil.cloneValue(cell)));
						key = Bytes.toString(CellUtil.cloneRow(cell));
					}
					map.put("rowKey",key);
					maps.put(key, map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				table.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return maps;
	}


	public static Map<String, Map<String, String>> gets(String tableName, List<String> rowKeys, String family, List<String> qualifiers) throws IOException {
		Map<String, Map<String, String>> maps = new HashMap<>();
		Connection conn = getConnection();
		Table table = null;
		try {
			table = conn.getTable(TableName.valueOf(tableName));
			table.setOperationTimeout(10000);
			List<Get> gets = new LinkedList<>();
			for (String rowKey :
					rowKeys) {
				gets.add(buildGet(rowKey, family, qualifiers));
			}
			Result[] results = table.get(gets);

			for (Result result :
					results) {
				HashMap<String, String> map = new HashMap<>();
				String key = null;
				for (Cell cell :
						result.rawCells()) {
					map.put(Bytes.toString(CellUtil.cloneQualifier(cell))
							, Bytes.toString(CellUtil.cloneValue(cell)));
					key = Bytes.toString(CellUtil.cloneRow(cell));
				}
				maps.put(key, map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				table.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return maps;
	}

	private static Get buildGet(String rowKey, String family, List<String> qualifiers) {
		Get get = new Get(Bytes.toBytes(rowKey));
		for (String qualifier :
				qualifiers) {
			if (!StringUtil.isBlank(qualifier)) {
				get.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier));
			}
		}
		return get;
	}

//	private static byte[] toBytes(Object para){
//		
//	}

	public static void main(String[] args) throws Exception {
//		System.out.println(HbaseUtil.getRowsByPrefixFamilyF("test_hbase","12345678",new ArrayList<>()));
        long start = System.currentTimeMillis();
        System.out.println(HbaseUtil.scan("tob_multi_head","f", "f",new ArrayList()));
        System.out.println(DateUtils.getTimeInterval(start));
	}
}
