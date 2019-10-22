

import com.agp.cloud.entity.*;
import com.agp.cloud.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
@RestController
@Slf4j
@RequestMapping("/query")
public class Main {
//    private static final Logger log = LoggerFactory
//            .getLogger(Main.class);
    //小文件默认行数限制10000
    private static final String QUERY_COUNT="select count(1) from rep_cs_customer_repaytype";
    private static final String QUERY_LIMIT="select max(id) from  rep_cs_customer_repaytype";
    private static final String QUERY_PREFIX="select * from rep_cs_customer_repaytype where id >=  ";
    private static final String QUERY_SUBFIX=" limit 10000";
    private static final int LINES_LIMIT=10000;
    private static  int START_OFFSET=0;
    private static long totalAmount=0;
    private static int maxId=0;
    private static String localPath=null;
    private static final String TABLE_NAME="custrepaytype";
    private static final String DASH="_";
    private static final String VERTICALLINE="|";
    private static final String TXT_SUBFIX=".txt";
    private static String serverPath=null;
    private static SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat simpleDateFormat2=new SimpleDateFormat("yyyy-MM-dd");
    /**
     * count(1) 待查询总计数目
     */
    public static void getAggregateNumbers(){
        Map<String, Object>stringObjectMap = MysqlUtils.executeSql(QUERY_COUNT);
        if (stringObjectMap!=null && stringObjectMap.size()==1){
            totalAmount=(long)stringObjectMap.values().iterator().next();
            log.info("Total items :{}",totalAmount);
        }
    }

    public static void getMaxId(){
        Map<String, Object>stringObjectMap = MysqlUtils.executeSql(QUERY_LIMIT);
        if (stringObjectMap!=null && stringObjectMap.size()==1){
            maxId=(int)stringObjectMap.values().iterator().next();
            log.info("Max effective id is:{}",maxId);
        }
    }


    /**
     * 计算需要分别子查询多少次
     * @return
     */
    public static long calculateSubQueryTimes(){
        getAggregateNumbers();
        if (totalAmount>0){
            long basicTimes = totalAmount / LINES_LIMIT;
            int bonus = totalAmount%LINES_LIMIT == 0 ? 0 : 1;
            log.info("times of queries :{}",basicTimes+bonus);
            return basicTimes+bonus;
        }
        return 0;
    }

//    /**
//     * @param
//     * first round  1+0*100000=1
//     * 由于<>0这个条件所以不能简单的相乘
//     * @return
//     */
/*
    public static int calculateOffset(int index){
        return START_OFFSET+index*LINES_LIMIT;
    }
*/

    public static List<List<String>> queryOnce(){
        String querySql=QUERY_PREFIX+START_OFFSET+QUERY_SUBFIX;
        log.info("query sql: {}",querySql);
        List<List<String>> rstData = MysqlUtils.executeSqlGetValues(querySql);
        if (!rstData.isEmpty()){
            Integer tmpMaxId = Integer.valueOf(rstData.get(rstData.size() - 1).get(0));
            if (tmpMaxId!=0){
                START_OFFSET=tmpMaxId+1;
                log.info("total numbers of queried data :{}",rstData.size());
            }else {
                log.info("this query rst is empty , then retry ");
            }
            return rstData.subList(0,rstData.size()-1);
        }
        return rstData;
    }

