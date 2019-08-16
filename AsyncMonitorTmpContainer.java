


import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class AsyncMonitorTmpContainer<K,V> extends LRUCacheUtils<K,V> {
    private static final ReentrantLock lockPut =new ReentrantLock();
    private static final ReentrantLock lockRemove =new ReentrantLock();


    public AsyncMonitorTmpContainer(){
    }
    public AsyncMonitorTmpContainer(int capacity){
        super(capacity);
    }

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
        }catch (Exception e){
        }finally {
            lockPut.unlock();
        }
        return put;
    }

    @Override
    public V remove(Object key) {
        lockRemove.lock();
        if (key==null) return null;
        V remove=null;
        try {
            remove = super.remove(key);
        }catch (Exception e){

        }finally {
            lockRemove.unlock();
        }
        return remove;
    }



    public Set<K> getList(){
        return this.keySet();
    }

    public static void main(String[] args) {
        AsyncMonitorTmpContainer<Integer,Integer> container=new AsyncMonitorTmpContainer();
        Random random=new Random();
        for (int i=0;i<100;++i){
            new Thread(){
                public void run(){
                    for (int j=0;j<10000;j++){
                        Integer key=random.nextInt(5000);
                        Integer v=random.nextInt(5000);
                        container.put(key,v);
                    }
                }
            }.start();

            new Thread(){
                public void run(){
                    for (int j=0;j<5000;j++){
                        Integer key=random.nextInt(5000);
                        container.remove(key);
                        if (container.size()>500)
                            System.out.println(container.size());
                    }
                }
            }.start();


        }
    }
}
