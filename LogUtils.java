//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LogUtils {
    public static final String LOG_FACTORY = "org.slf4j.LoggerFactory";
    public static final String LOGBACK_CLASSIC = "ch.qos.logback.classic";
    public static final String LOGBACK_CLASSIC_LOGGER = "ch.qos.logback.classic.Logger";
    public static final String LOGBACK_CLASSIC_LEVEL = "ch.qos.logback.classic.Level";
    public static final String LOG4J_CLASSIC = "org.apache.log4j";
    public static final String LOG4J_CLASSIC_LOGGER = "org.apache.log4j.Logger";
    public static final String LOG4J_CLASSIC_LEVEL = "org.apache.log4j.Level";
    private static ClassLoader classLoader = null;

    public LogUtils() {
    }

    public static boolean setLog4j2Level(String loggerName, String logLevel) {
        String logLevelUpper = logLevel == null ? "OFF" : logLevel.toUpperCase();
        Map map = getAllLogLevelFromLog4J2();

        try {
            if (map.get(loggerName) == null) {
                throw new RuntimeException("No logger for the name:" + loggerName);
            } else {
                Object logLevelObj = getFieldVaulue("org.apache.logging.log4j.Level", logLevelUpper);
                if (logLevelObj == null) {
                    throw new RuntimeException("No such log level: :" + logLevelUpper);
                } else {
                    Class<?>[] paramTypes = new Class[]{logLevelObj.getClass()};
                    Object[] params = new Object[]{logLevelObj};
                    Method method = map.get(loggerName).getClass().getMethod("setLevel", paramTypes);
                    method.invoke(map.get(loggerName), params);
                    Class<?> clazz = classLoader.loadClass("org.apache.log4j.LogManager");
                    Object loggerContext = clazz.getMethod("getContext", Boolean.class).invoke((Object)null, false);
                    loggerContext.getClass().getMethod("updateLoggers").invoke(loggerContext);
                    return true;
                }
            }
        } catch (Exception var10) {
            throw new RuntimeException("Couldn't set log4j level to" + logLevelUpper + "for the logger " + loggerName);
        }
    }

    public static boolean setLog4jLevel(String loggerName, String logLevel) {
        String logLevelUpper = logLevel == null ? "OFF" : logLevel.toUpperCase();

        try {
            Class<?> clz = Class.forName("org.apache.log4j.Logger");
            Object loggerObtained;
            Method method;
            if (loggerName != null && !loggerName.trim().isEmpty()) {
                method = clz.getMethod("getLogger", String.class);
                loggerObtained = method.invoke((Object)null, loggerName);
            } else {
                method = clz.getMethod("getRootLogger");
                loggerObtained = method.invoke((Object)null);
                loggerName = "ROOT";
            }

            if (loggerObtained == null) {
                throw new RuntimeException("No logger for the name:" + loggerName);
            } else {
                Object logLevelObj = getFieldVaulue("org.apache.log4j.Level", logLevelUpper);
                if (logLevelObj == null) {
                    throw new RuntimeException("No such log level: :" + logLevelUpper);
                } else {
                    Class<?>[] paramTypes = new Class[]{logLevelObj.getClass()};
                    Object[] params = new Object[]{logLevelObj};
                    Method method = clz.getMethod("setLevel", paramTypes);
                    method.invoke(loggerObtained, params);
                    return true;
                }
            }
        } catch (Exception var9) {
            throw new RuntimeException("Couldn't set log4j level to" + logLevelUpper + "for the logger " + loggerName);
        }
    }

    public static boolean setLogBackLevel(String loggerName, String logLevel) {
        String logLevelUpper = logLevel == null ? "OFF" : logLevel.toUpperCase();

        try {
            if (loggerName == null || loggerName.trim().isEmpty()) {
                loggerName = (String)getFieldVaulue("ch.qos.logback.classic.Logger", "ROOT_LOGGER_NAME");
            }

            Object loggerObtained = getLogObject(loggerName);
            if (loggerObtained == null) {
                throw new RuntimeException("No logger for the name:" + loggerName);
            } else {
                Object logLevelObj = getFieldVaulue("ch.qos.logback.classic.Level", logLevelUpper);
                if (logLevelObj == null) {
                    throw new RuntimeException("No such log level: :" + logLevelUpper);
                } else {
                    Class<?>[] paramTypes = new Class[]{logLevelObj.getClass()};
                    Object[] params = new Object[]{logLevelObj};
                    Class<?> clz = classLoader.loadClass("ch.qos.logback.classic.Logger");
                    Method method = clz.getMethod("setLevel", paramTypes);
                    method.invoke(loggerObtained, params);
                    return true;
                }
            }
        } catch (Exception var9) {
            throw new RuntimeException("Couldn't set log4j level to" + logLevelUpper + "for the logger " + loggerName);
        }
    }

    private static Object getLogObject(String name) {
        try {
            Class<?> clazz = classLoader.loadClass("org.slf4j.LoggerFactory");
            return clazz.getMethod("getLogger", String.class).invoke((Object)null, name);
        } catch (Exception var2) {
            var2.printStackTrace();
            return null;
        }
    }

    private static Object getFieldVaulue(String fullClassName, String fieldName) {
        try {
            Class<?> clazz = classLoader.loadClass(fullClassName);
            Field field = clazz.getField(fieldName);
            return field.get((Object)null);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | ClassNotFoundException var4) {
            return null;
        }
    }

    public static boolean setLogLevel(String loggerName, String logLevel) {
        if (checkLogType("ch.qos.logback.classic.Logger")) {
            return setLogBackLevel(loggerName, logLevel);
        } else if (checkLogType("org.apache.logging.log4j.core.config.LoggerConfig")) {
            return setLog4j2Level(loggerName, logLevel);
        } else if (checkLogType("org.apache.log4j.Logger")) {
            return setLog4jLevel(loggerName, logLevel);
        } else {
            throw new RuntimeException("not found log jar");
        }
    }

    public static Map<String, Object> getAllLogLevel() {
        if (checkLogType("ch.qos.logback.classic.Logger")) {
            return getAllLogLevelFromLogBack();
        } else if (checkLogType("org.apache.logging.log4j.core.config.LoggerConfig")) {
            return getAllLogLevelFromLog4J2();
        } else {
            return (Map)(checkLogType("org.apache.log4j.Logger") ? getAllLogLevelFromLog4J() : new HashMap());
        }
    }

    private static boolean checkLogType(String className) {
        try {
            classLoader.loadClass(className);
            return true;
        } catch (ClassNotFoundException var2) {
            return false;
        }
    }

    public static Map<String, Object> getAllLogLevelFromLog4J2() {
        HashMap res = new HashMap();

        try {
            Class<?> clazz = classLoader.loadClass("org.apache.log4j.LogManager");
            Object loggerContext = clazz.getMethod("getContext", Boolean.class).invoke((Object)null, false);
            Class<?> contextClass = loggerContext.getClass();
            Object config = contextClass.getMethod("getConfiguration").invoke(loggerContext);
            Class<?> configClass = config.getClass();
            Map<String, Object> map = (Map)configClass.getMethod("getLoggers").invoke(config);

            Object obj;
            String key;
            for(Iterator var7 = map.values().iterator(); var7.hasNext(); res.put(key, obj.getClass().getMethod("getLevel").invoke(obj))) {
                obj = var7.next();
                key = (String)obj.getClass().getMethod("getName").invoke(obj);
                if (null == key || key.length() == 0) {
                    key = "root";
                }
            }
        } catch (Exception var10) {
            ;
        }

        return res;
    }

    public static Map<String, Object> getAllLogLevelFromLog4J() {
        HashMap res = new HashMap();

        try {
            Class<?> clazz = classLoader.loadClass("org.apache.log4j.LogManager");
            Object obj = clazz.getMethod("getRootLogger").invoke((Object)null);
            Class<?> logerClass = obj.getClass();
            Object tmp = logerClass.getMethod("getLevel").invoke(obj);
            res.put((String)logerClass.getMethod("getName").invoke(obj), tmp);
            Enumeration enumeration = (Enumeration)clazz.getMethod("getCurrentLoggers").invoke((Object)null);

            while(enumeration.hasMoreElements()) {
                obj = enumeration.nextElement();
                tmp = logerClass.getMethod("getLevel").invoke(obj);
                if (null != tmp) {
                    res.put((String)logerClass.getMethod("getName").invoke(obj), tmp);
                }
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }

        return res;
    }

    public static Map<String, Object> getAllLogLevelFromLogBack() {
        HashMap res = new HashMap();

        try {
            Class<?> clazz = classLoader.loadClass("org.slf4j.LoggerFactory");
            Object rootLog = clazz.getMethod("getLogger", String.class).invoke((Object)null, "ROOT");
            Class<?> logerClass = rootLog.getClass();
            res.put((String)logerClass.getMethod("getName").invoke(rootLog), logerClass.getMethod("getLevel").invoke(rootLog));
            Object loggerContext = clazz.getMethod("getILoggerFactory").invoke((Object)null);
            List<Object> list = (List)loggerContext.getClass().getMethod("getLoggerList").invoke(loggerContext);
            Iterator var6 = list.iterator();

            while(var6.hasNext()) {
                Object obj = var6.next();
                Object tmp = logerClass.getMethod("getLevel").invoke(obj);
                if (null != tmp) {
                    res.put((String)logerClass.getMethod("getName").invoke(obj), tmp);
                }
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return res;
    }

    static {
        classLoader = LogUtils.class.getClassLoader();
    }
}
