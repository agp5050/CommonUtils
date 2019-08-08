

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
@Slf4j
public class AsyncMonitorTmpContainer<K,V> extends ConcurrentHashMap<K,V> {
    private static final ReentrantLock lockPut =new ReentrantLock();
    private static final ReentrantLock lockRemove =new ReentrantLock();
    private List<K> keyList=new ArrayList<>(2000);
    private static final int IN_CASE_OF_MEMO_OVERFLOW_THRESHOLD=5000;

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return super.getOrDefault(key, defaultValue);
    }

    @Override
    public V put(K key, V value) {
        if (key==null || value==null) return null;
        lockPut.lock();
        V put=null;
        try {
           put = super.put(key, value);
            keyList.add(key);
            if (keyList.size()>=IN_CASE_OF_MEMO_OVERFLOW_THRESHOLD){
                K k = keyList.get(0);
                log.warn("IN_CASE_OF_MEMO_OVERFLOW_THRESHOLD remove Oldest item :{}",k);
                remove(k);
            }
        }catch (Exception e){
        }finally {
            lockPut.unlock();
        }
        return put;
    }

    @Override
    public V remove(Object key) {
        if (key==null) return null;
        lockRemove.lock();
        V remove=null;
        try {
            remove = super.remove(key);
            keyList.remove(key);
        }catch (Exception e){

        }finally {
            lockRemove.unlock();
        }
        return remove;
    }

    public  List<K> getList(){
        return keyList;
    }

    public static void main(String[] args) {
        AsyncMonitorTmpContainer<Integer,Integer> container=new AsyncMonitorTmpContainer();
        Random random=new Random();
        for (int i=0;i<100;++i){
            new Thread(){
                public void run(){
                    for (int j=0;j<100;j++){
                        Integer key=random.nextInt(10);
                        Integer v=random.nextInt(20);
                        container.put(key,v);
                    }
                }
            }.start();

            new Thread(){
                public void run(){
                    for (int j=0;j<50;j++){
                        Integer key=random.nextInt(10);
                        container.remove(key);
                    }
                }
            }.start();


        }
    }
}
