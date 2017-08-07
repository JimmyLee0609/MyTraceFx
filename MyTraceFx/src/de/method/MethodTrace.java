package de.method;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;
import com.sun.jdi.request.VMDeathRequest;



public class MethodTrace {
	private static final Logger logger = LoggerFactory.getLogger("MethodTrace");
	private VirtualMachine vm;
	private Process process;
	private EventRequestManager eventRequestManager;
	private EventQueue eventQueue;
	private EventSet eventSet;
	private boolean vmExit = false;
	// write your own testclass
	private List<String> classUnloadExclusions;
	private List<String> classUnloadFilters;
	private List<String> classPrepareExclusions;
	private List<String> classPrepareFilters;
	private List<String> methodExitExclusions;
	private List<String> methodExitFilters;
	private List<String> methodEntryExclusions;
	private List<String> methodEntryFilters;

	private boolean flag = false;

	private String stopClassName;
	private String stopMethodName;
	private List<String> stopMethodArgs;

	private String beginClassName;
	private String beginMethodName;
	private List<String> beginMethodArgs;

	/*
	 * private String className = "test.copy.HelloWorld"; private static final
	 * String classPath = "-cp "
	 * +"D:\\oxygenEclipse\\BTrace\\Debugger\\target\\classes;"
	 * +"D:\\jdk\\x64\\jre\\lib\\tools.jar;"+"" ;
	 */

	// private String className = "Hello";
	/*
	 * private static final String classPath = "-cp " +
	 * "C:\\Users\\cobbl\\git\\MyTrace\\MyTrace\\target\\classes;" +
	 * "C:\\Users\\cobbl\\git\\MyTrace\\MyTrace\\lib\\javassist.jar;" +
	 * "C:\\Users\\cobbl\\git\\MyTrace\\MyTrace\\lib\\org.eclipse.draw2d_3.6.0.v20100525-1225.jar;"
	 * +
	 * "C:\\Users\\cobbl\\git\\MyTrace\\MyTrace\\lib\\org.eclipse.swt.win32.win32.x86_64_3.105.3.v20170228-0512.jar;"
	 * +"D:\\jdk\\x64\\jre\\lib\\tools.jar;"+"" ;
	 */
	// private String className = "com.qualityeclipse.genealogy.view.GenealogyView";
	private String className = "test.Hello";

	private static final String classPath = "-cp " + "D:\\oxygenEclipse\\BTrace\\Genealogy\\bin;"
			+ "D:\\oxygenEclipse\\BTrace\\JdbExample\\bin;"
			+ "D:\\oxygenEclipse\\BTrace\\Genealogy\\lib\\org.eclipse.swt.win32.win32.x86_64_3.105.3.v20170228-0512.jar;"
			+ "D:\\jdk\\x64\\jre\\lib\\tools.jar;"
			+ "D:\\oxygenEclipse\\BTrace\\Genealogy\\lib\\org.eclipse.draw2d_3.6.0.v20100525-1225.jar;";

	public static void main(String[] args) throws Exception {

		MethodTrace trace = new MethodTrace();
		// 启动目标
		trace.launchDebugee();
		// 设置过滤器
		trace.getFilter();
		// 注册请求
		trace.registerEvent();
		// 保存进程
		trace.processDebuggeeVM();
		trace.setBeginRecord();
		trace.setStopRecord();
		// 开始跟踪
		trace.eventLoop();
		// 摧毁虚拟机
		trace.destroyDebuggeeVM();

	}

	private void setStopRecord() {
		/*stopClassName = beginClassName;
		stopMethodName = beginMethodName;
		stopMethodArgs = new ArrayList<String>(beginMethodArgs);*/

	}

	private void setBeginRecord() {
		beginClassName = "test.Hello";
		beginMethodName = "main";
		beginMethodArgs = new ArrayList<String>();
		beginMethodArgs.add("java.lang.String[]");
	}

	/*
	 * 添加过滤条件
	 */
	private void getFilter() {

		ArrayList<String> list = new ArrayList<String>();
		// 添加类的过滤
		// list.add("");
		classUnloadExclusions = new ArrayList<String>(list);
		classPrepareExclusions = classUnloadExclusions;
		list.clear();

		// list.add("");
		classUnloadFilters = new ArrayList<String>(list);
		classPrepareFilters = classUnloadFilters;
		list.clear();

		// 添加排除
		// list.add("com.sun.*");
		list.add("sun.*");
		list.add("java.*");
//		list.add("test.*");
		methodExitExclusions = new ArrayList<String>(list);
		methodEntryExclusions = methodExitExclusions;
		list.clear();

		//监听的
		list.add("test.Hello");
		methodExitFilters = new ArrayList<String>(list);
		methodEntryFilters = methodExitFilters;
		list.clear();
	}

