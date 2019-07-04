


import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mainly used for daily account, each element represent a day cache data.
 * the element should better be concurrentMap
 * @param <E>
 */
@Data
public class FixedCopyOnWriteListUtil<E> {
    private static final String YYYYMMDD="yyyyMMdd";
    private static final SimpleDateFormat SDF=new SimpleDateFormat(YYYYMMDD);
    private String[] createDates;
    private transient volatile Object[] array;
    //default fixed size is 7
    private int fixedSize=7;
    public FixedCopyOnWriteListUtil(int size){
        if (size>0){
            this.fixedSize=size;
            array=new Object[fixedSize];
            createDates=new String[fixedSize];

        }
    }
    public FixedCopyOnWriteListUtil(){
        array=new Object[fixedSize];
        createDates=new String[fixedSize];
    }

    public static FixedCopyOnWriteListUtil getInstance(int fixedSize){
        if (fixedSize>0){
            return new FixedCopyOnWriteListUtil(fixedSize);
        }
        return new FixedCopyOnWriteListUtil();
    }

    public boolean add(E item){
        Object[] objects = Arrays.copyOf(array, array.length + 1);
        objects[array.length]=item;
        String[] dates = Arrays.copyOf(createDates, createDates.length + 1);
        dates[createDates.length]=new String(SDF.format(new Date()));
        if (objects.length>fixedSize){
            System.arraycopy(objects,1,array,0,fixedSize);
            System.arraycopy(dates,1,createDates,0,fixedSize);
        }
        return true;
    }

    public E getByDateString(String dateString){
        try {
           SDF.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        for (int i=0;i<createDates.length;i++){
            if (dateString.equals(createDates[i])){
                return (E)array[i];
            }
        }
        return null;
    }

    public String getTotalCache(){
        ObjectMapper objectMapper=new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this.getArray());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return JSON.toJSONString(this.getArray());
    }


    public static void main(String[] args) throws JsonProcessingException {
        FixedCopyOnWriteListUtil<Map<String,Integer>> instance = FixedCopyOnWriteListUtil.getInstance(7);
        Map<String,Integer> map=new ConcurrentHashMap<>(2048);
        map.put("aa",11);
        map.put("aa2",112);
        Map<String,Integer> map2=new ConcurrentHashMap<>(2048);
        map2.put("bb",11);
        map2.put("bb2",112);
        instance.add(map);
        instance.add(map2);
        System.out.println(instance.getByDateString("20190704"));
        System.out.println(instance.getTotalCache());
    }



}
