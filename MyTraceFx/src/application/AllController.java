package application;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.UUID;

import de.method.MethodTrace;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import parseXML.ParseXml;

public class AllController {
	@FXML
	private TextField connectBeginRecordClassName;

	@FXML
	private Button attachAddExcludePattern;

	@FXML
	private Button attachStopRecordDeleteArg;

	@FXML
	private Button connectAddExcludePattern;

	@FXML
	private ListView<String> connectBenginRecordMethodArgs;

	@FXML
	private Button beginConnect;

	@FXML
	private CheckBox connectRecordAllMethod;

	@FXML
	private TextField addConnectPath;

	@FXML
	private TextField attachPort;

	@FXML
	private SplitPane connectTab;

	@FXML
	private TextField connectBeginRecordMethodName;

	@FXML
	private ListView<String> connectPath;

	@FXML
	private Button connectStopRecordAddArg;

	@FXML
	private Tab AttachTab;

	@FXML
	private TextField connectClassName;

	@FXML
	private TextField attachFilterPattern;

	@FXML
	private TextField attachStopRecordMethodName;

	@FXML
	private ListView<String> attachExcludePatterns;

	@FXML
	private TextField attachBeginRecordMethodName;

	@FXML
	private TextField attachStopRecordMethodArg;

	@FXML
	private ListView<String> connectFiltersPatterns;

	@FXML
	private Button connectDelectPath;

	@FXML
	private Button attachStopRecordAddArg;

	@FXML
	private Button connectAddPathButton;

	@FXML
	private Button connectBeginRecordAddArg;

	@FXML
	private ListView<String> attachFilterPatterns;

	@FXML
	private Button attachShowRecord;

	@FXML
	private TextField connectStopRecordMethodArg;

	@FXML
	private TextField connectStopRecordMethodName;

	@FXML
	private Button connectDeleteFilterPattern;

	@FXML
	private Button connectStopRecordDeleteArg;

	@FXML
	private ListView<String> connectExcludePatterns;

	@FXML
	private Button attachBeginRecordAddArg;

	@FXML
	private TextField attachBeginRecordClassName;

	@FXML
	private ListView<String> attachStopRecordMethodArgs;

	@FXML
	private Button connectShowRecord;

	@FXML
	private TextField connectStopRecordClassName;

	@FXML
	private Button attachDeleteFilterPattern;

	@FXML
	private TextField connectExcludePattern;

	@FXML
	private ProgressBar connectProcess;

	@FXML
	private Button attachAddFilterPattern;

	@FXML
	private ListView<String> attachBeginRecordMethodArgs;

	@FXML
	private TextField attachStopRecordClassName;

	@FXML
	private Button connectBeginRecordDeleteArg;

	@FXML
	private WebView showWebView;

	@FXML
	private Button connectAddFilterPattern;

	@FXML
	private Button attachDeleteExcludePattern;

	@FXML
	private ListView<String> connectStopRecordMethodArgs;

	@FXML
	private TextField attachExcludePattern;

	@FXML
	private Tab ResultTab;

	@FXML
	private TextField connectBenginRecordMethodArg;

	@FXML
	private CheckBox attachRecordAllMethod;

	@FXML
	private Button attachBeginRecordDeleteArg;

	@FXML
	private Button attachBegin;

	@FXML
	private Button connectDeleteExcludePattern;

	@FXML
	private TextField attachBeginRecordMethodArg;

	@FXML
	private TextField connectFilterPattern;

	@FXML
	private Button connectDisconnect;

	@FXML
	void connectDisconnect(ActionEvent event) {

	}

	@FXML
	void addPath(ActionEvent event) {
		String text = addConnectPath.getText();
		connectPath.getItems().add(text);
	}

	@FXML
	void attachAddExcludePattern(ActionEvent event) {
		String text = attachExcludePattern.getText();
		attachExcludePatterns.getItems().add(text);
	}

	@FXML
	void attachAddFilterPattern(ActionEvent event) {
		String text = attachFilterPattern.getText();
		attachFilterPatterns.getItems().add(text);
	}

	@FXML
	void attachBegin(ActionEvent event) {

	}

	@FXML
	void attachBeginRecordAddArg(ActionEvent event) {
		String text = attachBeginRecordMethodArg.getText();
		attachBeginRecordMethodArgs.getItems().add(text);
	}

	@FXML
	void attachBeginRecordDeleteArg(ActionEvent event) {
		String item = attachBeginRecordMethodArgs.getSelectionModel().getSelectedItem();
		attachBeginRecordMethodArgs.getItems().remove(item);
	}

	@FXML
	void attachDeleteExcludePattern(ActionEvent event) {
		String item = attachExcludePatterns.getSelectionModel().getSelectedItem();
		attachExcludePatterns.getItems().remove(item);
	}