	public void launchDebugee() {
		LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();

		// Get arguments of the launching connector
		Map<String, Connector.Argument> defaultArguments = launchingConnector.defaultArguments();
		Connector.Argument mainArg = defaultArguments.get("main");
		Connector.Argument suspendArg = defaultArguments.get("suspend");
		Connector.Argument optionsArg = defaultArguments.get("options");
		// Set class of main method
		mainArg.setValue(className);
		suspendArg.setValue("true");
		optionsArg.setValue(classPath);

		try {
			vm = launchingConnector.launch(defaultArguments);
		} catch (Exception e) {
			// ignore
		}
	}

	public void processDebuggeeVM() {
		process = vm.process();
	}

	public void destroyDebuggeeVM() {
		process.destroy();
	}

	public void registerEvent() {
		// 注册请求
		eventRequestManager = vm.eventRequestManager();
		// 打开虚拟机调试模式
		vm.setDebugTraceMode(VirtualMachine.TRACE_EVENTS);
		// 虚拟机退出请求
		VMDeathRequest createVMDeathRequest = eventRequestManager.createVMDeathRequest();
		createVMDeathRequest.enable();
		// 注册方法进入请求
		MethodEntryRequest entryReq = eventRequestManager.createMethodEntryRequest();
		// 方法入口事件 添加需要监视的标识
		for (String methodEntryFilter : methodEntryFilters) {
			entryReq.addClassFilter(methodEntryFilter);
		}
		// 添加需要排除的标识
		for (String methodExclude : methodEntryExclusions) {
			entryReq.addClassExclusionFilter(methodExclude);
		}
		entryReq.setSuspendPolicy(MethodEntryRequest.SUSPEND_NONE);
		entryReq.enable();
		// 注册方法退出请求
		MethodExitRequest exitReq = eventRequestManager.createMethodExitRequest();
		// 方法出口事件添加需要监视的标识
		for (String methodExitFilter : methodExitFilters) {
			exitReq.addClassFilter(methodExitFilter);
		}
		// 添加需要排除的标识
		for (String methodExitExclusion : methodExitExclusions) {
			exitReq.addClassExclusionFilter(methodExitExclusion);
		}
		exitReq.setSuspendPolicy(MethodExitRequest.SUSPEND_NONE);
		exitReq.enable();
		// 注册线程开启请求
		ThreadStartRequest threadStartRequest = eventRequestManager.createThreadStartRequest();
		threadStartRequest.setSuspendPolicy(ThreadStartRequest.SUSPEND_NONE);
		threadStartRequest.enable();
		// 注册线程死亡请求
		ThreadDeathRequest threadDeathRequest = eventRequestManager.createThreadDeathRequest();
		threadDeathRequest.setSuspendPolicy(ThreadDeathRequest.SUSPEND_NONE);
		threadDeathRequest.enable();
		// 注册类进入请求
		ClassPrepareRequest classPrepareRequest = eventRequestManager.createClassPrepareRequest();
		// 添加类初始化监视标识
		for (String classPrepareFilter : classPrepareFilters) {
			classPrepareRequest.addClassFilter(classPrepareFilter);
		}
		// 添加排除标识
		for (String classPrepareExclusion : classPrepareExclusions) {
			classPrepareRequest.addClassExclusionFilter(classPrepareExclusion);
		}
		classPrepareRequest.setSuspendPolicy(ClassPrepareRequest.SUSPEND_NONE);
		classPrepareRequest.enable();
		// 注册类退出请求
		ClassUnloadRequest classUnloadRequest = eventRequestManager.createClassUnloadRequest();
		// 添加类退出排除标识
		for (String classUnloadFilter : classUnloadFilters) {
			classUnloadRequest.addClassFilter(classUnloadFilter);
		}
		// 添加排除标识
		for (String classUnloadExclusion : classUnloadExclusions) {
			classUnloadRequest.addClassExclusionFilter(classUnloadExclusion);
		}
		classUnloadRequest.setSuspendPolicy(ClassUnloadRequest.SUSPEND_NONE);
		classUnloadRequest.enable();
		// 监视器连接请求
		/*
		 * MonitorContendedEnterRequest monitorContendedEnterRequest =
		 * eventRequestManager.createMonitorContendedEnterRequest();
		 * monitorContendedEnterRequest.setSuspendPolicy(MonitorContendedEnterRequest.
		 * SUSPEND_NONE); monitorContendedEnterRequest.enable(); // 监视器已连请求
		 * MonitorContendedEnteredRequest monitorContendedEnteredRequest =
		 * eventRequestManager.createMonitorContendedEnteredRequest();
		 * monitorContendedEnteredRequest.setSuspendPolicy(
		 * MonitorContendedEnteredRequest.SUSPEND_NONE);
		 * monitorContendedEnteredRequest.enable();
		 */

		// 异常请求
		// eventRequestManager.createExceptionRequest(arg0, arg1, arg2);
		// 断点请求
		// eventRequestManager.createBreakpointRequest(arg0);

		// 访问断点数据请求
		// eventRequestManager.createAccessWatchpointRequest(arg0);
	}

