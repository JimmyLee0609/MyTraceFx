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
		// ����Ŀ��
		trace.launchDebugee();
		// ���ù�����
		trace.getFilter();
		// ע������
		trace.registerEvent();
		// �������
		trace.processDebuggeeVM();
		trace.setBeginRecord();
		trace.setStopRecord();
		// ��ʼ����
		trace.eventLoop();
		// �ݻ������
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
	 * ��ӹ�������
	 */
	private void getFilter() {

		ArrayList<String> list = new ArrayList<String>();
		// �����Ĺ���
		// list.add("");
		classUnloadExclusions = new ArrayList<String>(list);
		classPrepareExclusions = classUnloadExclusions;
		list.clear();

		// list.add("");
		classUnloadFilters = new ArrayList<String>(list);
		classPrepareFilters = classUnloadFilters;
		list.clear();

		// ����ų�
		// list.add("com.sun.*");
		list.add("sun.*");
		list.add("java.*");
//		list.add("test.*");
		methodExitExclusions = new ArrayList<String>(list);
		methodEntryExclusions = methodExitExclusions;
		list.clear();

		//������
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
		// ע������
		eventRequestManager = vm.eventRequestManager();
		// �����������ģʽ
		vm.setDebugTraceMode(VirtualMachine.TRACE_EVENTS);
		// ������˳�����
		VMDeathRequest createVMDeathRequest = eventRequestManager.createVMDeathRequest();
		createVMDeathRequest.enable();
		// ע�᷽����������
		MethodEntryRequest entryReq = eventRequestManager.createMethodEntryRequest();
		// ��������¼� �����Ҫ���ӵı�ʶ
		for (String methodEntryFilter : methodEntryFilters) {
			entryReq.addClassFilter(methodEntryFilter);
		}
		// �����Ҫ�ų��ı�ʶ
		for (String methodExclude : methodEntryExclusions) {
			entryReq.addClassExclusionFilter(methodExclude);
		}
		entryReq.setSuspendPolicy(MethodEntryRequest.SUSPEND_NONE);
		entryReq.enable();
		// ע�᷽���˳�����
		MethodExitRequest exitReq = eventRequestManager.createMethodExitRequest();
		// ���������¼������Ҫ���ӵı�ʶ
		for (String methodExitFilter : methodExitFilters) {
			exitReq.addClassFilter(methodExitFilter);
		}
		// �����Ҫ�ų��ı�ʶ
		for (String methodExitExclusion : methodExitExclusions) {
			exitReq.addClassExclusionFilter(methodExitExclusion);
		}
		exitReq.setSuspendPolicy(MethodExitRequest.SUSPEND_NONE);
		exitReq.enable();
		// ע���߳̿�������
		ThreadStartRequest threadStartRequest = eventRequestManager.createThreadStartRequest();
		threadStartRequest.setSuspendPolicy(ThreadStartRequest.SUSPEND_NONE);
		threadStartRequest.enable();
		// ע���߳���������
		ThreadDeathRequest threadDeathRequest = eventRequestManager.createThreadDeathRequest();
		threadDeathRequest.setSuspendPolicy(ThreadDeathRequest.SUSPEND_NONE);
		threadDeathRequest.enable();
		// ע�����������
		ClassPrepareRequest classPrepareRequest = eventRequestManager.createClassPrepareRequest();
		// ������ʼ�����ӱ�ʶ
		for (String classPrepareFilter : classPrepareFilters) {
			classPrepareRequest.addClassFilter(classPrepareFilter);
		}
		// ����ų���ʶ
		for (String classPrepareExclusion : classPrepareExclusions) {
			classPrepareRequest.addClassExclusionFilter(classPrepareExclusion);
		}
		classPrepareRequest.setSuspendPolicy(ClassPrepareRequest.SUSPEND_NONE);
		classPrepareRequest.enable();
		// ע�����˳�����
		ClassUnloadRequest classUnloadRequest = eventRequestManager.createClassUnloadRequest();
		// ������˳��ų���ʶ
		for (String classUnloadFilter : classUnloadFilters) {
			classUnloadRequest.addClassFilter(classUnloadFilter);
		}
		// ����ų���ʶ
		for (String classUnloadExclusion : classUnloadExclusions) {
			classUnloadRequest.addClassExclusionFilter(classUnloadExclusion);
		}
		classUnloadRequest.setSuspendPolicy(ClassUnloadRequest.SUSPEND_NONE);
		classUnloadRequest.enable();
		// ��������������
		/*
		 * MonitorContendedEnterRequest monitorContendedEnterRequest =
		 * eventRequestManager.createMonitorContendedEnterRequest();
		 * monitorContendedEnterRequest.setSuspendPolicy(MonitorContendedEnterRequest.
		 * SUSPEND_NONE); monitorContendedEnterRequest.enable(); // ��������������
		 * MonitorContendedEnteredRequest monitorContendedEnteredRequest =
		 * eventRequestManager.createMonitorContendedEnteredRequest();
		 * monitorContendedEnteredRequest.setSuspendPolicy(
		 * MonitorContendedEnteredRequest.SUSPEND_NONE);
		 * monitorContendedEnteredRequest.enable();
		 */

		// �쳣����
		// eventRequestManager.createExceptionRequest(arg0, arg1, arg2);
		// �ϵ�����
		// eventRequestManager.createBreakpointRequest(arg0);

		// ���ʶϵ���������
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
			// ����������¼�
			// System.out.println("VM started");
			logger.info("VM started");
		} else if (event instanceof MethodEntryEvent) {
			// ���������¼�
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
			// �����˳��¼�
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
			// System.out.println("������Ͽ�");
			logger.info("������Ͽ�" + "\n");
		} else if (event instanceof ThreadStartEvent) {
			ThreadReference thread = ((ThreadStartEvent) event).thread();
			// System.out.println("�߳̿���"+thread.uniqueID());
			logger.info("�߳�" + thread.name() + "----" + thread.uniqueID() + "����" + "\n");
		} else if (event instanceof ThreadDeathEvent) {
			ThreadReference thread = ((ThreadDeathEvent) event).thread();
			// System.out.println("�߳�����"+thread.uniqueID());
			logger.info("�߳�" + thread.name() + "----" + thread.uniqueID() + "����" + "\n");
		} else if (event instanceof VMDeathEvent) {
			System.out.println("���������");
			logger.info("������˳�" + "\n");
		} else if (event instanceof ExceptionEvent) {
			// �쳣�ϵ�
			Location location = ((ExceptionEvent) event).catchLocation();
			ObjectReference exception = ((ExceptionEvent) event).exception();
			System.out.println("�����쳣 ��" + exception.getClass() + "λ����" + location);
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
