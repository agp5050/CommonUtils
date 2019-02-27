

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.File;
import java.io.IOException;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class LevelDB {
	 private static final Logger logger = LoggerFactory.getLogger(LevelDB.class);
	 public  static volatile boolean init=false;
	 public  static volatile DB db = null;
		
	public static   void initDb(String path) throws IOException {
			if(!init && db==null) {
				synchronized (LevelDB.class) {
					Options options = new Options();
					options.createIfMissing(true);
					 db = factory.open(new File(path), options);
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
		
		public static  void writeKey(String key,String conent) {
			if(!init && db==null) {
				logger.error("level db not init");
				return;
			}
			db.put(key.getBytes(), conent.getBytes());
		}
		
		public static  void writeKey(String key,byte[] conent) {
			if(!init && db==null) {
				logger.error("level db not init");
				return;
			}
			db.put(key.getBytes(), conent);
		}
		
		public static byte[] getByKey(String key) {
			return db.get(key.getBytes());
		}
		

}
