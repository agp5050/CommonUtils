
import com.alibaba.fastjson.JSON;
import com.fbank.ai.DTO.CustomerInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
@Slf4j
public class HttpClient {
    public static  final  String testDeomUrl="http://10.26.101.62:9097/com/jfbank/ai/riskdecision/queryscfeatures/";
    public static String doPost(String  jsoString,String url) {
        if (StringUtils.isEmpty(url)){
            url=testDeomUrl;
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        StringEntity stringEntity = new StringEntity(jsoString, "UTF-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);
        CloseableHttpResponse execute = null;
        try {
            execute = httpClient.execute(httpPost);
            String rst = EntityUtils.toString(execute.getEntity());
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
}
