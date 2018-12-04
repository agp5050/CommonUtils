
import redis.clients.jedis.Jedis;

public class RedisPoolUtil {

	private RedisPoolUtil() {
	}

	private static RedisPool redisPool;

	public static String get(String key) {
		Jedis jedis = null;
		String result = null;
		try {
			jedis = RedisPool.getJedis();
			result = jedis.get(key);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				jedis.close();
			}
			return result;
		}
	}

	public static String getSet(String key, String value) {
		Jedis jedis = null;
		String result = null;
		try {
			jedis = RedisPool.getJedis();
			result = jedis.getSet(key, value);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				jedis.close();
			}
			return result;
		}
	}

	public static String set(String key, String value) {
		Jedis jedis = null;
		String result = null;
		try {
			jedis = RedisPool.getJedis();
			result = jedis.set(key, value);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				jedis.close();
			}
			return result;
		}
	}

	public static Long setnx(String key, String value) {
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = RedisPool.getJedis();
			result = jedis.setnx(key, value);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				jedis.close();
			}
			return result;
		}
	}

	public static Long expire(String key, int seconds) {
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = RedisPool.getJedis();
			result = jedis.expire(key, seconds);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				jedis.close();
			}
			return result;
		}
	}

	public static Long del(String key) {
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = RedisPool.getJedis();
			result = jedis.del(key);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				jedis.close();
			}
			return result;
		}
	}

	/**
	 * 递增
	 * 
	 * @param key
	 *            键
	 * @return
	 */
	public static long incr(String key) {
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = RedisPool.getJedis();
			result = RedisPool.getJedis().incr(key);
		} catch (Exception e) {
			RedisPool.close(jedis);
			return result;
		}
		RedisPool.close(jedis);
		return result;
	}

	public static String selectDB(int index) {
		Jedis jedis = null;
		String result = null;
		try {
			jedis = RedisPool.getJedis();
			result = RedisPool.getJedis().select(index);
		} catch (Exception e) {
			RedisPool.close(jedis);
			return result;
		}
		RedisPool.close(jedis);
		return result;
	}

	// 获取值方法
	public static String getDbKey(int db, String key) {
		Jedis jedis = null;
		String result = null;
		try {
			jedis = RedisPool.getJedis();
			jedis.select(db);
			result = jedis.get(key);

		} catch (Exception e) {
			RedisPool.close(jedis);
			return result;
		}
		RedisPool.close(jedis);
		return result;
	}

	// 自增方法
	public static long incrDbKey(int db, String key) {
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = RedisPool.getJedis();
			jedis.select(db);
			result = jedis.incr(key);
		} catch (Exception e) {
			RedisPool.close(jedis);
			return result;
		}
		RedisPool.close(jedis);
		return result;
	}

	// 初始化方法
	public static String setDbKey(int db, String key, String value) {
		Jedis jedis = null;
		String result = null;
		try {
			jedis = RedisPool.getJedis();
			jedis.select(db);
			result = jedis.set(key, value);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				jedis.close();
			}
			return result;
		}
	}
	public static Long setnxDbkey(int db,String key, String value) {
		Jedis jedis = null;
		Long result = null;
		try {
			jedis = RedisPool.getJedis();
			jedis.select(db);
			result = jedis.setnx(key, value);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				jedis.close();
			}
			return result;
		}
	}
	/*public static void main(String[] args) {
		//System.out.println(setDbKey(2, "test", "1"));
		System.out.println(getDbKey(2, "test4444"));
		System.out.println(incrDbKey(2,"test4444"));
		System.out.println(getDbKey(2, "test4444"));
 

	}
*/
}
