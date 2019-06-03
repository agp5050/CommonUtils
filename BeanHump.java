

import com.google.common.base.CaseFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;

public class BeanHump {
    public static final char UNDERLINE = '_';

    public BeanHump() {
    }

    public static String camelToUnderline(String param) {
        if (param != null && !"".equals(param.trim())) {
            int len = param.length();
            StringBuilder sb = new StringBuilder(len);

            for(int i = 0; i < len; ++i) {
                char c = param.charAt(i);
                if (Character.isUpperCase(c)) {
                    if (i > 0 && param.charAt(i - 1) != '_') {
                        sb.append('_');
                    }

                    sb.append(Character.toLowerCase(c));
                } else {
                    sb.append(c);
                }
            }

            return sb.toString();
        } else {
            return "";
        }
    }

    public static String underlineToCamel(String param) {
        if (param != null && !"".equals(param.trim())) {
            int len = param.length();
            StringBuilder sb = new StringBuilder(len);

            for(int i = 0; i < len; ++i) {
                char c = param.charAt(i);
                if (c == '_') {
                    ++i;
                    if (i < len) {
                        sb.append(Character.toUpperCase(param.charAt(i)));
                    }
                } else {
                    sb.append(c);
                }
            }

            return sb.toString();
        } else {
            return "";
        }
    }

    public static String underlineToCamel2(String param) {
        if (param != null && !"".equals(param.trim())) {
            StringBuilder sb = new StringBuilder(param);
            Matcher mc = Pattern.compile("_").matcher(param);
            int var3 = 0;

            while(mc.find()) {
                int position = mc.end() - var3++;
                String.valueOf(Character.toUpperCase(sb.charAt(position)));
                sb.replace(position - 1, position + 1, sb.substring(position, position + 1).toUpperCase());
            }

            return sb.toString();
        } else {
            return "";
        }
    }

    public static void main(String[] args) {
        System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "aa_ABI2"));
        System.out.println(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "aa_ABI2"));
        System.out.println(camelToUnderline("currQuotaAppNum"));
        System.out.println(DigestUtils.md5Hex("731555"));
        System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "first_Trans__TimeAgo"));
        System.out.println(camelToUnderline("firstTransTimeAgo"));
        System.out.println(underlineToCamel2("aa_bab_cac_a"));

    }
}
