

import java.util.HashMap;
import java.util.Map;

public class StringUtil {
    /**
     * JSONObject or ObjectMapper 不能直接解析出来，
     * 可以将他们里面的键值对解析放到一个map中。
     * @param string
     * @return
     */
    public static Map<String,String> translateNonnormalStrings(String string){
        String[] split = string.split(",");
        Map<String,String> map1=new HashMap<>();
        for (String item:split){
            item=trimPrefix(item);
            if (item.contains(":")){
                String[] split1 = item.split(":");
                if (split1.length==2){
                    String key=trimPreAndPostfix(split1[0]);
                    String value=trimPreAndPostfix(split1[1]);
                    map1.put(key,value);
                }else if (split1.length==3){
                    String key=trimPreAndPostfix(split1[1]);
                    String value=trimPreAndPostfix(split1[2]);
                    map1.put(key,value);
                }

            }
        }
        return map1;
    }
    private static String trimPreAndPostfix(String s) {
        s=trimPrefix(s);
        s=trimPostfix(s);
        return s;
    }

    private static String trimPostfix(String subItem) {
        while (subItem.endsWith("\"")||subItem.endsWith("]")||subItem.endsWith("}")){
            subItem=subItem.substring(0,subItem.length()-1);
        }
        return subItem;
    }

    private static String trimPrefix(String item) {
        while (item.startsWith("{")||item.startsWith("[")|| item.startsWith("\"")){
            item=item.substring(1);
        }
        return item;
    }
}