    /**
     * @param data  子查询的数据
     * @param queryRound  子查询结果的轮数，是第几轮的查询
     * @return local file path
     */
    public static String dataToLocalFile(List<List<String>> data, int queryRound) throws IOException {
        if (data.isEmpty())
            throw new RuntimeException("Query Data is Empty in round:"+queryRound);
        String localDefaultFilePath = getLocalDefaultFilePath();
        Path dirPath = Paths.get(localDefaultFilePath);
        if (Files.notExists(dirPath)){
            try {
                synchronized (Main.class){
                    if (Files.notExists(dirPath)){
                        Files.createDirectories(dirPath);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String fileName=getLocalFileName(queryRound);
        Path filePath = Paths.get(localDefaultFilePath + File.separator + fileName);
        Files.deleteIfExists(filePath);
        Files.createFile(filePath);

        List<String> lines=new ArrayList<>();
        //是否需要添加标题
//        if (queryRound==0){
//            List<String> metadata = MysqlUtils.getMetadata();
//            lines.add(String.join(VERTICALLINE,metadata));
//        }
        for (List<String> row:data){
            lines.add(String.join(VERTICALLINE,row));
        }
        try {
            Files.write(filePath,lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath.getFileName().toString();
    }

    private static String getLocalFileName(int queryRound) {
        String newFileName=TABLE_NAME+DASH+queryRound+TXT_SUBFIX;
        return newFileName;

    }

    public static LoginInfo getLoginfo(){
        LoginInfo build = LoginInfo.builder()
                .server(ResourceUtil.getString("ftp.server"))
                .port(ResourceUtil.getInt("ftp.port"))
                .username(ResourceUtil.getString("ftp.username"))
                .password(ResourceUtil.getString("ftp.password"))
                .build();
        return build;
    }

    public static String generateDailyDirName(){
        return simpleDateFormat.format(new Date());
    }
    // Server Path 因为跟客户机不同，最好不要用File.separator
    public static String generateServerPath(){
        return ResourceUtil.getString("ftp.path")+ File.separator+generateDailyDirName();
    }

    public static String getLocalDefaultFilePath(){
        return System.getProperty("user.dir")+File.separator+"tmp"+File.separator+generateDailyDirName();
    }

    public static FileInfo generateBaseFileInfo(){
        FileInfo build = FileInfo.builder()
                .serverPath(serverPath == null ? generateServerPath() : serverPath)
                .localPath(localPath == null ? getLocalDefaultFilePath() : localPath)
                .encoding("utf-8")
                .build();
        return build;
    }

    public static void callBack(){
        HttpCallBackParas param=HttpCallBackParas.builder()
                .createTime(simpleDateFormat2.format(new Date()))
                .dataSize(totalAmount+"")
                .fileSize(calculateSubQueryTimes()+"")
                .exceedType("custRepayType")
                .fileType("txt")
                .build();
        String url=ResourceUtil.getString("callback.api");
        String url1 = HttpCallBackParas.getUrl(param);
        log.info("callback api assembled api url : "+url+url1);
        String s = HttpClient.doGet(url+url1);
        log.info("callback cuishou rst:{}",s);
    }


//    select * from rep_wk_cs_reduce_reapy where id >= start limit batchCount
//    @Scheduled(cron = "0 30 8 * * ?")
    @RequestMapping("/trigger")
    public Result timedPost() {
        getAggregateNumbers();
        //获取最大的有效ID
        getMaxId();
        int i=0;
        List<List<String>> lists;
        do {
            lists = queryOnce();
            if (!lists.isEmpty()){
                try {
                    dataToLocalFile(lists, i);
                    i++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }while (START_OFFSET<maxId);
        log.info("the total num of local files is {}",i);
        log.info("query sql and download to local dir:{} "+generateDailyDirName());
        SFTP.uploadDirectory(ResourceUtil.getString("ftp.path"),generateDailyDirName(),getLocalDefaultFilePath());
        log.info("ftp upload successfully ....");
        log.info("start execute callback api:");
        callBack();
        log.info("finished execute callback api:");
        //每天清理一次。
        MysqlUtils.cleanAfterBatchQuery();
        cleanParasAfterFTP();
        return Result.SUCCESS;
    }

    private void cleanParasAfterFTP() {
        Main.maxId=0;
        Main.START_OFFSET=0;
    }

    private void queryAndSaveToLocal(int i) throws IOException {

    }

    @RequestMapping("/test")
    public Result test(){
        return Result.SUCCESS;
    }

    /**
     * @param lists
     * 实测1000多个文件1分钟就生成好了，生成完毕后，FTPUtil批量上传。
     * @param i
     */
    private static void asyncUploadFtp(List<List<String>> lists,int i) {
        new Thread(()->{
            String localFileName = null;
            try {
                localFileName = dataToLocalFile(lists, i);
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileInfo fileInfo = generateBaseFileInfo();
            fileInfo.setLocalFileName(localFileName);
            fileInfo.setServerFileName(localFileName);
            try {
                FTPUtil.getInstance().sendFile(getLoginfo(),fileInfo);
//                log.info("successfully upload file {}",localFileName);
            } catch (IOException e) {
//                log.warn(e.getMessage());
                e.printStackTrace();
            }

        }).start();
    }
}
