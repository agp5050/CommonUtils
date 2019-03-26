



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 字节转化的工具类
 * @date 2015-4-8
 */
public class ByteUtils {
  
	  public final static int GZIP_MAGIC = 0x8b1f;
	  
	  private static final Logger logger = LoggerFactory.getLogger(ByteUtils.class);
	  
	  /**
	     * 从输入流中构建消�?
	     * @param is 输入�?
	     * @param metadata 客户端元数据
	     * @throws IOException 读流失败则抛出异�?
	     */
	  public static  byte[]  buildMsg(InputStream is) throws  Exception {
	        byte[] messages=null;
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        
	        byte[] receiveBuf = new byte[1024];
	        int len=0;
	        while ((len = is.read(receiveBuf)) != -1) {
	        	try {
	                baos.write(receiveBuf, 0, len);
	            } catch (Exception e) {
	            	logger.error("Fail to do message check sum!", e);
	            	throw e;
	            }
	        }
	        if (baos.size() == 0) { // 判断是否有消息数�?
	            return null;
	        }
	        messages = baos.toByteArray();
	        try {
	            baos.close();
	        } catch (IOException e) {
	        	logger.error("Fail to close byte array output stream!", e);
	        	throw e;
	        }
	        if(ByteUtils.checkIfGzip(messages)) {
	        	try {
	        		return ByteUtils.unGZip(messages);
				} catch (Exception e) {
					 return messages;
				}
	        }
	        return messages;
	    }
  /**
   * gZip解压方法
   * 
   * @throws Exception
   */
  public static byte[] unGZip(byte[] data) throws Exception {
    byte[] b = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPInputStream gzip =null;
    ByteArrayInputStream bis =null;
    try {
       bis = new ByteArrayInputStream(data);
       gzip = new GZIPInputStream(bis);
      byte[] buf = new byte[1024 * 1024];
      int num = -1;
      while ((num = gzip.read(buf, 0, buf.length)) != -1) {
        baos.write(buf, 0, num);
      }
      b = baos.toByteArray();
    } catch (Throwable ex) {
      throw new Exception("UNGzip the byte [] error,please check the data format", ex);
    }finally {
    	closeStream(baos);
    	closeStream(gzip);
    	closeStream(bis);
	}
    return b;
  }
  
  private static void closeStream(Closeable clo) {
	  if(null!=clo) {
		  try {
			clo.close();
		} catch (Exception e) {
		}
	  }
  }
  
  /**
   * 数据压缩
   * 
   * @param is
   * @param os
   * @throws Exception
   */
  public static void compress(InputStream is, OutputStream os) throws Exception {
    GZIPOutputStream gos = new GZIPOutputStream(os);
    int count;
    byte data[] = new byte[1024];
    while ((count = is.read(data, 0, 1024)) != -1) {
      gos.write(data, 0, count);
    }
    gos.finish();
    gos.flush();
    gos.close();
  }
  
