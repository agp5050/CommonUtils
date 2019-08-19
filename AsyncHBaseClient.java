

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.google.common.collect.Lists;
import com.agp.bigdata.prometheus.util.PrometheusUtil;

import org.hbase.async.CallQueueTooBigException;
import org.hbase.async.GetRequest;
import org.hbase.async.GetResultOrException;
import org.hbase.async.HBaseClient;
import org.hbase.async.KeyValue;
import org.hbase.async.ScanFilter;
import org.hbase.async.Scanner;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import com.stumbleupon.async.Deferred;
import com.stumbleupon.async.TimeoutException;
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class AsyncHBaseClient {
	private static final Logger logger = LoggerFactory.getLogger(AsyncHBaseClient.class);
	
	//业务方接口设置的超时时间为2s,因此查询hbase的超时时间必须设置的小于2s
	@Value("${hbase.batch.timeout:1200}")
	private int timeout;
	
	@Value("${hbase.zk.quorum:node3,node4,node5}")
	private String zookeeperDir;
	
	private HBaseClient hBaseClient;
	
	private ExecutorService sinkCallbackPool;
	
	 @PostConstruct
	  public void initConfig() {
			sinkCallbackPool = Executors
					.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat(" HBase Call Pool").build());
			hBaseClient = new HBaseClient(zookeeperDir, "/hbase",
					new NioClientSocketChannelFactory(sinkCallbackPool, sinkCallbackPool));
			logger.info("zookeeper="+zookeeperDir+",timeout="+timeout);
	 }
	 
	 /**
	  * 根据certId查询
	  * @param certId
	  * @return
	  * @throws Exception
	  */
	 public Map<String,Object> queryByCertId(String hbaseTable,String baseKey) throws Exception {
		 long st=System.currentTimeMillis();
		 String key=MD5.getMySQLMD5(baseKey).substring(0, 5)+"_"+baseKey;
		 logger.info("rowkey:"+key);
		 Map<String,Object> mapRes= new ConcurrentHashMap<>();
		 try {
			 ArrayList<KeyValue> res = null;
			 while(true){
				 try {
					 GetRequest getRequest = new GetRequest(hbaseTable, key);
					 getRequest.setServerBlockCache(false);
					 Deferred<ArrayList<KeyValue>> list=hBaseClient.get(getRequest);
					 res= list.joinUninterruptibly(timeout);
					 break;
				} catch (Exception e) {
					logger.info("query hbase exception:",e);
					if(e instanceof TimeoutException || e instanceof CallQueueTooBigException){
						PrometheusUtil.counterStatisByLabel("execption", "label", "timeout");
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e1) {
						}
					}else{
						PrometheusUtil.counterStatisByLabel("execption", "label", "fatal");
						break;
					}
				}
			 }
			
			if(res == null || res.size() == 0){
				return mapRes;
			}
			 for(KeyValue m : res){
				 mapRes.put( new String(m.qualifier()), new String(m.value()));
			 }
			 long end_time = System.currentTimeMillis();
			 logger.info("hbase query:"+(end_time - st));

		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw e;
		}
		 double delay=(System.currentTimeMillis()-st)/1000.0;
		 //查询
		 PrometheusUtil.getHistogram("delay", "label").
		 labels("single").observe(delay);
		 return mapRes;
	 }
	 
	 
	 /**
	  * 根据certId查询
	  * @param certId
	  * @return
	  * @throws Exception
	  */
	 public Map<String,Object> queryByCertId(String hbaseTable,String[] columns,String baseKey) {
		 long st=System.currentTimeMillis();
		 String key=MD5.getMySQLMD5(baseKey).substring(0, 5)+"_"+baseKey;
		 logger.info("rowkey:"+key);
		
		 Map<String,Object> mapRes= new ConcurrentHashMap<>();
		 int retryCount = 0;
		 ArrayList<KeyValue> res = null;
		 while(retryCount < 3){
			 try {
				 GetRequest getRequest = new GetRequest(hbaseTable, key);
				 getRequest.setServerBlockCache(false);
				 getRequest.family("f");
				 byte[][] quaByte = new byte[columns.length][];
				 for (int i = 0; i < columns.length; i++) {
					quaByte[i] = columns[i].getBytes();
				}
				 getRequest.qualifiers(quaByte);
				 Deferred<ArrayList<KeyValue>> list=hBaseClient.get(getRequest);
				 res= list.joinUninterruptibly(timeout);
				 res.forEach( m-> mapRes.put(new String(m.qualifier()), new String(m.value())));
				 break;
			} catch (Exception e) {
				logger.info("query hbase exception:",e);
				if(e instanceof TimeoutException || e instanceof CallQueueTooBigException){
					PrometheusUtil.counterStatisByLabel("execption", "label", "timeout");
					try {
						Thread.sleep(200);
						retryCount ++;
					} catch (InterruptedException e1) {
					}
				}else{
					PrometheusUtil.counterStatisByLabel("execption", "label", "fatal");
					break;
				}
			}
		 }
		 logger.info("hbase query:"+(System.currentTimeMillis() - st));
		 //查询
		 PrometheusUtil.getHistogram("delay", "label").
		 labels("single").observe((System.currentTimeMillis()-st)/1000.0);
		 return mapRes;
	 }
	 
	 public static void main(String[] args) {
		 String[] columns = new String[]{"aa"};
		 byte[][] quaByte = new byte[columns.length][];
		 for (int i = 0; i < columns.length; i++) {
			quaByte[i] = columns[i].getBytes();
		}
	 }
	 
	 
}
