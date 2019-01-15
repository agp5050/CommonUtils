import com.bootstrap.common.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.util.Properties;

public class HtmlMailUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(HtmlMailUtil.class);
	
	public static boolean sendHtmlMail(String to, String from, String subject, String context, String filename) throws Exception {
		
		JavaMailSenderImpl mailSender = getJavaMailSender();
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", mailSender.getHost());
        props.setProperty("mail.smtp.user", mailSender.getUsername());
        props.setProperty("mail.smtp.password", mailSender.getPassword());
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        String port = String.valueOf(mailSender.getPort());
        props.setProperty("mail.smtp.port", port);
        props.setProperty("mail.smtp.socketFactory.port", port);
        Session mailSession = Session.getDefaultInstance(props, new SimpleAuthenticator(mailSender.getUsername(), mailSender.getPassword()));
		mailSession.setDebug(true);
        Transport transport = mailSession.getTransport();
        
        MimeMessage message = new MimeMessage(mailSession);
        message.setSubject(subject,"utf-8");
        message.setFrom(new InternetAddress(mailSender.getUsername()));
        message.addRecipient(Message.RecipientType.TO,
             new InternetAddress(to));
        MimeMultipart multipart = new MimeMultipart("related");

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(context, "text/html;charset=utf-8");
        multipart.addBodyPart(messageBodyPart);

        if (filename!=null) {
            messageBodyPart = new MimeBodyPart();
            FileDataSource fileds = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(fileds));
            messageBodyPart.setFileName(MimeUtility.encodeText(fileds.getName(), "UTF-8", null));
            multipart.addBodyPart(messageBodyPart);
        }
        
        message.setContent(multipart);

        transport.connect();
        transport.sendMessage(message,
            message.getRecipients(Message.RecipientType.TO));
        transport.close();
		return true;
	}

	private static JavaMailSenderImpl getJavaMailSender() {
		return (JavaMailSenderImpl) SpringContextHolder.getBean("mailSender");
	}
	
	// used for base64 string ... in some circumstances apis receive base64 string not binary file steam. then this method is propose。
	 public static boolean sendHtmlMailWithBase64String(String to,String cc,String bcc, String subject, String context, String filename,String fileBase64String) throws Exception {

        JavaMailSenderImpl mailSender = getJavaMailSender();
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", mailSender.getHost());
        props.setProperty("mail.smtp.user", mailSender.getUsername());
        props.setProperty("mail.smtp.password", mailSender.getPassword());
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        String port = String.valueOf(mailSender.getPort());
        props.setProperty("mail.smtp.port", port);
        props.setProperty("mail.smtp.socketFactory.port", port);
        Session mailSession = Session.getDefaultInstance(props, new SimpleAuthenticator(mailSender.getUsername(), mailSender.getPassword()));
        mailSession.setDebug(true);
        Transport transport = mailSession.getTransport();

        MimeMessage message = new MimeMessage(mailSession);
        message.setSubject(subject,"utf-8");
        message.setFrom(new InternetAddress(mailSender.getUsername()));
        if (StringUtils.isNotEmpty(to)){
            InternetAddress[] internetAddressTo = new InternetAddress().parse(to);
            message.setRecipients(Message.RecipientType.TO,internetAddressTo);
        }
        if (StringUtils.isNotEmpty(cc)){
            InternetAddress[] internetAddressCc = new InternetAddress().parse(cc);
            message.setRecipients(Message.RecipientType.CC,internetAddressCc);
        }
        if (StringUtils.isNotEmpty(bcc)){
            InternetAddress[] internetAddressBcc = new InternetAddress().parse(bcc);
            message.setRecipients(Message.RecipientType.BCC,internetAddressBcc);
        }


        MimeMultipart multipart = new MimeMultipart("related");

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(context, "text/html;charset=utf-8");
        multipart.addBodyPart(messageBodyPart);

        if (filename!=null && fileBase64String!=null) {
            messageBodyPart = new MimeBodyPart();
            DataSource dataSource1=new ByteArrayDataSource(Base64.getDecoder().decode(fileBase64String), "application/octet-stream");
            messageBodyPart.setDataHandler(new DataHandler(dataSource1));
            messageBodyPart.setFileName(MimeUtility.encodeText(filename,"gbk","B"));
            multipart.addBodyPart(messageBodyPart);
        }

        message.setContent(multipart);

        transport.connect();
        transport.sendMessage(message,
                message.getAllRecipients());
        transport.close();
        return true;
    }

}

 class SimpleAuthenticator extends Authenticator {
	private String username;

	private String password;

	public SimpleAuthenticator(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.username, this.password);

	}
}

