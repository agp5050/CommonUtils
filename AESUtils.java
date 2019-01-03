import com.sun.crypto.provider.DESCipher;
import org.apache.hadoop.hbase.io.crypto.aes.AES;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import sun.security.krb5.internal.crypto.Des;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */
public class AESUtils {

    public static String CIPHER_ALGORITHM = "AES"; // optional value AES/DES/DESede

    private static final String KEY = "8ab1de95-cc8b-4ff1-9df0-cf6b579d457e";

    public static Key getKey(String strKey) {
        try {
            if (strKey == null) {
                strKey = "";
            }
            KeyGenerator _generator = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG","SUN");
            secureRandom.setSeed(strKey.getBytes("UTF-8"));
            _generator.init(128, secureRandom);
            for (int i=0;i<3;++i){

            }
            return _generator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(" 初始化密钥出现异常 ");
        }
    }

    /**
     * 加密
     * @param content 待加密内容
     * @return
     */
    public static String encrypt(String content) {
        try {
            SecureRandom sr = new SecureRandom();
            Key secureKey = getKey(KEY);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secureKey, sr);
            byte[] bt = cipher.doFinal(content.getBytes("UTF-8"));
            String strS = new BASE64Encoder().encode(bt);
            strS = replaceBlank(strS);
            return strS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密
     * @param content 待解密内容
     * @return
     */
    public static String decrypt(String content) {
        try {
            content = replaceBlank(content);
            SecureRandom sr = new SecureRandom();
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            Key secureKey = getKey(KEY);
            cipher.init(Cipher.DECRYPT_MODE, secureKey, sr);

            byte[] res = new BASE64Decoder().decodeBuffer(content);
            res = cipher.doFinal(res);
            return new String(new String(res, "UTF-8").getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //如果有错就返加null
        return null;
    }

    private static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
            dest = dest.replaceAll("\"","");
        }
        return dest;
    }

    public static void main(String[] args) {
        String pwd = "eyPkxygxAh+CRfv0zi8pJ0e5lWm2WsyrVdQOqeTMi9I6jMeKID82+4YaXAbdVpBb";
        System.out.println(decrypt(pwd));
        String md5Hex = AESUtils.encrypt("aaaaaa");
        System.out.println(md5Hex.length());
        System.out.println(AESUtils.decrypt("6mfKe/bHp0uTIBVPUnThYg=="));
        System.out.println("e10adc3949ba59abbe56e057f20f883e".length());
        System.out.println("RU8gSc51nI5u1cz61crDerGWkbuUshEP".length());
        System.out.println("8ab1de95-cc8b-4ff1-9df0-cf6b579d457e".length());
        System.out.println("5b895d697aab11e8844200163e3078085b895d697aab11e8844200163e307808");
        System.out.println(encrypt("e10adc3949ba59abbe56e057f20f883e"));
    }

}
