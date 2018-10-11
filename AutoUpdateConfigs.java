
import org.ho.yaml.Yaml;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.HashMap;

public class AutoUpdateConfigs<T> implements Runnable{
    ConfigurableApplicationContext ctx=null;
    T bean=null;
    public AutoUpdateConfigs(){}
    public AutoUpdateConfigs(ConfigurableApplicationContext ctx){
        this.ctx=ctx;
    }

    @Override
    public void run() {
        T bean = (T)ctx.getBean(this.bean.getClass());
        File dumpFile=new File(System.getProperty("user.dir") + "/conf/application-prod.yml");
        try {
            HashMap<String,String> paras = Yaml.loadType(dumpFile, HashMap.class);
            updateBean(bean,paras);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void updateBean(T bean, HashMap<String,String> paras) {
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field:declaredFields){
            field.setAccessible(true);
            String newValue = paras.get(field.getName());
            if (newValue!=null){
                try {
                    field.set(bean,newValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
