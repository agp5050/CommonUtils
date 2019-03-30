
import com.jcraft.jsch.*;

import java.io.*;
import java.util.Properties;

public class SFTP {

    public static ChannelSftp InitConnect() {
        String host = ResourceUtil.getString("ftp.server");
        int port = ResourceUtil.getInt("ftp.port");
        String username = ResourceUtil.getString("ftp.username");
        String password = ResourceUtil.getString("ftp.password");

        ChannelSftp sftp = null;
        try {
            JSch jsch = new JSch();
            jsch.getSession(username, host, port);
            Session sshSession = jsch.getSession(username, host, port);
            System.out.println("Session created.");
            sshSession.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            System.out.println("Session connected.");
            System.out.println("Opening Channel.");
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
            System.out.println("Connected to " + host + ".");
        } catch (Exception e) {
            e.printStackTrace();
//            logger.error("get channel exception !" ,e);
        }
        return sftp;
    }

    /**
     * 上传文件（将本地文件上传到FTP）
     * @param dir             约定根目录，必须存在
     * @param directory 文件次级目录，未存在时创建，可有多级
     * @param uploadFile 要上传的文件完整路径
     * @param sftp
     */
    public static boolean uploadFile(String dir,String directory, String uploadFile) {
        ChannelSftp sftp = InitConnect();
        FileInputStream fileInputStream = null;
        try {
//			logger.info("文件夹："+dir+",跳转路径："+directory+"，内容："+uploadFile);
            sftp =mkdirs(dir,directory,sftp);
            File file=new File(uploadFile);
            fileInputStream = new FileInputStream(file);
            sftp.put(fileInputStream, file.getName());
        } catch (Exception e) {
            e.printStackTrace();
//            logger.error("上传文件失败：",e);
            return false;
        }finally {
            try {
                if(fileInputStream != null){
                    fileInputStream.close();
                }
            } catch (IOException e) {
            }
            close(sftp);
        }
        return true;
    }


    /**
     * 直接在FTP写文件
     * @param dir             约定根目录，必须存在
     * @param directory 文件次级目录，未存在时创建，可有多级
     * @param uploadFile 要上传的文件完整路径
     * @param sftp
     */
    public static boolean WriteFile(String dir,String directory, String filename,String context) {
        ChannelSftp sftp = InitConnect();
        FileInputStream fileInputStream = null;
        try {
//			logger.info("文件夹："+dir+",跳转路径："+directory+"，filename："+filename+",comtext="+context);
            sftp =mkdirs(dir,directory,sftp);
            sftp.put(new ByteArrayInputStream(context.getBytes("utf-8")), filename);
        } catch (Exception e) {
//            logger.error("上传文件失败：",e);
            e.printStackTrace();
            return false;
        }finally {
            try {
                if(fileInputStream != null){
                    fileInputStream.close();
                }
            } catch (IOException e) {
            }
            close(sftp);
        }
        return true;
    }

    /**
     * 上传目录下的所有文件(批量上传)
     *
     * @Title: uploadByDirectory
     * @param dir --> server path ,    directory --> server directory located on dir , maybe multi-level
     *            uploadDirectory --> the local path which files in it will be upload.
     * @throws Exception
     */
    public static boolean uploadDirectory(String dir,String directory, String uploadDirectory) {
        ChannelSftp sftp = InitConnect();
        FileInputStream fileInputStream = null;
        try {
//            logger.info("文件夹："+dir+",跳转路径："+directory+"，上传目录："+uploadDirectory);
            sftp =mkdirs(dir,directory,sftp);
            File uploadfd = new File(uploadDirectory);
            if(!uploadfd.exists() && !uploadfd.isDirectory()){
//                logger.info("Directory = "+uploadDirectory +" is not exist ");
            }
            File[] filelist=uploadfd.listFiles();
            if(filelist != null){
                for(int i=0;i<filelist.length;i++){
                    fileInputStream = new FileInputStream(filelist[i]);
                    sftp.put(fileInputStream, filelist[i].getName());
//                    logger.info(filelist[i].getName()+" upload success");
                }

            }
        } catch (Exception e) {
//            logger.error("上传文件失败：",e);
            return false;
        }finally {
            try {
                if(fileInputStream != null){
                    fileInputStream.close();
                }
            } catch (IOException e) {
            }
            close(sftp);
        }
        return true;
    }

    /**
     * 下载文件
     * @param directory 下载目录
     * @param downloadFile 下载的文件
     * @param saveFile 存在本地的路径
     * @param sftp
     */
    public static void download(String directory, String downloadFile,String saveFile) {
        ChannelSftp sftp = InitConnect();
        try {
            sftp.cd(directory);
            File file=new File(saveFile);
            sftp.get(downloadFile, new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
//            logger.error("下载文件失败：",e);
        }finally {
            close(sftp);
        }
    }

    /**
     * 删除文件
     * @param directory 要删除文件所在目录
     * @param deleteFile 要删除的文件
     * @param sftp
     */
    public static void delete(String directory, String deleteFile) {
        ChannelSftp sftp = InitConnect();
        try {
            sftp.cd(directory);
            sftp.rm(deleteFile);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            close(sftp);
        }
    }

    private static void close(ChannelSftp sftp){
        if(!sftp.isClosed()){
            try {
                sftp.getSession().disconnect();
                sftp.quit();
                sftp.disconnect();
            } catch (JSchException e) {
                e.printStackTrace();
                e.printStackTrace();
//                logger.error("close sftp exception");
            }
        }
//        logger.info("close sftp success");
    }


    /**
     * 上传文件
     * @param dir             约定根目录，必须存在，已/开头
     * @param directory 文件次级目录，未存在时创建，可有多级，已文件名称开头
     * @param uploadFile 要上传的文件完整路径
     * @param sftp
     */
    private static ChannelSftp mkdirs(String dir,String directory, ChannelSftp sftp) {
        try {
            sftp.cd(dir);
            String[] path = directory.split("\\/");
            for(int i=0;i<path.length;i++){
                if("".equals(path[i])){
                    continue;  //为空字符串时不能进行cd,否则就跳到用户跟目录了
                }
                try{
                    sftp.mkdir(path[i]);
                    sftp.cd(path[i]);
                }catch(Exception e){
//                    logger.info("目录:"+dir+"下"+path[i]+"已存在！");
                    sftp.cd(path[i]);
                }
            }
//            logger.info("pwd="+sftp.pwd());
        } catch (Exception e) {
//            logger.error("跟目录不存在，dir="+dir);
        }
        return sftp;
    }
}
