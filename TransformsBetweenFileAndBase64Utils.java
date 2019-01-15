
import java.io.*;
import java.util.Base64;
public class TransformsBetweenFileAndBase64Utils {
    public static File base64ToFile(String base64,String fileName) {
        File file = null;
        FileOutputStream out = null;
        try {
            // 解码，然后将字节转换为文件
            file = new File(fileName);
            if (!file.exists())
                file.createNewFile();
            byte[] bytes = Base64.getDecoder().decode(base64);// 将字符串转换为byte数组
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            byte[] buffer = new byte[1024];
            out = new FileOutputStream(file);
            int byteread = 0;
            while ((byteread = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteread); // 文件写操作
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (out!= null) {
                    out.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return file;
    }

    public static String fileToBase64(File file) {
        String base64 = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            base64 = Base64.getEncoder().encodeToString(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return base64;
    }

    public static void main(String[] args) {
        String base64 = TransformsBetweenFileAndBase64Utils.fileToBase64(new File("agp_test.iml"));
        System.out.println(base64);
        TransformsBetweenFileAndBase64Utils.base64ToFile(base64,"agp_test2.iml");
    }

}
