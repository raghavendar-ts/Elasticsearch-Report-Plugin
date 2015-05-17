package app.report;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONArray;

public class MailAPI {

	Properties properties = new Properties();
	Properties eMailProperties = new Properties();
	Session session;
	MimeMessage message;
	Multipart multipart = new MimeMultipart();
	BodyPart messageBodyPart = null;

	MailAPI() {
		try {
			File jarPath = new File(MailAPI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
			String propertiesPath = jarPath.getParentFile().getAbsolutePath();
			eMailProperties.load(new FileInputStream(propertiesPath + "\\properties\\mail.properties"));
			session = Session.getInstance(eMailProperties, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		message = new MimeMessage(session);
	}

	public void setFrom(String fromLocal) {
		InternetAddress from;
		try {
			from = new InternetAddress(fromLocal);
			message.setFrom(from);
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void setSubject(String subject) {
		try {
			message.setSubject(subject);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void addRecipients(String[] eMailList) {
		for (int i = 0; i < eMailList.length; i++) {
			try {
				message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(eMailList[i]));
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	public void addRecipients(JSONArray eMailList) {
		for (int i = 0; i < eMailList.length(); i++) {
			try {
				message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(eMailList.getString(i)));
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	public void setText(String mailContent) {
		try {
			messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(mailContent);
			multipart.addBodyPart(messageBodyPart);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void attachWB(HSSFWorkbook wb, String fileName) {
		messageBodyPart = new MimeBodyPart();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataSource ds = null;
		try {
			wb.write(baos);
			byte[] bytes = baos.toByteArray();
			ds = new ByteArrayDataSource(bytes, "application/excel");
			DataHandler dh = new DataHandler(ds);
			messageBodyPart.setDataHandler(dh);
			messageBodyPart.setFileName(fileName + ".xls");
			multipart.addBodyPart(messageBodyPart);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void send() {
		Transport transport;
		try {
			message.setContent(multipart);
			transport = session.getTransport("smtp");
			transport.connect(eMailProperties.getProperty("username"), eMailProperties.getProperty("password"));
			transport.sendMessage(message, message.getAllRecipients());
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

}
