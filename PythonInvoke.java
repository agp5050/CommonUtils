import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PythonInvoke {
    public static String invoke(String path, List<String> paras){
        try {
            log.info("python invoke started!");
            List<String> argList=new ArrayList<>();
            argList.add("python");
            argList.add(path);
            argList.addAll(paras);
            String[] args=new String[argList.size()];
            Process pr=Runtime.getRuntime().exec(argList.toArray(args));

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    pr.getInputStream()));
            StringBuffer stringBuffer=new StringBuffer();
            String line;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
            }
            in.close();
            pr.waitFor();

            log.info("python invoke ended!");
            return stringBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
//web项目需要根据assembly配置python项目目录，根据System.getProperty("user.id")获取地址。
