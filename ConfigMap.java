

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.te.datalake.util.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ConfigMap {
    @Autowired
    Configs configs;
    static boolean flag=true;
    private static final String DEFAULT_KEY_VALUE="空或者其他";
    //as it's a readonly used map ,use hash map for performance;
    public static Map<String, Map<String,String>> filedValueReflectMap=new HashMap<>();
    static {
        //execute only once in lifetime.
        try {
            readExcelData2Map(filedValueReflectMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //in case online update excels.
    public void updateMap() throws IOException {
        Map<String, Map<String,String>> newMap=new HashMap<>();
        String excelName = configs.getExcelName();
        String excelSheet = configs.getExcelSheet();
        log.info("update for excel:{}-- sheet:{}",excelName,excelSheet);
        //assemble dir $DEPLOYDIR/conf
        String parentPath=System.getProperty("user.dir")+File.separator+"/conf";
        excelName=parentPath+ File.separator+excelName;
        readExcelData2MapWithPathAndSheet(newMap,excelName,excelSheet);
        log.info("old map equals new map :{}",Boolean.toString(filedValueReflectMap.equals(newMap)));
        log.info("old map size:{}---new map size:{}",filedValueReflectMap.size(),newMap.size());
        if (!newMap.isEmpty() && !filedValueReflectMap.equals(newMap)){
            synchronized (filedValueReflectMap){
                filedValueReflectMap=newMap;
            }
        }
    }

    private static void readExcelData2Map(Map<String, Map<String, String>> filedValueReflectMap) throws IOException {
        String path= null;
        try {
            path = getExcelPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        readExcelData2MapWithPathAndSheet(filedValueReflectMap,path,"");

    }

    private static void readExcelData2MapWithPathAndSheet(Map<String, Map<String, String>> filedValueReflectMap, String path,String sheetName) throws IOException {
        if (StringUtils.isEmpty(path))
            return;
        if (!Files.exists(Paths.get(path)))
            return;
        if (StringUtils.isEmpty(sheetName))
            sheetName="Sheet1";
        log.info("read excel located at {}",path);
        List<List<Object>> lists = ExcelUtil.readExcel(path, sheetName);
        dumpList2Map(lists,filedValueReflectMap);
    }

    private static void dumpList2Map(List<List<Object>> lists, Map<String, Map<String, String>> filedValueReflectMap) {
        if (lists==null || lists.size()==0 || filedValueReflectMap==null)
            return;
        int size = lists.size();
        //remove the first column name row.
        //"变量名","变量值","对应id"
        //industry","B1017","1"
        //"industry","空或者其他","0"
        lists=lists.subList(1,size);
        String rowKey;
        String rowValue;
        String rowReflectValue;
        for (List<Object> row:lists){
            if (row.size()!=3)
                continue;
            rowKey=row.get(0).toString().trim();
            rowValue=row.get(1).toString().trim();
            rowReflectValue=row.get(2).toString().trim();
            Map<String, String> stringStringMap = filedValueReflectMap.get(rowKey);
            if (stringStringMap==null){
                stringStringMap=new HashMap<>();
                filedValueReflectMap.put(rowKey,stringStringMap);
            }
            stringStringMap.put(rowValue,rowReflectValue);
        }

        validDefaultValue(filedValueReflectMap);
    }

    /**
     * @param filedValueReflectMap
     * check if the map has the key "空或者其他"
     * if not add the default key.
     */
    private synchronized static void validDefaultValue(Map<String, Map<String, String>> filedValueReflectMap) {
        String defaultKey="空或者其他";
        Iterator<Map<String, String>> iterator = filedValueReflectMap.values().iterator();
        if (iterator.hasNext()){
            Map<String, String> next = iterator.next();
            if (!next.containsKey(defaultKey)){
                next.put(defaultKey,"1");
            }
        }
    }

    public static String getExcelPath() throws IOException {
        String defaultName="model_parameter_reflect.xlsx";
        String os = System.getenv("OS");
        if (os!=null && os.contains("Windows")){
            ClassPathResource pathResource=new ClassPathResource(defaultName);
            return pathResource.getFile().getAbsolutePath();
        } else {
            String s = System.getProperty("user.dir") + File.separator + "conf" + File.separator + defaultName;
            return s;
        }
    }

    public static <T> List<T> reflectFromMap(Collection<T> collection){
        ArrayList<T> collect = collection.stream().map(item -> reflectObjectValue(item, filedValueReflectMap)).collect(Collectors.toCollection(ArrayList::new));
        return collect;
    }

    /**
     * 两种方式反射一个是工具类，一个是简单的解析。
     * @param next
     * @param filedValueReflectMap
     * @param <T>
     * @return
     */
    private static <T> T reflectObjectValue(T next, Map<String, Map<String, String>> filedValueReflectMap) {

        if (flag){
            flag=false;
            return simpleReflectObjectValue(next,filedValueReflectMap);
        }else {
            flag=true;
        return reflectObjectValueByJSON(next,filedValueReflectMap);

        }

    }

    private static <T> T simpleReflectObjectValue(T next, Map<String, Map<String, String>> filedValueReflectMap) {
        if (filedValueReflectMap==null) return null;
        boolean primitive2 = ParserConfig.isPrimitive2(next.getClass());
        if (primitive2) return null;
        Class<?> aClass = next.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field item:declaredFields){
            item.setAccessible(true);
            String fieldName = item.getName();
            Map<String, String> stringStringMap = filedValueReflectMap.get(fieldName);
            if (stringStringMap==null)continue;
            Object value;
            try {
                value = item.get(next);
                String reflectValue = stringStringMap.get(value);
                if (reflectValue==null){
                    item.set(next,stringStringMap.get(DEFAULT_KEY_VALUE));
                }else {
                    item.set(next,reflectValue);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return next;
    }

    private static <T> T reflectObjectValueByJSON(T next, Map<String, Map<String, String>> filedValueReflectMap) {
        JSONObject jsonObject = (JSONObject)JSON.toJSON(next, SerializeConfig.globalInstance);
        log.info("jsonObject ....{}",jsonObject);
        for (Map.Entry<String,Object> entry:jsonObject.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue()==null?"":entry.getValue().toString();
            Map<String, String> stringStringMap = filedValueReflectMap.get(key);
            if (stringStringMap==null) continue;
            if (!stringStringMap.containsKey(value)){
                entry.setValue(stringStringMap.get(DEFAULT_KEY_VALUE));
            }else {
                entry.setValue(stringStringMap.get(value));
            }
        }
        return (T) jsonObject.toJavaObject(next.getClass());
    }

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("com.te.datalake.config.ConfigMap");
        System.out.println(JSON.toJSONString(ConfigMap.filedValueReflectMap));

    }
}
