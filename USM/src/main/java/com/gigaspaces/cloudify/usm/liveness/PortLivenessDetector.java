package com.gigaspaces.cloudify.usm.liveness;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.gigaspaces.cloudify.dsl.Plugin;
import com.gigaspaces.cloudify.dsl.context.ServiceContext;
import com.gigaspaces.cloudify.dsl.utils.ServiceUtils;
import com.gigaspaces.cloudify.usm.UniversalServiceManagerBean;
import com.gigaspaces.cloudify.usm.events.AbstractUSMEventListener;
import com.gigaspaces.cloudify.usm.events.EventResult;
import com.gigaspaces.cloudify.usm.events.PreStartListener;
import com.gigaspaces.cloudify.usm.events.StartReason;

/**
 * PortLivenessDetector class is responsible for verifying that the process has finished loading
 * by checking whether the ports opened by the process are indeed open.
 * the application ports to check are defined in the configuration file.
 * 
 * There are 2 ways of using the PortLiveness. 
 * 1. The first is by adding a plugin to the DSL in the following manner:
 * plugins ([
 * 		plugin {
 *			name "portLiveness"
 *			className "com.gigaspaces.cloudify.usm.liveness.PortLivenessDetector"
 *			config ([
 *						"Port" : [39000,38999,38998],
 *						"TimeoutInSeconds" : 30,
 *						"Host" : "127.0.0.1"
 *					])
 *		},
 *
 *		plugin{...
 *
 * 2. By adding the following closures to the Service Groovy file in the following manner:
 * 
 * 		* Add the following command to the preStart Closure(Not a mandatory check):
 * 			ServiceUtils.isPortsFree([23894,34,3243], "127.0.0.1") //to see that the ports are available before process starts.
 * 		
 * 		* Add the following closure to the lifecycle closure:
 * 			startDetection {ServiceUtils.waitForPortToOpen([23894,34,3243],"127.0.0.1", 60)} //to see that the process has started and is using these ports.
 *  
 * @author adaml
 * 
 */
public class PortLivenessDetector extends AbstractUSMEventListener implements LivenessDetector, Plugin,
PreStartListener {

	private static final java.util.logging.Logger logger = java.util.logging.Logger
			.getLogger(PortLivenessDetector.class.getName());
	private static final String HOST_KEY = "Host";
	private static final String PORT_KEY = "Port";
	
	private static final String DEFAULT_HOST = "127.0.0.1";

	
	//Injected values
	private List<Integer> portList;
	private String hostName = DEFAULT_HOST;
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void setConfig(Map<String, Object> config) {
		this.portList = (List<Integer>) config.get(PORT_KEY);
		if(this.portList == null) {
			throw new IllegalArgumentException("Parameter portList of Plugin " + this.getClass().getName() + " is mandatory");
		}
		
		if (config.get(HOST_KEY) != null){
			this.hostName = (String) config.get(HOST_KEY);
		}
	}
	
	/**
	 * Checks if a set of ports is open open (i.e. you can connect to them).
	 * 
	 * @return true if all ports in the list are open, false if any one of them is not.
	 * 
	 */
	@Override
	public boolean isProcessAlive() throws TimeoutException {
		logger.info("Testing if the following ports are open: " + this.portList.toString());
		return !ServiceUtils.isPortsFree(this.portList);
	}

	@Override
	public void init(UniversalServiceManagerBean usm) {

	}

	@Override
	public int getOrder() {
		return 5;
	}

	/**
	 * Verifies that the ports are not in use before the service starts. 
	 */
	@Override
	public EventResult onPreStart(StartReason reason) {
		for (Integer port : this.portList) {
		
			if(!ServiceUtils.isPortFree(port, hostName)) {
				throw new IllegalStateException("The Port Liveness Detector found that port "
						+ port + " is IN USE before the process was launched!");

			}
		}
		
		return EventResult.SUCCESS;
	}
	
	@Override
	public void setServiceContext(ServiceContext context) {
		// ignore
	}

}
