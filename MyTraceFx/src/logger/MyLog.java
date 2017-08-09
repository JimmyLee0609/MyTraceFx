package logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MyLog {
	File file;

	StringBuilder builder;
	FileWriter fileWriter;

	public MyLog(File file) {
		this.file = file;
		try {
			fileWriter=new FileWriter(file,true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		builder = new StringBuilder(65535);
		builder.append(loggerHeader());
	}

	public MyLog(String fileName) {
		this(new File(fileName));
	}

	public MyLog() {
		this(MyLog.class.getResource("Mylog.class").getPath());
	}

	public static void main(String[] args) {
		File file = new File("C:\\log\\f9f082dc-a8da-47c7-9427-187d42a33dec.html");
		MyLog log = new MyLog(file);
		// logger样式
		log.log(loggerHeader());
		// 表头
		log.log("<table cellspacing='0'> <tr class='header'> <td class='Message'>Message</td></tr>");
		// 表体
		log.log("<tr class='info even'><td class='Message'>VM started</td></tr>");
		log.log("<tr class='info odd'><td class='Message'>--ClassPrepare: java.lang.invoke.MethodHandle @main</td></tr>");
		log.log("<tr class='info even'><td class='Message'>--ClassPrepare: java.lang.invoke.MethodHandleImpl @main</td></tr>");

		log.log("</body></html>");
		log.save();
	}

	private static String loggerHeader() {
		StringBuilder sb = new StringBuilder();
		// logger样式
		sb.append(
				"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>");
		sb.append("<html>  <head>    <title> Logger Messages</title>");
		sb.append("<META http-equiv='content-type' content='text/html; charset=GBK'>");
		sb.append("<style  type='text/css'>");
		sb.append("table { margin-left: 2em; margin-right: 2em; border-left: 2px solid #AAA; }");
		sb.append("TR.even { background: #FFFFFF; }");
		sb.append("TR.odd { background: #EAEAEA; }");
		sb.append("TR.warn TD.Level, TR.error TD.Level, TR.fatal TD.Level {font-weight: bold; color: #FF4040 }");
		sb.append("TD { padding-right: 1ex; padding-left: 1ex; border-right: 2px solid #AAA; }");
		sb.append("TD.Time, TD.Date { text-align: right; font-family: courier, monospace; font-size: smaller; }");
		sb.append("TD.Thread { text-align: left; }");
		sb.append("TD.Level { text-align: right; }");
		sb.append("TD.Logger { text-align: left; }");
		sb.append("TR.header { background: #596ED5; color: #FFF; font-weight: bold; font-size: larger; }");
		sb.append("TD.Exception { background: #A2AEE8; font-family: courier, monospace;}");
		sb.append("</style>");
		sb.append("</head>");
		sb.append("<body> <hr/> <p>Log session start time Wed Aug 09 16:17:23 GMT+08:00 2017</p><p></p>");
		sb.append("<table cellspacing='0'> <tr class='header'> <td class='Message'>Message</td></tr>");
		return sb.toString();
	}

	boolean flag = false;

	public void log(String detail) {
		if (flag) {
			builder.append("<tr class='info even'>" + detail);
			flag = false;
		} else {
			builder.append("<tr class='info odd'>" + detail);
			flag = true;
		}
		if (builder.length() > 62535) {
			try {
				fileWriter.append(builder.toString());
				fileWriter.flush();
				int end = builder.length();
				builder.delete(0, end);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void save() {
		try {
			fileWriter.append(builder.append("</table></body></html>").toString());
			fileWriter.flush();
			int end = builder.length();
			builder.delete(0, end);
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setFile(File file) {
		this.file = file;
		try {
			fileWriter=new FileWriter(file,true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
