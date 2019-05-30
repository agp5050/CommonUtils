

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hbase.async.GetRequest;
import org.hbase.async.HBaseClient;
import org.hbase.async.KeyValue;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.aa.bigdata.dc.common.util.MD5;
import com.aa.bigdata.prometheus.util.PrometheusUtil;
import com.aa.bigdata.utils.ResourceUtil;
import com.stumbleupon.async.Deferred;
import com.stumbleupon.async.TimeoutException;

public class MutiHbaseClient {
	private static final Logger logger = LoggerFactory.getLogger(MutiHbaseClient.class);
	private HBaseClient hBaseClient;
	
	private ExecutorService sinkCallbackPool;
	
	private static final byte[] FAMILY="f".getBytes();
	private static final byte[] QUALIFIER="original_data".getBytes();
	private int waitTimeOut=6*1000;
	private final String clusterZk;
	private String clusterType;
	
	
	public String getClusterZk() {
		return clusterZk;
	}


	public MutiHbaseClient(String clusterZk) {
		this.clusterZk=clusterZk;
		clusterType=this.clusterZk.substring(this.clusterZk.indexOf("hbase")).replace(".", "");
		try {
			sinkCallbackPool = Executors
					.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat(" HBase Call Pool"+clusterZk).build());
			hBaseClient = new HBaseClient(ResourceUtil.getString(clusterZk), "/hbase",
					new NioClientSocketChannelFactory(sinkCallbackPool, sinkCallbackPool));
			waitTimeOut=Integer.parseInt(ResourceUtil.getString("hbase.query.timeout","10000"));
		} catch (Exception e) {
			logger.error("init hbase client error",e);
			System.exit(-1);
		}
	}
	
	 public String querySingle(String tableName,String transNo) throws Exception {
		 long st=System.currentTimeMillis();
		 String key=MD5.getMySQLMD5(transNo).substring(0, 5)+"_"+transNo;
		 ArrayList<KeyValue> res=null;
		 logger.info("tablename is:"+ResourceUtil.getString("query_table_name_"+tableName));
		 byte[] tab=ResourceUtil.getString("query_table_name_"+tableName).getBytes();
		 int tryTimes=0;
		 while(tryTimes<=6) {
			 try {
				 GetRequest get= new GetRequest(tab, key.getBytes(),FAMILY,QUALIFIER);
				 get.setServerBlockCache(false);
				 Deferred<ArrayList<KeyValue>> list=hBaseClient.get(new GetRequest(tab, key.getBytes(),FAMILY,QUALIFIER));
				 res= list.joinUninterruptibly(waitTimeOut);
				 break;
			} catch (Exception e) {
				if(e instanceof TimeoutException) {
					Thread.sleep(waitTimeOut);
					logger.error("query timeout,tableName="+tableName+",transNo="+transNo+"rowKey="+key,e);
					PrometheusUtil.counterStatisByLabel("exception_timeout_"+clusterType, "table", tableName);
					tryTimes++;
				}else {
					logger.error("query error,tableName="+tableName+",transNo="+transNo+"rowKey="+key,e);
					PrometheusUtil.counterStatisByLabel("exception_error_"+clusterType, "table", tableName);
					break;
				}
			}
		 }
		 logger.info(getClusterZk()+"spend time is:"+(System.currentTimeMillis()-st));
		 PrometheusUtil.getHistogram("delay_"+clusterType, "table").labels(tableName).observe( (System.currentTimeMillis()-st)/1000.0 );
		 if(res!=null && res.size()>0) {
				Optional<KeyValue> opt =  res.stream().filter(f-> new String(f.qualifier()).equals("original_data") ).findFirst();
				if(opt.get()!=null) {
					String str= new String(opt.get().value());
					res.clear();
					res=null;
					return str;
				}
		 }
		 return null;
	 }
	 
	 
//	 public  void addRawDataToMap(String tableName,String key,String value) {
//		 synchronized (MutiHbaseClient.class) {
//			 try {
//				 Set list=RAWDATA.get(tableName+"_"+key);
//				 if(list==null) {
//					 list= new LinkedHashSet<>();
//					 RAWDATA.put(tableName+"_"+key, list);
//					 list.add(System.currentTimeMillis()+"");
//				 }
//				list.add(""+value.length());
//			} catch (Exception e) {
//			}
//		}
//	 }
	 
	 
	 //数据一致性校验
//	 static {
//		 Thread tt=new Thread("raw_data_check") {
//				public void run() {
//					while(true) {
//						try {
//							try {
//								Thread.currentThread().sleep(10*60*1000);
//							} catch (InterruptedException e) {
//							}
//							Set<String> sets=RAWDATA.keySet();
//							long curr=System.currentTimeMillis();
//							for(String key:sets) {
//								Set<String> list=RAWDATA.get(key);
//								if(list.size()>0 && curr-Long.parseLong(list.iterator().next())>10*60*1000) {
//									if(list.size()>2) {
//										logger.error(list.toString()+"find data inconstance,transno="+key);
//										String[] arr=key.split("_",2);
//										PrometheusUtil.counterStatisByLabel("diff", "table", arr[0]);
//									}
//									RAWDATA.remove(key);
//								}
//							}
//						} catch (Exception e) {
//						  logger.error(e.getMessage(),e);
//						}
//					}
//				}
//			};
//			tt.start();
//	 }
	
}
