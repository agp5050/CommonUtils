import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.te.datalake.configs.Configs;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class AutoUpdateConfigs<T>{
    private T configs;
    private String filePath;
    public AutoUpdateConfigs(T configs, String path){
        this.configs=configs;
        this.filePath=path;
    }

    public void updateBean() {
        Yaml yaml=new Yaml();
        boolean ifModified=false;
        try {
            HashMap<String,Object> hashMap = yaml.loadAs(new FileReader(filePath), HashMap.class);
            HashMap<String,Object> hashMapN=new HashMap<>();
            //只支持1层目录，如果再加上一层循环可以支持多层目录yaml
            for (Object object:hashMap.values()){
                if (object.getClass()== LinkedHashMap.class){
                    LinkedHashMap object1 = (LinkedHashMap) object;
                    hashMapN.putAll(object1);
                }
            }
            Field[] declaredFields = configs.getClass().getDeclaredFields();
            for (Field field:declaredFields){
                field.setAccessible(true);
                Object newValue = hashMapN.get(field.getName());
                if (newValue!=null){
                    if (newValue.equals(field.get(configs))){
                        continue;
                    }
                    ifModified=true;
                    field.set(configs,newValue);
                }

            }
            //如果有修改打印到控制台
            if (ifModified){
                System.out.println(configs+"-----new Bean");

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        Configs configs = new Configs();
        System.out.println(configs);
        String path=System.getProperty("user.dir")+"\\src\\main\\resources\\application-test.yml";
        System.out.println(path);
        AutoUpdateConfigs<Configs> autoUpdateConfigs=new AutoUpdateConfigs<>(configs,path);
        autoUpdateConfigs.updateBean();
        System.out.println(configs);
    }
}



@Component
@Slf4j
class AutoUpdateThread extends Thread {
    @Autowired
    Configs configs;
    public  static  ScheduledExecutorService cronExecutorcronExecutor = Executors
            .newScheduledThreadPool(2,new ThreadFactoryBuilder().setNameFormat("dxddpost-%d").build());

    public synchronized static void newExecutor(){
        cronExecutorcronExecutor= Executors
                .newScheduledThreadPool(2,new ThreadFactoryBuilder().setNameFormat("dxddpost-%d").build());
    }
    public void run(){
        log.warn("update bean submitted...");
        cronExecutorcronExecutor.scheduleAtFixedRate(()->{
            String filePath=System.getProperty("user.dir") + "/conf/application-prod.yml";
            AutoUpdateConfigs<Configs> configsAutoUpdateConfigs = new AutoUpdateConfigs(configs,filePath);
            configsAutoUpdateConfigs.updateBean();
        },Long.valueOf(configs.getDelay()),Long.valueOf(configs.getInterval()),TimeUnit.MILLISECONDS);
    }
    public AutoUpdateThread(Configs configs){
        this.configs=configs;
    }
}