  /**
   * 数据压缩
   * 
   * @param data
   * @return
   * @throws Exception
   */
  public static byte[] compress(byte[] data) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(data);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    // 压缩
    compress(bais, baos);
    byte[] output = baos.toByteArray();
    baos.flush();
    baos.close();
    bais.close();
    return output;
  }

    public static byte[] transformLongtoBytes(long x) {
        byte[] v = new byte[8];
        v[0] = (byte) (x >> 56);
        v[1] = (byte) (x >> 48);
        v[2] = (byte) (x >> 40);
        v[3] = (byte) (x >> 32);
        v[4] = (byte) (x >> 24);
        v[5] = (byte) (x >> 16);
        v[6] = (byte) (x >> 8);
        v[7] = (byte) (x >> 0);

        return v;
    }

    public static long transformBytestoLong(byte[] x) {
        return ((((long) x[0] & 0xff) << 56) | (((long) x[1] & 0xff) << 48) | (((long) x[2] & 0xff) << 40)
                | (((long) x[3] & 0xff) << 32) | (((long) x[4] & 0xff) << 24) | (((long) x[5] & 0xff) << 16)
                | (((long) x[6] & 0xff) << 8) | (((long) x[7] & 0xff) << 0));

    }
    public static int byteToInt2(byte[] b) {  
        int mask=0xff;  
        int temp=0;  
        int n=0;  
        for(int i=0;i<4;i++){  
           n<<=8;  
           temp=b[i]&mask;  
           n|=temp;  
       }  
      return n;  
    }  
    
    public static byte[] intToBytes(int n) {
        byte[] b = new byte[4];
        b[3] = (byte) (n & 0xff);
        b[2] = (byte) (n >> 8 & 0xff);
        b[1] = (byte) (n >> 16 & 0xff);
        b[0] = (byte) (n >> 24 & 0xff);
        return b;
    }
    
    private static int readUByte(byte by) {
        int b = by  & 0xFF;
        if (b <= -1 || b > 255) {
        	return -1;
        }
        return b;
    }
    
    public static boolean checkIfGzip(byte[] bytes){
    	if(null==bytes || bytes.length<3) {
    		return false;
    	}
    	 if(readUByte(bytes[2])!=8){
    		 return false;
    	 }
    	 int b =readUByte(bytes[0] );
    	 int b2 =readUByte(bytes[1] );
    	 if (b == -1 || b2 == -1) {
    		 return false;
         }
         int res=(b2<< 8) | b;
         if(res!= GZIP_MAGIC){
        	 return false;
         }
         return true;
    }
    public static String bytesToHexString(byte[] src){   
        StringBuilder stringBuilder = new StringBuilder("");   
        if (src == null || src.length <= 0) {   
            return null;   
        }   
        for (int i = 0; i < src.length; i++) {   
            int v = src[i] & 0xFF;   
            String hv = Integer.toHexString(v);   
            if (hv.length() < 2) {   
                stringBuilder.append(0);   
            }   
            stringBuilder.append(hv);   
        }   
        return stringBuilder.toString();   
    }   
    
    public static byte[] hexStringToBytes(String hexString) {   
        if (hexString == null || hexString.equals("")) {   
            return null;   
        }   
        hexString = hexString.toUpperCase();   
        int length = hexString.length() / 2;   
        char[] hexChars = hexString.toCharArray();   
        byte[] d = new byte[length];   
        for (int i = 0; i < length; i++) {   
            int pos = i * 2;   
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));   
        }   
        return d;   
    }   
    
    private static byte charToByte(char c) {   
        return (byte) "0123456789abcdef".indexOf(c);   
    }  
    
    public static void main(String[] args) throws Exception{
    	int GZIP_MAGIC = 0x8b1f;
    	byte[] bu=new byte[2];
    	bu[0]=31;
    	bu[1]=-117;
    	   int v = bu[0] & 0xFF;  
    	   String kkks="[{ds}]";
    	   byte[] uu=kkks.getBytes();
    	 System.out.println(uu[0]=='[');
      String abc="https://www.baidu.com/s?ie=utf-8&f=8&rsv_bp=1&tn=baidu&wd=java%20gzip%20%E5%8E%8B%E7%BC%A9%20%E8%A7%A3%E6%9E%90&oq=java%2520gzip%2520%25E5%258E%258B%25E7%25BC%25A9%2520%25E6%2596%2587%25E4%25BB%25B6%25E5%25A4%25B4&rsv_pq=ffc0706900002614&rsv_t=5708dLBVkY66ZpLL5V8KD93CgR%2FPZAu7Xnfqp3w6teg9rdQM6UsheWFJpuk&rqlang=cn&rsv_enter=0&rsv_sug3=205&rsv_sug1=147&rsv_sug7=100&rsv_sug2=0&inputT=2306&rsv_sug4=2975";
      byte[] yy=abc.getBytes();
      byte[] b = compress(abc.getBytes());
      
      byte[] bu2=new byte[2];
  	bu2[0]=b[0];
  	bu2[1]=b[1];
  	 System.out.println(bytesToHexString(bu2));
     //b=unGZip(yy);
  	 ByteArrayInputStream bis = new ByteArrayInputStream(b);
      int a=116;
      char GZIP3=8;
      String ds=new String(b);
      System.out.println(ds.getBytes().length+"##"+abc.getBytes().length);
      System.out.println(checkIfGzip(b )+"####");
      System.out.println("finla-"+(readUShort(bis)==GZIP_MAGIC));
    }
    
    private static int readUByte(InputStream in) throws IOException {
        int b = in.read();
        if (b == -1) {
            throw new EOFException();
        }
        if (b < -1 || b > 255) {
            throw new IOException( ".read() returned value out of range -1..255: " + b);
        }
        System.out.println(b);
        return b;
    }
    private static int readUShort(InputStream in) throws IOException {
        int b = readUByte(in);
        return (readUByte(in) << 8) | b;
    }
}
