package com.xuexi.controller;

import com.xuexi.constant.Global;
import com.xuexi.entity.MdCommonUser;
import com.xuexi.service.FileUploadService;
import com.xuexi.utils.UtilTools;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @Author Mr.An
 * @Date 18/6/1 上午9:57
 */
@Controller
public class FileUploadController {

    private final static Logger LOGGER=Logger.getLogger(FileUploadController.class);
    private final static String FORMALDIR="task";
    @Autowired
    private FileUploadService fileUploadService;
    private final String[] fileType = { ".txt", ".sql", ".doc", ".docx", ".xls",".xlsx" };
    @ResponseBody
    @RequestMapping(value = "/api/v1/tmp/upload",method = RequestMethod.POST)
    private String uploadTmp(HttpServletRequest request) throws IOException {

        File folder = new File(Global.TMP_UPLOAD_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        try{
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setHeaderEncoding("UTF-8");
            if(!ServletFileUpload.isMultipartContent(request)){
                return Global.GET_FAILURE_JSON("未包含文件上传域,上传格式不是multipart/form-data或者multipart/mixed");// 未包含文件上传域
            }
            List<FileItem> list = upload.parseRequest(request);
            for(FileItem item : list){
                if(item.isFormField()){
                    String name = item.getFieldName();

                    String value = item.getString("UTF-8");
                    if(name.equals("jobname")&&StringUtils.isEmpty(value)){

                        return Global.GET_FAILURE_JSON("JobName必须指定");
                    }
                    System.out.println(name + "=" + value);}
                else {
                    String filename = item.getName();
                    if (StringUtils.isEmpty(filename)){
                        return Global.GET_FAILURE_JSON("未选择附件。");
                    }
                    LOGGER.info(filename+"///filename");
                    if(!checkFileType(filename)){
                        return Global.GET_FAILURE_JSON("附件格式不允许（必须是sql,txt,doc,docx,xls,xlsx）");
                    }
                    filename = filename.substring(filename.lastIndexOf(File.separator)+1);
                    InputStream in = item.getInputStream();
                    String fullname=folder.getAbsolutePath() + File.separator + filename;
                    LOGGER.info(fullname+"//fullname");
                    File file = new File(fullname);
                    if (file.exists()){
                        file.delete();
                    }
                    FileOutputStream out = new FileOutputStream(file);
                    byte buffer[] = new byte[1024];
                    int len = 0;
                    while((len=in.read(buffer))>0){
                        out.write(buffer, 0, len);
                    }
                    in.close();
                    out.close();
                    item.delete();

                }
            }
        }catch (Exception e) {
            e.printStackTrace();

        }
        return Global.SUCCESS_JSON;




    }
    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    private String upload(@RequestParam("fileupload") MultipartFile myfile,HttpServletRequest request) throws IOException {
        MdCommonUser user = (MdCommonUser)request.getSession().getAttribute(Global.USER_SESSION_KEY);
        if (user == null || StringUtils.isEmpty(user.getNameEn())) {
            return Global.GET_FAILURE_JSON("用户名非法");
        }
        String jobname = request.getParameter("jobname");
        if (StringUtils.isEmpty(jobname)){
            return Global.GET_FAILURE_JSON("jobname属性缺失，请提供jobname名称");
        }
        if (myfile.isEmpty()) {
            return Global.GET_FAILURE_JSON("未包含文件上传域,上传格式不是multipart/form-data或者multipart/mixed");// 未包含文件上传域
        } else {
            if (!checkFileType(myfile.getOriginalFilename())) {
                return Global.GET_FAILURE_JSON("不允许的文件格式");// 不允许的文件格式
            }

            // 创建文件夹
            StringBuilder folderStr = new StringBuilder("~"+File.separator+FORMALDIR);
            File folder = new File(folderStr.toString());
            if (!folder.exists()) {
                folder.mkdirs();
            }

            // 产生文件名
            String fileSuffix = UtilTools.getExtention(myfile.getOriginalFilename());
            String fileName = user.getNameEn()+"_"+myfile.getName()+ fileSuffix;

            Path path = fileUploadService.fileUpload(Paths.get(fileName));
            if (path!=null){
                return Global.SUCCESS_JSON;
            }else{
                return Global.FAIL_JSON;
            }
        }


    }


        private boolean checkFileType(String fileName) {
        Iterator<String> type = Arrays.asList(this.fileType).iterator();
        while (type.hasNext()) {
            String ext = type.next();
            if (fileName.toLowerCase().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }


}
