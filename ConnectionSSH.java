package com.jikexueyuan.demo.springmvc.lesson6.utils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConnectionSSH {

    private final static String SHELLFile="/Users/finup/software/apache-tomcat-8.5.31/webapps/ROOT/cmd/AutoPullPush.sh";

    public static void exec_shell(String username,String host,int port,String shellPath){
     try{
         JSch jsch = new JSch();
         String pubKeyPath = "~/.ssh/id_rsa";
         jsch.addIdentity(pubKeyPath);
         String user = username;
         String host_str = host;
         Session session=jsch.getSession(user, host_str, port);//为了连接做准备
         session.setConfig("StrictHostKeyChecking", "no");
         session.connect();
         String command = "/bin/bash "+shellPath;
         ChannelExec channel=(ChannelExec)session.openChannel("exec");
         channel.setCommand(command);
         BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
         channel.connect();
         String msg;
         while((msg = in.readLine()) != null){
             System.out.println(msg);
         }
         channel.disconnect();
         session.disconnect();

     }catch (Exception e){
         System.out.println(e.getMessage());
     }
    }
    public static void main(String[] args) throws JSchException, IOException {
        ConnectionSSH.exec_shell("root","node1",22,SHELLFile);
    }
}