package de.method;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
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

import logger.MyLog;

public class MethodTrace implements Runnable{
	MyLog logger=new MyLog();
	public static void main(String[] args) throws Exception {

		MethodTrace trace = new MethodTrace();
		trace.setLoggerFileLocation(new File("C:\\log\\f9f082dc-a8da-47c7-9427-187d42a33dec.html"));
		// 启动目标
		trace.launchDebugee();
		// 设置过滤器
		trace.getFilter();
		// 注册请求
		trace.registerEvent();
		// 保存进程
		trace.processDebuggeeVM();
//		trace.setBeginRecord();
//		trace.setStopRecord();
		// 开始跟踪
		trace.eventLoop();
		// 摧毁虚拟机
		trace.destroyDebuggeeVM();

	}
	private VirtualMachine vm;
	private Process process;
	private EventRequestManager eventRequestManager;
	private EventQueue eventQueue;
	private EventSet eventSet;

	private boolean vmExit = false;

	private List<String> methodExclusions=new ArrayList<String>();
	private List<String> methodFilters=new ArrayList<String>();

//	开始记录标记
	private boolean flag = false;

	public boolean isFlag() {
		return flag;
	}


	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	private String stopClassName;
	private String stopMethodName;
	private List<String> stopMethodArgs;

	private String beginClassName;
	private String beginMethodName;
	private List<String> beginMethodArgs;

	private String className;
	private String classPath;

	public void destroyDebuggeeVM() {
		process.destroy();
		logger.save();
	}


	public void eventLoop() throws Exception {
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
			logger.log("<th>VM started</th>");
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
				logger.log("<td class='Message'>MethodEnter</td><td class='Message'>" + type.name() + ":</td><td class='Message'>" + name + "</td><td class='Message'>(" + argumentTypeNames + " )</td><td class='Message'>"
						+ Modifier.modifier(modifiers) + "</td><td class='Message'>" + returnType + "</td><td class='Message'>@" + thread + "</td>");
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
			// System.out.printf("Exit -> method:
			// %s\n",method.name()+"...threadName:
			// "+threadName);
			if (flag) {
				logger.log("<td class='Message'>MethodExit -></td><td class='Message'> " + type.name() + ":</td><td class='Message'>" + name + "</td class='Message'><td>(" + argumentTypeNames + " )</td><td class='Message'>"
						+ Modifier.modifier(modifiers) + "</td><td class='Message'>" + returnType + "</td><td class='Message'>@" + thread + "</td>");
			}
		} else if (event instanceof VMDisconnectEvent) {
			vmExit = true;
			// System.out.println("虚拟机断开");
			logger.log("<th>虚拟机断开</th>");
		} else if (event instanceof ThreadStartEvent) {
			ThreadReference thread = ((ThreadStartEvent) event).thread();
			// System.out.println("线程开启"+thread.uniqueID());
			logger.log("<td class='Message'>线程</td><td class='Message'>@" + thread.name() + ": " + thread.uniqueID() + "</td><td class='Message'>开启" + "</td>");
		} else if (event instanceof ThreadDeathEvent) {
			ThreadReference thread = ((ThreadDeathEvent) event).thread();
			// System.out.println("线程死亡"+thread.uniqueID());
			logger.log("<td class='Message'>线程</td><td class='Message'>@" + thread.name() + ": " + thread.uniqueID() + "</td><td class='Message'>走完" + "</td>");
		} else if (event instanceof VMDeathEvent) {
			logger.log("<th>虚拟机退出" + "</th>");
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
			// System.out.println("--ClassPrepare: "+className+" --thread:
			// "+threadName);
			logger.log("<td class='Message'>--ClassPrepare: </td><td class='Message'>" + className + "</td> <td class='Message'>@" + threadName + "</td>");
		} else if (event instanceof ClassUnloadEvent) {
			String name = ((ClassUnloadEvent) event).className();
			// System.out.println("--ClassUnload: "+name);
			logger.log("<td class='Message'>--ClassUnload: </td><td class='Message'>" + name + "</td>");
		}
	}

	public String getClassName() {
		return className;
	}

	public  String getClassPath() {
		return classPath;
	}

	/*
	 * 添加过滤条件
	 */
	private void getFilter() {

		ArrayList<String> list = new ArrayList<String>();

		// 添加排除
		// list.add("com.sun.*");
		list.add("sun.*");
		list.add("java.*");
		// list.add("test.*");
		methodExclusions = new ArrayList<String>(list);
		list.clear();

		// 监听的
		list.add("test.Hello");
		methodFilters = new ArrayList<String>(list);
		list.clear();
	}

	public List<String> getMethodExclusions() {
		return methodExclusions;
	}

	public List<String> getMethodFilters() {
		return methodFilters;
	}

	public VirtualMachine launchDebugee() throws IOException, IllegalConnectorArgumentsException, VMStartException {
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
		return vm = launchingConnector.launch(defaultArguments);
	}

	public void processDebuggeeVM() {
		process = vm.process();
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
		for (String methodEntryFilter : methodFilters) {
			entryReq.addClassFilter(methodEntryFilter);
		}
		// 添加需要排除的标识
		for (String methodExclude : methodExclusions) {
			entryReq.addClassExclusionFilter(methodExclude);
		}
		entryReq.setSuspendPolicy(MethodEntryRequest.SUSPEND_NONE);
		entryReq.enable();
		// 注册方法退出请求
		MethodExitRequest exitReq = eventRequestManager.createMethodExitRequest();
		// 方法出口事件添加需要监视的标识
		for (String methodExitFilter : methodFilters) {
			exitReq.addClassFilter(methodExitFilter);
		}
		// 添加需要排除的标识
		for (String methodExitExclusion : methodExclusions) {
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
		classPrepareRequest.setSuspendPolicy(ClassPrepareRequest.SUSPEND_NONE);
		classPrepareRequest.enable();
		// 注册类退出请求
		ClassUnloadRequest classUnloadRequest = eventRequestManager.createClassUnloadRequest();
		classUnloadRequest.setSuspendPolicy(ClassUnloadRequest.SUSPEND_NONE);
		classUnloadRequest.enable();
		// 监视器连接请求
		/*
		 * MonitorContendedEnterRequest monitorContendedEnterRequest =
		 * eventRequestManager.createMonitorContendedEnterRequest();
		 * monitorContendedEnterRequest.setSuspendPolicy(
		 * MonitorContendedEnterRequest. SUSPEND_NONE);
		 * monitorContendedEnterRequest.enable(); // 监视器已连请求
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

	public void setBeginRecord(String beginClassName, String beginMethodName, ArrayList<String> beginMethodArgs) {
		this.beginClassName = beginClassName;
		this.beginMethodName = beginMethodName;
		this.beginMethodArgs = new ArrayList<String>(beginMethodArgs);
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}

	public void setMethodExclusions(List<String> methodExitExclusions) {
		this.methodExclusions = methodExitExclusions;
	}

	public void setMethodFilters(List<String> methodExitFilters) {
		this.methodFilters = methodExitFilters;
	}

	public void setStopRecord(String stopClassName, String stopMethodName, ArrayList<String> stopMethodArgs) {
		this.stopClassName=stopClassName;
		this.stopMethodName=stopMethodName;
		this.stopMethodArgs=new ArrayList<String>(stopMethodArgs);
	}
	public void setLoggerFileLocation(File file){
		logger.setFile(file);
	}


	@Override
	public void run() {
		try {
			eventLoop();
			destroyDebuggeeVM();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
