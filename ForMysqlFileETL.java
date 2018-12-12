public class ForMysqlFileETL {
    private static final String filePath="D:\\github\\信审\\归档\\wk_db_loan.sql";
    private static final String dateStr="\'0000-00-00 00:00:00\'";
//    '2018-12-04 11:09:18.862' datePattern for this
    private static final String datePattern="'\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3}'";
    private static final String newStr="CURRENT_TIMESTAMP";
    public static void main(String[] args) {
        long start=System.currentTimeMillis();
        FileReader fileReader;
        try {
            fileReader=new FileReader(new File(filePath));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            int i = filePath.lastIndexOf(".");
            String newFilePath = filePath.substring(0, i) + 1 + filePath.substring(i);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(newFilePath)));
            String s;
            while ((s = bufferedReader.readLine())!=null){
                String s1 = s.replaceAll("\\?", "").replaceAll("？", "").replaceAll(dateStr,newStr).replaceAll(datePattern,newStr);
                bufferedWriter.write(s1+"\n");
            }
            System.out.println((System.currentTimeMillis()-start)+" milliseconds spent for file ETL");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