	private void eventLoop() throws Exception {
		eventQueue = vm.eventQueue();

		while (true) {
			if (vmExit == true) {
				break;
			}
			eventSet = eventQueue.remove();
			EventIterator eventIterator = eventSet.eventIterator();
			while (eventIterator.hasNext()) {
				Event event = (Event) eventIterator.next();
				execute(event);
				if (!vmExit) {
					eventSet.resume();
				}
			}
		}
	}

	private void execute(Event event) throws Exception {
		if (event instanceof VMStartEvent) {
			// 虚拟机启动事件
			// System.out.println("VM started");
			logger.info("VM started");
		} else if (event instanceof MethodEntryEvent) {
			// 方法进入事件
			Method method = ((MethodEntryEvent) event).method();

			String thread = ((MethodEntryEvent) event).thread().name();
			ReferenceType type = method.declaringType();
			String name = method.name();
			List<String> argumentTypeNames = method.argumentTypeNames();
			int modifiers = method.modifiers();
			String returnType = method.returnTypeName();
			if (type.name().equals(beginClassName) && name.equals(beginMethodName)
					&& argumentTypeNames.equals(beginMethodArgs)) {
				flag = true;
			}
			// System.out.printf("Enter -> Method: %s, Signature:%s, ThreadName:
			// %s\n",method.name(),method.signature(),thread);
			// System.out.printf("\t ReturnType:%s\n", method.returnTypeName());
			if (flag) {
				logger.info("Enter -> " + type.name() + " --->:" + name + "<-----(" + argumentTypeNames + " )----"
						+ Modifier.modifier(modifiers) + "  " + returnType + "@" + thread + "\n");
			}
		} else if (event instanceof MethodExitEvent) {
			// 方法退出事件
			Method method = ((MethodExitEvent) event).method();
			ReferenceType type = method.declaringType();
			String thread = ((MethodExitEvent) event).thread().name();
			String returnType = method.returnTypeName();
			int modifiers = method.modifiers();
			String name = method.name();
			List<String> argumentTypeNames = method.argumentTypeNames();

			if (type.name().equals(stopClassName) && name.equals(stopMethodName)
					&& argumentTypeNames.equals(stopMethodArgs)) {
				flag = false;
			}
			// System.out.printf("Exit -> method: %s\n",method.name()+"...threadName:
			// "+threadName);
			if (flag) {
				logger.info("Exit -> " + type.name() + " --->:" + name + "<-----(" + argumentTypeNames + " )---"
						+ Modifier.modifier(modifiers) + " " + returnType + "<----@" + thread + "\n");
			}
		} else if (event instanceof VMDisconnectEvent) {
			vmExit = true;
			// System.out.println("虚拟机断开");
			logger.info("虚拟机断开" + "\n");
		} else if (event instanceof ThreadStartEvent) {
			ThreadReference thread = ((ThreadStartEvent) event).thread();
			// System.out.println("线程开启"+thread.uniqueID());
			logger.info("线程" + thread.name() + "----" + thread.uniqueID() + "开启" + "\n");
		} else if (event instanceof ThreadDeathEvent) {
			ThreadReference thread = ((ThreadDeathEvent) event).thread();
			// System.out.println("线程死亡"+thread.uniqueID());
			logger.info("线程" + thread.name() + "----" + thread.uniqueID() + "走完" + "\n");
		} else if (event instanceof VMDeathEvent) {
			System.out.println("虚拟机死亡");
			logger.info("虚拟机退出" + "\n");
		} else if (event instanceof ExceptionEvent) {
			// 异常断点
			Location location = ((ExceptionEvent) event).catchLocation();
			ObjectReference exception = ((ExceptionEvent) event).exception();
			System.out.println("捕获到异常 ：" + exception.getClass() + "位置在" + location);
			// eventRequestManager.createBreakpointRequest(location);
		} else if (event instanceof ClassPrepareEvent) {
			ReferenceType type = ((ClassPrepareEvent) event).referenceType();
			String className = type.name();
			ThreadReference thread = ((ClassPrepareEvent) event).thread();
			String threadName = thread.name();
			// System.out.println("--ClassPrepare: "+className+" --thread: "+threadName);
			logger.info("--ClassPrepare: " + className + " @" + threadName + "\n");
		} else if (event instanceof ClassUnloadEvent) {
			String name = ((ClassUnloadEvent) event).className();
			// System.out.println("--ClassUnload: "+name);
			logger.info("--ClassUnload: " + name + "\n");
		}
	}
}
