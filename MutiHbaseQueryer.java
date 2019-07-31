

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.abc.bigdata.model.ApolloCorelationInit;
import com.abc.bigdata.monitor.MonitorUtil;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.ReadOptions;
import org.iq80.leveldb.Snapshot;
import  org.iq80.leveldb.impl.Iq80DBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.abc.bigdata.dc.common.thread.TaskQueue;
import com.abc.bigdata.dc.common.thread.ThreadPoolExecutor;
import com.abc.bigdata.dc.common.util.ByteUtils;
import com.abc.bigdata.model.MessageModel;
import com.abc.bigdata.prometheus.util.PrometheusUtil;
import com.abc.bigdata.service.CrawlerServiceNew;
import com.abc.bigdata.task.QuartzTaskOnAll;
import com.abc.bigdata.utils.FileUtil;
import com.abc.bigdata.utils.HttpClient;
import com.abc.bigdata.utils.JackSonUtil;
import com.abc.bigdata.utils.ResourceUtil;

import static com.abc.bigdata.monitor.MonitorUtil.asyncMonitorTmpContainer;
import static com.abc.bigdata.monitor.MonitorUtil.enableMonitor;


public class MutiHbaseQueryer implements ApolloCorelationInit {
	private static final Logger logger = LoggerFactory.getLogger(MutiHbaseQueryer.class);
	private static final Map<String, ExecutorService> TABLEMAP = new ConcurrentHashMap<>();
	public static final Set<String> ERRORCLUSTER = new HashSet<>();

	public static final String SPLITKEY = "~jfdc~";
	private static List<MutiHbaseClient> mhcList = new ArrayList<>();

	private static Map<String, MutiHbaseClient> clientMap = new ConcurrentHashMap<>();

	private static LinkedBlockingQueue<String> QUEUE = new LinkedBlockingQueue(500000);
	private static String errorInfoFilePath = "";

	private static  MutiHbaseQueryer mutiHbaseQueryer=new MutiHbaseQueryer();

	/**
	 * Controller异步存储到队列
	 * @param tableName
	 * @param transNo
	 */
	public static void addToQueue(String tableName, String transNo) {
		String newKey = tableName + SPLITKEY + transNo;
		MutiLevelDB.writeKey(newKey);
		QUEUE.offer(newKey);
		logger.info("after add queue" + newKey);
	}

	/**
	 * mapdb 轮询放入队列
	 * @param tableName
	 * @param transNo
	 */
	public static void addToQueueWithout(String tableName, String transNo) {
		String newKey = tableName + SPLITKEY + transNo;
		QUEUE.offer(newKey);
	}

	static {
		initConfig();
		ResourceUtil.addToWatch(mutiHbaseQueryer);
	}
	static void reInitConfig(){
		mhcList.clear();
		clientMap.clear();
		initConfig();
		logger.warn("{} is re-Initiate after apollo config changed",MutiHbaseQueryer.class.getName());
	}
	static void initConfig(){
		Set<Object> keys = ResourceUtil.getProperties().keySet();// 返回属性key的集合
        logger.warn(JSON.toJSONString(keys));
		for (Object key : keys) {
			if (key.toString().startsWith("cluster.hbase")) {
				MutiHbaseClient client = new MutiHbaseClient(key.toString());
				mhcList.add(client);
				clientMap.put(key.toString(), client);
			}
		}
		if (mhcList.size() == 0) {
			logger.error("hbase client size is 0,please check your config file of cluster.hbase configuration");
			System.exit(-1);
		}
	}

	// 后台线程 轮询queue
	private static void initBackupThread() {
		Thread tt = new Thread("tuple_queue") {
			public void run() {
				while (true) {
					try {
						String newKey = QUEUE.take();
						logger.info("newkey=" + newKey);
						String[] arr = newKey.split(SPLITKEY);
						processData(arr[0], arr[1]);
					} catch (Throwable e) {
						logger.error("initBackupThread " + e.getMessage(), e);
					}
				}
			}
		};
		tt.start();

		if (System.getProperty("error.file") != null) {
			errorInfoFilePath = System.getProperty("error.file");
		} else {
			errorInfoFilePath = new File("").getAbsolutePath();
		}

		// 轮询检测mapdb
		Thread mapdb = new Thread("mapdb_loop") {
			public void run() {
				reCheckMapDB(false);
				while (true) {
					reCheckMapDB(true);
				}
			}
		};
		mapdb.start();
	}

