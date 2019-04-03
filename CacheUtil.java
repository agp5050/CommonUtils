import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

public class CacheUtil {
    public static Map<String, Cache> MAP = new ConcurrentHashMap();

    public CacheUtil() {
    }

    public static Cache getCacheObj(String key) {
        Cache cache = (Cache)MAP.get(key);
        if (cache == null) {
            Class var2 = CacheUtil.class;
            synchronized(CacheUtil.class) {
                MAP.put(key, CacheBuilder.newBuilder().expireAfterWrite(48L, TimeUnit.HOURS).maximumSize(5000L).weakValues().build());
            }

            return (Cache)MAP.get(key);
        } else {
            return cache;
        }
    }

    public static void putObj(String key, String val, String type) {
        String cacheKey = DigestUtils.md5Hex(val);

        try {
            getCacheObj(key).put(cacheKey, new String((type + val).getBytes("utf-8")));
        } catch (UnsupportedEncodingException var5) {
            ;
        }

    }

    public static void cleanTopic(String key) {
        Cache cache = getCacheObj(key);
        cache.cleanUp();
    }

    public static Collection<String> queryByKey(String key, String searchKey) {
        Cache cache = getCacheObj(key);
        ConcurrentMap<String, String> map = cache.asMap();
        return StringUtils.isNotBlank(searchKey) ? (Collection)map.entrySet().stream().filter((f) -> {
            return ((String)f.getValue()).contains(searchKey);
        }).map((m) -> {
            return (String)m.getValue();
        }).collect(Collectors.toList()) : map.values();
    }
}
