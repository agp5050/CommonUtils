
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

@Slf4j
public class HttpClient {
    //测试地址
//    public static  final  String testDeomUrl="http://10.26.101.62:9097/com/jfbank/ai/riskdecision/queryscfeatures/";
    public static  final  String testDeomUrl="http://127.0.0.1:9055/jffox/cloud/api/v1/cuishou";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2438.3 Safari/537.36";
    public static String doPost(String  jsoString,String url) {
        if (StringUtils.isEmpty(url)){
            url=testDeomUrl;
        }
        CloseableHttpClient httpClient = HttpClientBuilder.create().useSystemProperties().setKeepAliveStrategy(myStrategy)
                .setUserAgent(USER_AGENT).build();
        HttpPost httpPost = new HttpPost(url);
        StringEntity stringEntity = new StringEntity(jsoString, "UTF-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);
        CloseableHttpResponse execute = null;
        try {
            execute = httpClient.execute(httpPost);
            String rst = EntityUtils.toString(execute.getEntity(),"UTF-8");
            return rst;
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            execute.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String doGet(String url){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet=new HttpGet(url);
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            return EntityUtils.toString(entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static final ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
        @Override
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return 5 * 1000;
        }
    };
}
