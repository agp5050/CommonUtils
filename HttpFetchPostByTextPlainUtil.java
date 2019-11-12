

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * 解决Content type 'text/plain;charset=UTF-8' not supported error in spring boot inside RestController class
 * 注意text的格式不是直接转化为javabean 所以 @RequestBody JAVABEAN bean 这种模式提示不支持，需要改成 request /response
 * 另一个简单解决办法：@RequestBody String data就可以了。以string的形式接收。
 */
@Slf4j
public class HttpFetchPostByTextPlainUtil {
    public static String fetchPostByTextPlain(HttpServletRequest request) {
        try {
            BufferedReader reader = request.getReader();
            char[] buf = new char[512];
            int len = 0;
            StringBuffer contentBuffer = new StringBuffer();
            while ((len = reader.read(buf)) != -1) {
                contentBuffer.append(buf, 0, len);
            }
            return contentBuffer.toString();

        } catch (IOException e) {
            e.printStackTrace();
            log.error("[获取request中用POST方式“Content-type”是“text/plain”发送的json数据]异常:{}", e.getCause());
        }
        return "";
    }

    public static <T> T fetchPostByTextPlain(HttpServletRequest request, Class<T> clazz) {
        try {
            BufferedReader reader = request.getReader();
            char[] buf = new char[512];
            int len = 0;
            StringBuffer contentBuffer = new StringBuffer();
            while ((len = reader.read(buf)) != -1) {
                contentBuffer.append(buf, 0, len);
            }
            return JSON.parseObject(contentBuffer.toString(), clazz);

        } catch (IOException e) {
            e.printStackTrace();
            log.error("[获取request中用POST方式“Content-type”是“text/plain”发送的json数据]异常:{}", e.getCause());
        }
        return null;
    }

}
