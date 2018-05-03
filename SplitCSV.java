
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Mr.An
 * @Date 18/5/3 下午5:26
 */
public  class SplitCSV {

    public static void split2(String srcPath) throws IOException {
        ArrayList<String> arrayList = new ArrayList<>();
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(srcPath),Charset.forName("gbk"));
        String line;
        while((line=bufferedReader.readLine())!=null){
            line=line.replaceAll("\"","");
            arrayList.add(line+"\n");
        }
        bufferedReader.close();
        int size = arrayList.size();
        int splitSize=size/2;
        List<String> subList0 = arrayList.subList(1, splitSize);
        List<String> subList1 = arrayList.subList(splitSize, size);
        String pathWithoutSuffix = srcPath.substring(0, srcPath.lastIndexOf(".csv"));
        for(int i=0;i<2;i++){
            String dstPath=pathWithoutSuffix+"_split"+i+".csv";
            BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(dstPath),Charset.forName("gbk"));
            if(i==0){
                for (String line0:subList0){
                    bufferedWriter.write(line0);
                }
                bufferedWriter.flush();
            }

            if(i==1){
                for (String line1:subList1){
                    bufferedWriter.write(line1);
                }
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        }


    }

    public static void split2ten(String srcPath) throws IOException {
        ArrayList<String> arrayList = new ArrayList<>();
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(srcPath),Charset.forName("gbk"));
        String line;
        while((line=bufferedReader.readLine())!=null){
            line=line.replaceAll("\"","");
            arrayList.add(line+"\n");
        }
        bufferedReader.close();
        int size = arrayList.size();
        int splitSize=size/10;
        System.out.println(size+"///"+splitSize);
        String pathWithoutSuffix = srcPath.substring(0, srcPath.lastIndexOf(".csv"));
        for(int i=0;i<10;i++){
            String dstPath=pathWithoutSuffix+"_split"+i+".csv";
            BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(dstPath), Charset.forName("gbk"));
            if(i==0){
                for (String line0:arrayList.subList(1, splitSize)){
                    bufferedWriter.write(line0);
                }
                bufferedWriter.flush();
            }else if(i==9){
                for (String line0:arrayList.subList(splitSize*9, size)){
                    bufferedWriter.write(line0);
                }
                bufferedWriter.flush();
            }else{
                for (String line0:arrayList.subList(splitSize*i, splitSize*(i+1))){
                    bufferedWriter.write(line0);
                }
                bufferedWriter.flush();
            }


            bufferedWriter.close();
        }

    }

    public static void main(String[] args) throws IOException {
//        SplitCSV.split2("/Users/finup/Desktop/abc.csv");
//                SplitCSV.split2("/Users/finup/Desktop/gongxiangpingtaishangbao20180430.csv");

        SplitCSV.split2ten("/Users/finup/Desktop/gongxiangpingtaishangbao20180430.csv");
    }
}
