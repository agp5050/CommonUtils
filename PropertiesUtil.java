import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置文件获取
 */
public class PropertiesUtil {
    private String path = "";
    private InputStream inputStream = null;
    public static Properties props = null;

    /**
     * 加载配置文件
     */
    private void getProps() {
        try {
            props = new Properties();
            inputStream = PropertiesUtil.class.getClassLoader().getResourceAsStream(path);
            props.load(inputStream);
        } catch (FileNotFoundException e) {
            System.out.println("file is not exist! file:" + path);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("file reading exception!");
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public PropertiesUtil() {
        path = "properties/commonutil.properties";
        getProps();
    }

    public PropertiesUtil(String path) {
        this.path = path;
        getProps();
    }

    public static String getProperty(String propName) {
        new PropertiesUtil();
        return props.getProperty(propName);
    }

    public static String getProperty(String propName, String defaultValue) {
        new PropertiesUtil();
        String value = props.getProperty(propName, defaultValue);
        if (value == null || "".equals(value)) {
            return defaultValue;
        }
        return value;
    }

    public static String getPropertyByPath(String propName, String path) {
        new PropertiesUtil(path);
        return props.getProperty(propName);
    }

    public static String getPropertyByPath(String propName, String defaultValue, String path) {
        new PropertiesUtil(path);
        String value = props.getProperty(propName, defaultValue);
        if (value == null || "".equals(value)) {
            return defaultValue;
        }
        return value;
    }

       public static String getProps(String key, String defVal) {
        if (DEFAULT_PROPS == null) {
            synchronized (PropertiesUtil.class) {
                if (DEFAULT_PROPS == null) {
                    DEFAULT_PROPS = readPropertiesFile("/client_spconf.properties");
                }
            }
        }
        if(props.getProperty(key)==null){
          return defVal
        }
        return props.getProperty(key);
    }
        public static Properties readPropertiesFile(String file) {
        InputStream in = PropertiesUtil.class.getResourceAsStream(file);
        Properties prop = new Properties();
        try {
            prop.load(in);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return prop;
    }
}
