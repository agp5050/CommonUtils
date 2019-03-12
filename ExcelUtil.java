import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.File;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.TreeSet;

public class ExcelUtil {
    /**
     * read the jsonobject and write the items into excel. xlxs excel.
     * @param dst
     * @param jsonObject
     * @return
     */
    public static JSONObject createExcel(String dst,JSONObject jsonObject,String dataKey){
        //返回调用结果
        JSONObject result=new JSONObject();
        try{
            File file=new File(dst);
            file.createNewFile();
//            OutputStream outputStream=new FileOutputStream(file);
            Workbook workbook=new XSSFWorkbook();
            Sheet first_sheet = workbook.createSheet("First sheet");
            JSONArray jsonArray = jsonObject.getJSONArray(dataKey);
            JSONObject jsonObject1 = jsonArray.getJSONObject(0);
            Row row = first_sheet.createRow(0);
            int horizontalIndex=0;
            Set<String> sortedSet=new TreeSet<>(jsonObject1.keySet());
            for (String key:sortedSet){
                Cell cell = row.createCell(horizontalIndex++);
                cell.setCellValue(key);
            }
            int vertical=1;
            for (int i=0;i<jsonArray.size();++i) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                Row row1 = first_sheet.createRow(vertical++);
                int horizontalIndexInner=0;
                for (String key:sortedSet){
                    Cell cell = row1.createCell(horizontalIndexInner++);
                    cell.setCellValue(jsonObject2.get(key).toString());
                }
            }
            workbook.write(new FileOutputStream(file));
            workbook.close();
        }catch (Exception e){
            result.put("result","failed");
            result.put("reason",e.getMessage());
            return result;
        }
            result.put("result","success");
        return  result;
    }

}
