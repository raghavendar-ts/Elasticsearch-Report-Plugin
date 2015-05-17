package app.report;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.json.JSONArray;
import org.json.JSONObject;

public class ESReport {
	int i = 0;
	HSSFWorkbook wb = new HSSFWorkbook();
	HSSFSheet sheet;
	HSSFRow row;
	HSSFCell cell;
	HSSFFont font = wb.createFont();
	HSSFCellStyle data_style;
	HSSFCellStyle title_style;
	HSSFCellStyle header_style;
	ScriptEngineManager mgr = new ScriptEngineManager();
	ScriptEngine engine = mgr.getEngineByName("JavaScript");

	int rownumber = 0;

	// INPUT PARAMETERS
	String index;
	String type;
	String host = "localhost";
	String clusterName;
	String config;
	String statement;
	String description;
	String reportTitle;
	String routing = "";
	String nullValue = "NULL";
	int batchsize = 250;
	JSONArray configObj;
	JSONObject queryObj;
	JSONObject reportAccessType;
	JSONObject valueMapping;
	Settings settings;
	Client esclient;

	int k = 0;
	long hitscount = 0;
	int rows_fetched = 0;
	int y = 0;

	ESReport() {
		System.out.println("Initializing Constructor");
		setStyles();
	}

	public ESReport(Client client) {
		System.out.println("Initializing Constructor");
		setStyles();
		this.esclient = client;
	}

	private void setStyles() {
		setDataStyle();
		setTitleStyle();
		setHeaderStyle();
	}

	private void setHeaderStyle() {
		header_style = wb.createCellStyle();
		font.setFontHeightInPoints((short) 11);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setColor(HSSFColor.WHITE.index);
		header_style.setFont(font);
		header_style.setFillForegroundColor(HSSFColor.ROYAL_BLUE.index);
		header_style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
	}

	private void setTitleStyle() {
		title_style = wb.createCellStyle();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setColor(HSSFColor.WHITE.index);
		font.setFontHeightInPoints((short) 14);
		title_style.setFont(font);
		title_style.setFillForegroundColor(HSSFColor.ROYAL_BLUE.index);
		title_style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
	}

	private void setDataStyle() {
		data_style = wb.createCellStyle();
		data_style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		data_style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		data_style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		data_style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
	}

	public void process(String inputStr) {
		i = 0;
		System.out.println("Process Started");

		JSONObject input = new JSONObject(inputStr);
		initializeParameters(input);

		sheet = wb.createSheet(reportTitle);

		setTitle();
		setHeaders();

		System.out.println("Building Excel Report");
		do {
			queryObj.put("from", batchsize * k);
			SearchResponse response = null;
			if (routing.equals("")) {
				response = esclient.prepareSearch(index).setTypes(type).setSource(queryObj.toString()).execute().actionGet();
			} else {
				response = esclient.prepareSearch(index).setTypes(type).setRouting(routing).setSource(queryObj.toString()).execute().actionGet();
			}

			SearchHits hits = response.getHits();
			hitscount = hits.totalHits();
			buildDataLayout(hits);
			System.out.println("Processed " + Integer.valueOf((batchsize * k) + batchsize) + " of " + hitscount);
			k++;
			rows_fetched = batchsize * k;
		} while (rows_fetched < hitscount);
		System.out.println("Finished processing data");
		formatExcelSheet();

		reportAccess(wb, reportAccessType);
		esclient.close();
	}

	private void initializeParameters(JSONObject input) {
		System.out.println("Initializing Input Parameters");

		System.out.println(input);
		index = input.getString("index");
		type = input.getString("type");
		host = "localhost";
		clusterName = input.getString("clusterName");
		config = input.get("config").toString();
		statement = input.get("statement").toString();
		description = input.getString("description");
		reportTitle = input.getString("reportTitle");

		if (input.has("valueMapping")) {
			valueMapping = input.getJSONObject("valueMapping");
		}
		if (input.has("host")) {
			host = input.getString("host");
		}
		if (input.has("routing")) {
			routing = input.getString("routing");
		}
		if (input.has("batchSize")) {
			batchsize = input.getInt("batchSize");
		}
		if (input.has("nullValue")) {
			nullValue = input.getString("nullValue");
		}

		configObj = new JSONArray(config);
		queryObj = new JSONObject(statement);

		// queryObj.put("fields", buildQueryFields(configObj));
		queryObj.put("size", batchsize);
		reportAccessType = input.getJSONObject("reportAccess");
		reportAccessType.put("description", description);

		settings = ImmutableSettings.settingsBuilder().put("client.transport.sniff", true).put("cluster.name", clusterName).build();
		if (esclient == null) {
			esclient = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(host, 9300));
		}

