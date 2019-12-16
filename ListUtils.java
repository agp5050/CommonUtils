
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.function.Function;

/**
 * List集合工具类.
 *
 *
 * @date 2018/7/31 11:16
 */
public class ListUtils {

    /**
     * list数据分类转换成Map.
     *
     * @param elements
     * @param fun
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> Map<R, T> toMap(List<T> elements, Function<T, R> fun) {
        if (CollectionUtils.isEmpty(elements)) {
            return Collections.emptyMap();
        }
        Map<R, T> map = new LinkedHashMap<>();
        for (T t : elements) {
            map.put(fun.apply(t), t);
        }
        return map;
    }

    /**
     * 将list数据指定字段分类转换成Map.
     *
     * @param elements
     * @param keyFun
     * @param valueFun
     * @param <T>
     * @param <K>
     * @param <V>
     * @return
     */
    public static <T, K, V> Map<K, V> toMap(List<T> elements,
                                            Function<T, K> keyFun,
                                            Function<T, V> valueFun) {
        if (CollectionUtils.isEmpty(elements)) {
            return Collections.emptyMap();
        }
        Map<K, V> map = new LinkedHashMap<>();
        for (T t : elements) {
            map.put(keyFun.apply(t), valueFun.apply(t));
        }
        return map;
    }

    /**
     * list数据分类转换成Map.
     *
     * <pre>
     *     多数据key重复的情况
     * </pre>
     * @param elements
     * @param fun
     * @param <T>  集合元素类型
     * @param <R>  key类型
     * @return
     */
    public static <T, R> Map<R, List<T>> toListMap(List<T> elements, Function<T, R> fun) {
        if (CollectionUtils.isEmpty(elements)) {
            return Collections.emptyMap();
        }
        Map<R, List<T>> map = new HashMap<>();
        for (T t : elements) {
            R key = fun.apply(t);
            List<T> tmp = map.getOrDefault(key, new ArrayList<>());
            tmp.add(t);
            map.put(key, tmp);
        }
        return map;
    }

    /**
     * list数据分类转换成Map.
     *
     * @param elements  数据列表
     * @param keyFun  key表达式
     * @param valueFun  value表达式
     * @param <T>  数据原类型
     * @param <K>  返回map key类型
     * @param <V>  返回map value 类型
     * @return
     */
    public static <T, K, V> Map<K, List<V>> toListMap(List<T> elements,
                                                Function<T, K> keyFun, Function<T, V> valueFun) {
        if (CollectionUtils.isEmpty(elements)) {
            return Collections.emptyMap();
        }
        Map<K, List<V>> map = new HashMap<>();
        for (T t : elements) {
            K key = keyFun.apply(t);
            V value = valueFun.apply(t);
            List<V> tmp = map.getOrDefault(key, new ArrayList<>());
            tmp.add(value);
            map.put(key, tmp);
        }
        return map;
    }

    /**
     * 获取列表中某一个字段的值.
     *
     * @param dataList
     * @param func
     * @param <T>  列表元素类型
     * @param <R>  需要获取的字段类型
     * @return
     */
    public static <T, R> List<R> fetchFieldsValue(List<T> dataList, Function<T, R> func) {
        if (CollectionUtils.isEmpty(dataList)) {
            return Collections.emptyList();
        }
        List<R> result = new ArrayList<>();
        for (T t : dataList) {
            result.add(func.apply(t));
        }
        return result;
    }

     /**
     * 复制列表.
     *
     * @param descClassType 复制目标类型
     * @param srcList  复制源列表
     * @param <D>  目标列表类型
     * @param <S>  源列表类型
     * @return
     */
    public static <D, S> List<D> copyList(Class<D> descClassType, List<S> srcList) {
        if (CollectionUtils.isEmpty(srcList)) {
            return Collections.emptyList();
        }
        Class<?> componetClass = srcList.get(0).getClass();

        List<D> destList = new ArrayList<>();
        try {
            boolean isPrimitives = isPrimitives(componetClass, descClassType);
            for (S srcData : srcList) {
                if (!isPrimitives){
                    D destData = descClassType.newInstance();
                    BeanUtils.copyProperties(destData, srcData);
                    destList.add(destData);
                }else {
                    if (componetClass ==descClassType){
                        destList.add((D) srcData);
                    }else {
                        throw new RuntimeException("class TYPE is primitives");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return destList;
    }

    private static <D> boolean isPrimitives(Class<D> descClassType, Class<?> componetClass) {
        return componetClass.isPrimitive()
                || componetClass == Short.class
                || componetClass == Integer.class
                || componetClass == Long.class
                || componetClass == Float.class
                || componetClass == Double.class
                || componetClass == Boolean.class
                || componetClass == String.class
                || componetClass == Byte.class
                || componetClass == Character.class
                || componetClass.isEnum();

    }
    
    public static <E,R> void splitList(List<E> list, Integer splitLength, Function<List<E>,R> function){
        if (list==null || list.size()==0) return;
        int size=list.size();
        int roundTimes=size/splitLength;
        int remains=size%splitLength;
        int from=0,to=0;
        for (int i=0;i<roundTimes;++i){
            from=i*splitLength;
            to=(i+1)*splitLength;
            List<E> es = list.subList(from, to);
            function.apply(es);
        }
        List<E> es = list.subList(to, to + remains);
        function.apply(es);
    }
}
