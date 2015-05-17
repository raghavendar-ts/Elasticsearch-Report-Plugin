package app.report;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
//import java.nio.file.Files;
//import java.nio.file.Paths;


public class ESReportMain {
	public static void main(String[] args) {
		String config = null;
		String filePath = args[0];
		try {
			FileInputStream input = new FileInputStream(filePath);
			byte[] fileData = new byte[input.available()];
			input.read(fileData);
			input.close();
			config = new String(fileData, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ESReport s = new ESReport();
		s.process(config);
	}
}