	/**
	 * 定时 重新检测mapdb
	 */
	private static void reCheckMapDB(boolean loop) {
		Set<String> valList = new HashSet<>();
		List<String> list = Arrays.asList(QUEUE.toArray(new String[QUEUE.size()]));
		Snapshot snapshot =  MutiLevelDB.db.getSnapshot();
		ReadOptions readOptions = new ReadOptions();  
		readOptions.fillCache(false);//遍历中swap出来的数据，不应该保存在memtable中。  
		readOptions.snapshot(snapshot);//默认snapshot为当前。  
		  DBIterator dbit;
		  try {
				dbit =  MutiLevelDB.db.iterator(readOptions);
				int size=0;
				   while(dbit.hasNext() ) {
					   Entry<byte[], byte[]> en=dbit.next();
					   String key=new String(en.getKey());
					   Long ts = System.currentTimeMillis() - ByteUtils.transformBytestoLong(en.getValue());
					   size++;
					   if (!loop || (ts > 100 * 1000 && ts < 300 * 1000 && !list.contains(key))) {// 100s--300s
							valList.add(key);
						} else if (ts > 300 * 1000) {
							FileUtil.saveToLocal(key, errorInfoFilePath, "mapdbErrorData2.data");
							MutiLevelDB.removeKey(en.getKey());
						}
				   }
				   logger.error("mutihbase leveldb size:"+size);
				   dbit.close();//must be
		  }catch (Throwable e) {
		}   
				   
				
		  try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
			}
			int ind = 0;
			for (String s : valList) {
				if (MutiLevelDB.db.get(s.getBytes()) != null) {
					ind++;
					String[] arr = s.split(SPLITKEY);
					addToQueueWithout(arr[0], arr[1]);
				}
				if (ind % 300 == 0) {
					try {
						Thread.sleep(10 * 1000);
					} catch (InterruptedException e) {
					}
				}
			}
			long sleepTime = 3 * 60 * 1000l - (ind / 300) * 10 * 1000l;
			sleepTime = sleepTime <= 0l ? 10l : sleepTime;
			try {
				Thread.sleep(sleepTime);// 每隔3分钟进行检测
			} catch (InterruptedException e) {
			}
	}

	/**
	 * 查询hbase 处理数据过程
	 * @param tableName
	 * @param transNo
	 */
	public static void processData(String tableName, String transNo) {
		String cacheKey=tableName+":"+transNo;
		logger.info("tableName=" + tableName + ",transNo=" + transNo);
		ExecutorService cs = getCompletionService(tableName);
		String newKey = tableName + SPLITKEY + transNo;
		try {
			String original_data = (String) cs.submit(() -> query(tableName, transNo)).get();
			if(null==original_data) {
				PrometheusUtil.counterStatisByLabel("query_hbase_none", "table", tableName);
			}
			if (null != original_data) {
				original_data = null;
				MutiLevelDB.removeKey(newKey.getBytes());
			} else if (QuartzTaskOnAll.apiKeyToServiceType.get(tableName) == null) {
				logger.warn("apiKey=" + tableName + ",taskKey=" + transNo + "不在爬虫接口查询范围内");
				MutiLevelDB.removeKey(newKey.getBytes());
			} else {
				try {
					MessageModel message = new MessageModel();
					String product = QuartzTaskOnAll.apiKeyToServiceType.get(tableName);
					message.setServiceType(product);
					message.setTransNo(transNo);
					message.setSource("FK");
					message.setTopic(QuartzTaskOnAll.apiKeyTopic.get(tableName));
					CrawlerServiceNew.queryFromCrawler(message,false);
					logger.warn(tableName + "query from 爬虫" + transNo);
				} catch (Throwable e) {
					PrometheusUtil.counterStatisByLabel("exception_fk", "fk_resend",
							com.abc.bigdata.dc.common.util.StringUtils.subString(e.getMessage(), 30));
					logger.error("processData 风控补数接口异常：", e.getMessage());
				}
			}
		} catch (Exception e) {
			logger.error("processData 风控补数接口异常2：", e.getMessage());
		}finally {
			//start added by agp , used for data-link-monitor
			try {
				asyncMonitorTmpContainer.remove(cacheKey);
			}catch (NullPointerException e){
			}
			//end added by agp, used for data-link-monitor
		}
	}

	/**
	 * 初始化MAPDB
	 * 
	 * @param file
	 */
	public static void initDB(String file) {
		try {
			MutiLevelDB.initDb(file);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		initBackupThread();
		logger.info("start muti initDB");
	}

	/**
	 * 根据表名和transNo查询
	 * @param tableName
	 * @param transno
	 * @return
	 */
	public static String query(final String tableName, String transno) {
        String cacheKey=tableName+":"+transno;
		long st = System.currentTimeMillis();
		MutiHbaseClient mhc = clientMap.get("cluster.hbase.ali");
		String res = null;
		if (mhc != null) {
			try {
				res = mhc.querySingle(tableName, transno);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				PrometheusUtil.counterStatisByLabel("exception_fk", "label", "alihbase");
			}
		}
		if (res == null && clientMap.get("cluster.hbase.2") != null) {
			try {
				res = clientMap.get("cluster.hbase.2").querySingle(tableName, transno);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				PrometheusUtil.counterStatisByLabel("exception_fk", "label", "2hbase");
			}
		}
		if (res == null && clientMap.get("cluster.hbase.182") != null) {
			try {
				res = clientMap.get("cluster.hbase.182").querySingle(tableName, transno);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				PrometheusUtil.counterStatisByLabel("exception_fk", "label", "182hbase");
			}
		}
		long spendTime = System.currentTimeMillis() - st;
		PrometheusUtil.getHistogram("delay_min", "table").labels(tableName).observe(spendTime / 1000.0);
		logger.info("final cost time:" + spendTime+","+transno);

		if (null != res && res.trim().length() > 0) {
			Map<String, Object> logAgentData = new HashMap<>(2);
			logAgentData.put("topic", "credit_resend_riskpartment");
			logAgentData.put("msg", JackSonUtil.readValueAsObjFromStr(res, Map.class));
			String pose = "";
			try {
			    //start added by agp , used for data-link-monitor
                if (enableMonitor){
                    if (asyncMonitorTmpContainer.containsKey(cacheKey)){
                        MonitorUtil.putMonitorParam(asyncMonitorTmpContainer.getOrDefault(cacheKey,null),transno,tableName,logAgentData);
                    }else {
                        MonitorUtil.putMonitorParam(null,transno,tableName,logAgentData);
                    }
                }
                //end added by agp, used for data-link-monitor
				pose = HttpClient.sendPostByJSON(ResourceUtil.getString("log_agent_url"), null, logAgentData);
				logger.info("invoke send http success:" + pose);
			} catch (Exception e) {
				PrometheusUtil.counterStatisByLabel("exception_fk", "label","logagent");
				logger.info("send logAgent error!" ,logAgentData);
			}
		}

		return res;
	}


	/**
	 * 每个表对应一个线程池
	 * @param tableName
	 * @return
	 */
	private static ExecutorService initExecutor(String tableName) {
		TaskQueue queue = new TaskQueue();
		ThreadPoolExecutor tpe = new ThreadPoolExecutor(6, 200, 45L, TimeUnit.SECONDS, queue,
				new ThreadFactoryBuilder().setNameFormat("muti hbase poll" + tableName).build());
		queue.setParent(tpe);
		return tpe;
	}

	/**
	 * 每个表对应一个线程池
	 * @param tableName
	 * @return
	 */
	private static ExecutorService getCompletionService(String tableName) {
		ExecutorService cs = TABLEMAP.get(tableName);
		if (null == cs) {
			cs = initExecutor(tableName);
			TABLEMAP.put(tableName, cs);
		}
		return cs;
	}

	@Override
	public void reInit() {
		reInitConfig();
	}

	static class MutiLevelDB{
		 public  static volatile boolean init=false;
		 public  static volatile DB db = null;
		 
			
		public static   void initDb(String path) throws IOException {
				if(!init && db==null) {
					synchronized (MutiLevelDB.class) {
						Options options = new Options();
						options.createIfMissing(true);
						 db = (DB) Iq80DBFactory.factory.open(new File(path), options);
						init=true;
						 Runtime.getRuntime().addShutdownHook(new Thread(){
							   public void run(){
							    try{
							    	if(null!=db) {
							    		db.close();
							    	}
							    }catch (Exception e) {
							    }
							   }
							  });
					}
				}
			}
		public static void writeKey(String content) {
			long time=System.currentTimeMillis();
			db.put(content.getBytes(), ByteUtils.transformLongtoBytes(time));
		}
		public static void removeKey(byte[] keys) {
			db.delete(keys);
		}
	}
}