	@FXML
	void attachDeleteFilterPattern(ActionEvent event) {
		String item = attachFilterPatterns.getSelectionModel().getSelectedItem();
		attachFilterPatterns.getItems().remove(item);
	}

	@FXML
	void attachShowRecord(ActionEvent event) {

	}

	@FXML
	void attachStopRecordAddArg(ActionEvent event) {
		String text = attachStopRecordMethodArg.getText();
		attachStopRecordMethodArgs.getItems().add(text);
	}

	@FXML
	void attachStopRecordDeleteArg(ActionEvent event) {
		String item = attachStopRecordMethodArgs.getSelectionModel().getSelectedItem();
		attachStopRecordMethodArgs.getItems().remove(item);
	}

	File logFile;

	@FXML
	void beginConnect(ActionEvent event) throws Exception {
		if(null!=logFile){
			logFile.delete();
		}
		UUID uuid = UUID.randomUUID();
		String fileName = uuid.toString() + ".html";
		File file = new File("C:\\log\\" + fileName);
		logFile=file;

		MethodTrace trace = new MethodTrace();

		// 获取装载的类名，类路径
		String className = connectClassName.getText();
		ObservableList<String> classPath = connectPath.getItems();
		String str = "-cp ";
		for (String string : classPath) {
			if (string.contains(";"))
				str += string;
			else
				str += string + ";";
		}
		trace.setLoggerFileLocation(file);
		trace.setClassName(className);
		trace.setClassPath(str);
		trace.launchDebugee();
		// 获取排除的模式
		ObservableList<String> ExcludePatterns = connectExcludePatterns.getItems();
		trace.getMethodExclusions().addAll(ExcludePatterns);
		// 获取Filter的模式
		ObservableList<String> FilterPatterns = connectFiltersPatterns.getItems();
		trace.getMethodFilters().addAll(FilterPatterns);

		// 注册事件
		trace.registerEvent();

		if (connectRecordAllMethod.isSelected()) {
			trace.setFlag(true);
		} else {
			// 开始记录的标识
			String beginClassName = connectBeginRecordClassName.getText();
			String beginMethodName = connectBeginRecordMethodName.getText();
			ObservableList<String> beginMethodArgs = connectBenginRecordMethodArgs.getItems();
			trace.setBeginRecord(beginClassName, beginMethodName, new ArrayList<String>(beginMethodArgs));
			// 结束记录的标识
			String stopClassName = connectStopRecordClassName.getText();
			String stopMethodName = connectStopRecordMethodName.getText();
			ObservableList<String> stopMethodArgs = connectStopRecordMethodArgs.getItems();
			trace.setStopRecord(stopClassName, stopMethodName, new ArrayList<String>(stopMethodArgs));
		}

		trace.processDebuggeeVM();
		// 开始跟踪
		trace.eventLoop();

		// 摧毁虚拟机
		trace.destroyDebuggeeVM();
		connectProcess.setProgress(100.00);
		System.out.println(fileName);
	}

	@FXML
	void connectAddExcludePattern(ActionEvent event) {
		String text = connectExcludePattern.getText();
		connectExcludePatterns.getItems().add(text);
	}

	@FXML
	void connectAddFilterPattern(ActionEvent event) {
		String text = connectFilterPattern.getText();
		connectFiltersPatterns.getItems().add(text);

	}

	@FXML
	void connectBeginRecordAddArg(ActionEvent event) {
		String text = connectBenginRecordMethodArg.getText();
		connectBenginRecordMethodArgs.getItems().add(text);
	}

	@FXML
	void connectBeginRecordDeleteArg(ActionEvent event) {
		String item = connectBenginRecordMethodArgs.getSelectionModel().getSelectedItem();
		connectBenginRecordMethodArgs.getItems().remove(item);
	}

	@FXML
	void connectDeleteExcludePattern(ActionEvent event) {
		String item = connectExcludePatterns.getSelectionModel().getSelectedItem();
		connectExcludePatterns.getItems().remove(item);
	}

	@FXML
	void connectDeleteFilterPattern(ActionEvent event) {
		String item = connectFiltersPatterns.getSelectionModel().getSelectedItem();
		connectFiltersPatterns.getItems().remove(item);
	}

	@FXML
	void connectShowRecord(ActionEvent event) {
		if(null!=logFile)
		showWebView.getEngine().load("file:///"+logFile.getPath());
	}

	@FXML
	void connectStopRecordAddArg(ActionEvent event) {
		String text = connectStopRecordMethodArg.getText();
		connectStopRecordMethodArgs.getItems().add(text);
	}

	@FXML
	void connectStopRecordDeleteArg(ActionEvent event) {
		String item = connectStopRecordMethodArgs.getSelectionModel().getSelectedItem();
		connectStopRecordMethodArgs.getItems().remove(item);
	}

	@FXML
	void delectPath(ActionEvent event) {
		String item = connectPath.getSelectionModel().getSelectedItem();
		connectPath.getItems().remove(item);
	}

}