		k = 0;
		hitscount = 0;
		rows_fetched = 0;
	}

	private void setTitle() {
		System.out.println("Setting Title and Headers");

		row = sheet.createRow(rownumber);
		rownumber++;

		cell = row.createCell((short) 0);
		cell.setCellValue(reportTitle);
		cell.setCellStyle(title_style);

		for (int i = 1; i < configObj.length(); i++) {
			cell = row.createCell((short) i);
			cell.setCellStyle(title_style);
		}
	}

	private void setHeaders() {
		for (int i = 1; i < configObj.length(); i++) {
			cell = row.createCell((short) i);
			cell.setCellStyle(title_style);
		}

		row = sheet.createRow(rownumber);
		for (int i = 0; i < configObj.length(); i++) {
			cell = row.createCell((short) i);
			JSONObject headerJSON = (JSONObject) configObj.get(i);
			cell.setCellValue(headerJSON.getString("title"));
			cell.setCellStyle(header_style);
		}
		rownumber++;
	}

	private void buildDataLayout(SearchHits hits) {
		// For each row
		for (int i = 0; i < hits.getHits().length; i++) {
			// Row n
			Map<String, SearchHitField> responseFields = hits.getAt(i).getFields();
			row = sheet.createRow(rownumber);
			for (int j = 0; j < configObj.length(); j++) {
				cell = row.createCell((short) j);

				JSONObject headerJSON = (JSONObject) configObj.get(j);
				// JSONArray fields = headerJSON.getJSONArray("fields");
				String format = null;

				format = headerJSON.getString("format");
				format = getExprValue(responseFields, format);

				cell.setCellValue(format);
			}
			rownumber++;
		}
	}

	private String getExprValue(Map<String, SearchHitField> responseFields, String format) {
		String exprTemp = format;
		int exprIndexSize = 0;

		int startIndexCount = StringUtils.countMatches(exprTemp, "[");
		int endIndexCount = StringUtils.countMatches(exprTemp, "]");

		if (startIndexCount == endIndexCount) {
			exprIndexSize = startIndexCount;
		}

		for (int i = 0; i < exprIndexSize; i++) {

			JSONObject exprIndex = getExprIndex(exprTemp);
			String elementeryExpr = exprTemp.substring(exprIndex.getInt("startIndex") + 1, exprIndex.getInt("endIndex"));
			String[] elementeryExprArray = elementeryExpr.split(",");

			// 0 getValue
			// 1 getDValue
			// 2 Length
			// 3 Format Number Length
			// 4 Sub String
			// 5 Character at index
			// 6 Calculate
			// 7 Range
			// 8 Array indexOf(int value)
			// 9 Array indexOf(String value)
			// 10 Array valueAt(index)

			if (elementeryExprArray[0].equals("0")) {
				String t = getValue(responseFields, elementeryExprArray[1]);
				t = getExprValue(responseFields, t);
				exprTemp = exprTemp.replaceFirst(Pattern.quote(exprTemp.substring(exprIndex.getInt("startIndex"), exprIndex.getInt("endIndex") + 1)), t);
			}

			if (elementeryExprArray[0].equals("1")) {
				String t = getDependencyValue(responseFields, elementeryExprArray[1], elementeryExprArray[2]);
				t = getExprValue(responseFields, t);
				exprTemp = exprTemp.replaceFirst(Pattern.quote(exprTemp.substring(exprIndex.getInt("startIndex"), exprIndex.getInt("endIndex") + 1)), t);
			}

			if (elementeryExprArray[0].equals("2")) {
				String t = getStringLength(elementeryExprArray[1]);
				exprTemp = exprTemp.replaceFirst(Pattern.quote(exprTemp.substring(exprIndex.getInt("startIndex"), exprIndex.getInt("endIndex") + 1)), t);
			}

			if (elementeryExprArray[0].equals("3")) {
				String t = getFormatNumberLength(elementeryExprArray[1], Integer.valueOf(elementeryExprArray[2]));
				exprTemp = exprTemp.replaceFirst(Pattern.quote(exprTemp.substring(exprIndex.getInt("startIndex"), exprIndex.getInt("endIndex") + 1)), t);
			}

			if (elementeryExprArray[0].equals("4")) {
				String t = getSubString(elementeryExprArray[1], Integer.valueOf(elementeryExprArray[2]), Integer.valueOf(elementeryExprArray[3]));
				exprTemp = exprTemp.replaceFirst(Pattern.quote(exprTemp.substring(exprIndex.getInt("startIndex"), exprIndex.getInt("endIndex") + 1)), t);
			}

			if (elementeryExprArray[0].equals("5")) {
				String t = getCharacter(elementeryExprArray[1], Integer.valueOf(elementeryExprArray[2]));
				exprTemp = exprTemp.replaceFirst(Pattern.quote(exprTemp.substring(exprIndex.getInt("startIndex"), exprIndex.getInt("endIndex") + 1)), t);
			}

			if (elementeryExprArray[0].equals("6")) {
				String t = getComputedString(elementeryExprArray[1]);
				exprTemp = exprTemp.replaceFirst(Pattern.quote(exprTemp.substring(exprIndex.getInt("startIndex"), exprIndex.getInt("endIndex") + 1)), t);
			}

			if (elementeryExprArray[0].equals("7")) {
				String t = getRange(elementeryExprArray[1], elementeryExprArray[2]);
				t = getExprValue(responseFields, t);
				exprTemp = exprTemp.replaceFirst(Pattern.quote(exprTemp.substring(exprIndex.getInt("startIndex"), exprIndex.getInt("endIndex") + 1)), t);
			}

			if (elementeryExprArray[0].equals("8")) {
				String t = getArrayIndexOf(responseFields, elementeryExprArray[1], Integer.valueOf(elementeryExprArray[2]));
				exprTemp = exprTemp.replaceFirst(Pattern.quote(exprTemp.substring(exprIndex.getInt("startIndex"), exprIndex.getInt("endIndex") + 1)), String.valueOf(t));
			}

			if (elementeryExprArray[0].equals("9")) {
				String t = getArrayIndexOf(responseFields, elementeryExprArray[1], elementeryExprArray[2]);
				exprTemp = exprTemp.replaceFirst(Pattern.quote(exprTemp.substring(exprIndex.getInt("startIndex"), exprIndex.getInt("endIndex") + 1)), String.valueOf(t));
			}

			if (elementeryExprArray[0].equals("10")) {
				String t = getArrayValueAt(responseFields, elementeryExprArray[1], Integer.valueOf(elementeryExprArray[2]));
				exprTemp = exprTemp.replaceFirst(Pattern.quote(exprTemp.substring(exprIndex.getInt("startIndex"), exprIndex.getInt("endIndex") + 1)), t);
			}
		}

		return exprTemp;
	}

	// ProcessType: 0
	private String getValue(Map<String, SearchHitField> responseFields, String fieldName) {
		if (responseFields.containsKey(fieldName)) {
			SearchHitField fieldValueObj = responseFields.get(fieldName);
			return fieldValueObj.getValue().toString();
		} else {
			return nullValue;
		}
	}

	// ProcessType: 1
	private String getDependencyValue(Map<String, SearchHitField> responseFields, String valueMappingKey, String value) {
		JSONObject tempMapping = valueMapping.getJSONObject(valueMappingKey);
		if (tempMapping.has(value)) {
			return tempMapping.getString(value);
		} else {
			return "-";
		}
	}

	// ProcessType: 2
	private String getStringLength(String fieldValue) {
		if (!fieldValue.equals(nullValue)) {
			return String.valueOf(fieldValue.length());
		} else {
			return "-";
		}
	}

	// ProcessType: 3
	private String getFormatNumberLength(String fieldValue, Integer formatNumberLength) {
		String format = StringUtils.repeat("0", formatNumberLength);
		DecimalFormat mFormat = new DecimalFormat(format);
		if (StringUtils.isNumeric(fieldValue)) {
			return mFormat.format(Integer.valueOf(fieldValue));
		} else {
			return "-";
		}
	}

	// ProcessType: 4
	private String getSubString(String fieldValue, int from, int end) {
		if (!fieldValue.equals("-")) {
			return fieldValue.substring(from, end);
		} else {
			return fieldValue;
		}
	}

	// ProcessType: 5
	private String getCharacter(String fieldValue, int index) {
		if (index < fieldValue.length() && !fieldValue.equals(nullValue)) {
			return String.valueOf(fieldValue.charAt(index));
		} else {
			return nullValue;
		}
	}

	// ProcessType: 6
	private String getComputedString(String fieldValue) {
		if (!fieldValue.equals(nullValue) && !fieldValue.equals("")) {
			try {
				return String.valueOf(engine.eval(fieldValue));
			} catch (ScriptException e) {
				return nullValue;
			}
		}
		return nullValue;
	}

	// ProcessType: 7
	private String getRange(String valueMappingKey, String fieldValue) {
		System.out.println(valueMappingKey + ":" + fieldValue);
		JSONObject tempMapping = valueMapping.getJSONObject(valueMappingKey);

		if (!fieldValue.equals(nullValue) && !fieldValue.equals("")) {
			Iterator<String> keys = tempMapping.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				String keyTemp = key;
				key = key.replace("x", fieldValue);
				try {
					if ((Boolean) engine.eval(key)) {
						return tempMapping.getString(keyTemp);
					}
				} catch (ScriptException e) {
					return nullValue;
				}
			}
		}
		if (tempMapping.has("default")) {
			return tempMapping.getString("default");
		}
		return nullValue;
	}

	// ProcessType: 8
	private String getArrayIndexOf(Map<String, SearchHitField> responseFields, String fieldName, int value) {
		try {
			return String.valueOf(responseFields.get(fieldName).getValues().indexOf(value));
		} catch (Exception e) {
			return nullValue;
		}
	}

	// ProcessType: 9
	private String getArrayIndexOf(Map<String, SearchHitField> responseFields, String fieldName, String value) {
		try {
			return String.valueOf(responseFields.get(fieldName).getValues().indexOf(value));
		} catch (Exception e) {
			return nullValue;
		}
	}

	// ProcessType 10
	private String getArrayValueAt(Map<String, SearchHitField> responseFields, String fieldName, int arrayIndex) {
		try {
			return String.valueOf(responseFields.get(fieldName).getValues().get(arrayIndex));
		} catch (Exception e) {
			return nullValue;
		}
	}

	private JSONObject getExprIndex(String exprTemp) {
		int startIndex = 0;
		int endIndex = 0;

		for (int i = 0; i < exprTemp.length(); i++) {
			if (exprTemp.substring(i, i + 1).equals("[")) {
				startIndex = i;
				continue;
			}
			if (exprTemp.substring(i, i + 1).equals("]")) {
				endIndex = i;
				break;
			}
		}

		JSONObject exprIndex = new JSONObject();
		exprIndex.put("startIndex", startIndex);
		exprIndex.put("endIndex", endIndex);

		return exprIndex;
	}

	public void reportAccess(HSSFWorkbook wb2, JSONObject reportAccess) {
		DecimalFormat mFormat = new DecimalFormat("00");
		Calendar date = new GregorianCalendar();
		String fileName = reportAccess.getString("fileName");
		fileName += "_" + date.get(Calendar.YEAR) + mFormat.format(Integer.valueOf(date.get(Calendar.MONTH) + 1)) + date.get(Calendar.DAY_OF_MONTH) + "_"
				+ mFormat.format(date.get(Calendar.HOUR_OF_DAY)) + mFormat.format(date.get(Calendar.MINUTE));
		JSONObject reportAccessType = null;

		if (reportAccess.has("ftp")) {
			System.out.println("Saving file for FTP access");
			reportAccessType = reportAccess.getJSONObject("ftp");
			reportAccessType.put("description", reportAccess.getString("description"));
			reportAccessTypeFile(wb, reportAccessType, fileName);
		}

		if (reportAccess.has("email")) {
			System.out.println("Sending E-Mail...");
			reportAccessType = reportAccess.getJSONObject("email");
			reportAccessType.put("description", reportAccess.getString("description"));
			reportAccessTypeEMail(wb, reportAccessType, fileName);
		}
	}

	public void reportAccessTypeEMail(HSSFWorkbook localwb, JSONObject reportAccessTypeEMail, String fileName) {
		JSONArray eMailList = reportAccessTypeEMail.getJSONArray("deliverTo");
		MailAPI mailAPI = new MailAPI();
		// mailAPI.setFrom(fromEMail);
		mailAPI.setSubject(reportAccessTypeEMail.getString("subject"));
		mailAPI.addRecipients(eMailList);
		mailAPI.setText(description);
		mailAPI.attachWB(localwb, fileName);
		mailAPI.send();
		System.out.println("E-Mail Sent");
	}

	public void reportAccessTypeFile(HSSFWorkbook localWB, JSONObject reportAccessTypeFile, String fileName) {
		FileOutputStream out;
		try {
			out = new FileOutputStream(reportAccessTypeFile.getString("filePath") + "//" + fileName + ".xls");
			localWB.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void formatExcelSheet() {
		for (int i = 0; i < configObj.length(); i++) {
			try {
				sheet.autoSizeColumn((short) i);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
