/**
 * Copyright(c) 2013-2015 by Puhuifinance Inc.
 * All Rights Reserved
 */
package com.puhui.message.util;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestUtils {
    private static final Logger logger = LoggerFactory.getLogger(RequestUtils.class);
    public static final String METHOD_GET = "GET";

    /**
     * 取客户端ip地址
     *
     * @param request
     * @return
     */
    public static String getRemoteAddr(HttpServletRequest request) {

        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if ("127.0.0.1".equals(ip)) {
                // 根据网卡取本机配置的ip
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        // 对于通过多个代理的情况，第一个IP为真实的IP，多个IP按照','分割
        if (ip != null && ip.length() > 15 && ip.indexOf(',') > -1) {
            ip = ip.substring(0, ip.indexOf(','));
        }
        return ip;
    }

    public static String postSend(HttpClient httpClient, NameValuePair[] data, String url) {
        PostMethod post = new PostMethod(url);
        try {
            post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8"); // 在头文件中设置转码
            post.setRequestBody(data);
            int resultCode = httpClient.executeMethod(post);
            if (HttpStatus.SC_OK != resultCode) {
                if (url.contains("AudioSend")) {
                    throw new MessageServiceException("语音接口访问异常");
                } else {
                    throw new MessageServiceException("短信接口访问异常");
                }
            }
            return post.getResponseBodyAsString().trim();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new MessageServiceException(e.getMessage());
        } finally {
            post.releaseConnection();
        }
    }

    public static String sendHttpsPost(String url, List<org.apache.http.NameValuePair> param, String data) {
        StringBuilder result = null;

        // 使用此工具可以将键值对编码成"Key=Value&Key2=Value2&Key3=Value3”形式的请求参数
        String requestParam = URLEncodedUtils.format(param, "UTF-8");
        try {
            // 设置SSLContext
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] { new MyX509TrustManager() }, null);

            // 打开连接
            // 要发送的POST请求url?Key=Value&Key2=Value2&Key3=Value3的形式
            URL requestUrl = new URL(url + "?" + requestParam);
            HttpsURLConnection httpsConn = (HttpsURLConnection) requestUrl.openConnection();

            // 设置套接工厂
            httpsConn.setSSLSocketFactory(sslcontext.getSocketFactory());

            // 加入数据
            httpsConn.setRequestMethod("POST");
            httpsConn.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(httpsConn.getOutputStream());
            if (data != null) {
                out.writeBytes(data);
            }

            out.flush();
            out.close();

            // 获取输入流
            BufferedReader in = new BufferedReader(new InputStreamReader(httpsConn.getInputStream()));
            int code = httpsConn.getResponseCode();
            if (HttpsURLConnection.HTTP_OK == code) {
                String temp = in.readLine();
                /* 连接成一个字符串 */
                while (temp != null) {
                    if (result != null) {
                        result.append(temp);
                    } else {
                        result = new StringBuilder(temp);
                    }
                    temp = in.readLine();
                }
            }
        } catch (Exception e) {
            logger.error("请求服务出错", e);
        }

        return Optional.ofNullable(result).map(StringBuilder::toString).orElse(null);
    }

    /**
     * 发起https请求并获取结果
     *
     * @param requestUrl
     *            请求地址
     * @param outputStr
     *            提交的数据
     * @return 请求结果
     */
    public static String sendHttpsGet(String requestUrl, String outputStr) {
        StringBuilder buffer = new StringBuilder();
        String result = null;
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = { new MyX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            URL url = new URL(requestUrl);
            HttpsURLConnection httpUrlConn = (HttpsURLConnection) url.openConnection();
            httpUrlConn.setSSLSocketFactory(ssf);

            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            // 设置请求方式（GET/POST）
            httpUrlConn.setRequestMethod("GET");
            httpUrlConn.connect();

            // 当有数据需要提交时
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }

            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 释放资源
            inputStream.close();
            httpUrlConn.disconnect();
            result = buffer.toString();
        } catch (ConnectException ce) {
            logger.error("server connection timed out.", ce);
        } catch (Exception e) {
            logger.error("https request error:{}", e);
        }
        return result;
    }

    public static String getSend(HttpClient httpClient, String url) throws IOException {
        GetMethod get = new GetMethod(url);
        try {
            get.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8"); // 在头文件中设置转码
            int resultCode = httpClient.executeMethod(get);
            if (HttpStatus.SC_OK == resultCode) {
                return get.getResponseBodyAsString().trim();
            } else {
                throw new MessageServiceException("短信接口访问异常,返回code:" + resultCode);
            }
        } finally {
            get.releaseConnection();
        }
    }

    public static String getSend(HttpClient httpClient, String url, Header header) {
        GetMethod get = new GetMethod(url);
        try {
            get.addRequestHeader(header); // 在头文件中设置转码
            int resultCode = httpClient.executeMethod(get);
            if (HttpStatus.SC_OK == resultCode) {
                return get.getResponseBodyAsString().trim();
            } else {
                throw new MessageServiceException("短信接口访问异常,返回code:" + resultCode);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new MessageServiceException(e.getMessage());
        } finally {
            get.releaseConnection();
        }
    }

}
