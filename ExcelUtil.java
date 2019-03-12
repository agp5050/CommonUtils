import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
        OutputStream outputStream=null;
        Workbook workbook=null;
        try{
            File file=new File(dst);
            file.createNewFile();
//            OutputStream outputStream=new FileOutputStream(file);
            workbook=new XSSFWorkbook();
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
            outputStream=new FileOutputStream(file);
            workbook.write(outputStream);

        }catch (Exception e){
            result.put("result","failed");
            result.put("reason",e.getMessage());
            return result;
        }finally {
          if (workbook!=null){
              try {
                  workbook.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
              workbook=null;
          }
          if (outputStream!=null){
              try {
                  outputStream.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
              outputStream=null;
          }
        }
            result.put("result","success");
        return  result;
    }

}
