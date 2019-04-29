package com.jffox.cloud.controller;

import com.jffox.cloud.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@RestController
public class SimpleFileUploadController {
    public static String fileDir=System.getProperty("user.dir")+ File.separator+"python"+File.separator;
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    @PostConstruct
    void initUploadDir(){
        File dir = new File(fileDir);
        if (!dir.exists() || dir.isFile()){
            boolean mkdirs = dir.mkdirs();
            log.info("mk upload dir {}",mkdirs);
            if (!mkdirs){
                System.exit(1);
            }
        }
    }
    @RequestMapping("/upload")
    public Result upload(@RequestParam MultipartFile file){
        if (file!=null){
            String fileName = file.getOriginalFilename();
            if (StringUtils.isEmpty(fileName)) return Result.INTERNAL_EXCEPTION;
            String fullFileName=fileDir+fileName;
            File fileupload=new File(fullFileName);
            try{
                if (fileupload.exists()){
                    renameFile(fileupload);
                }
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileupload));
                out.write(file.getBytes());
                out.flush();
                out.close();
            }catch (IOException e){
                log.info(e.getMessage());
                e.printStackTrace();
            }catch (Exception e){
                log.info(e.getMessage());
                e.printStackTrace();
            }

        }

        return Result.SUCCESS;
    }

    private void renameFile(File fileupload) {
        String format = formatter.format(new Date());
        String absolutePath = fileupload.getAbsolutePath();
        int dotIndex = absolutePath.lastIndexOf(".");
        String newFileName=absolutePath.substring(0,dotIndex)+"_"+format+absolutePath.substring(dotIndex);
        fileupload.renameTo(new File(newFileName));
    }

}
