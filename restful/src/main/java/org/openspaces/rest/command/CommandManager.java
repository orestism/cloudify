package org.openspaces.rest.command;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.HandlerMapping;
/**
 * CommandManager creates and runs commands, each command
 * depending on the previous command's output. 
 * 
 * The Command manager holds a list of commands where each
 * command holds a reference to the previous command.
 * 
 * The manager will output the last command's output object, as-well as
 * necessary information concerning the command's execution parameters.
 * 
 * @author adaml
 *
 */
public class CommandManager {
	
	private List<CommandObject> listOfCommands;
	private String commandURL;
	
	/**
	 * Constructor takes as input the entire commands URI, held in the request
	 * and the root object from which to begin invocation.
	 * @param request - the commands request 
	 * @param root - the root command's object
	 */
	public CommandManager(HttpServletRequest request, Object root){
		String executionPath = getExecutionPath(request);
		this.commandURL = getRestUrl(request) + "/" + executionPath;
		initilizeCommandList(executionPath, root);
	}
	
	/**
	 * run the initialized commands one by one, with each command 
	 * depending on the previous command's output.
	 */
	public void runCommands(){
		for (CommandObject command : listOfCommands){
			command.runCommand();
		}
	}
	
	/**
	 * get the last command.
	 * @return the final object for parsing.
	 */
	public CommandObject getFinalCommand(){
		return this.listOfCommands.get(listOfCommands.size() - 1);
	}
	
	/**
	 * get the full execution URL for the command. 
	 * @return command URL.
	 */
	public String getCommandURL(){
		return this.commandURL;
	}
	
	/**
	 * get the last command's name.
	 * @return the name of the last command in the list.
	 */
	public String getFinalCommandName(){
		return this.listOfCommands.get(listOfCommands.size() - 1).getCommandName();
	}
	
	//TODO: change get attribute 
	private String getExecutionPath(HttpServletRequest request) {
		//String executionPath = request.getRequestURI().substring(15);
		String executionPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		if (executionPath.endsWith("/")) {
			executionPath = executionPath.substring(0, executionPath.length() - 1);
		}
		return executionPath;
	}
	
	//Initialize the list of commands. each command will hold a reference to its previous command.
	private void initilizeCommandList(String commands, Object root) {
		this.listOfCommands = new ArrayList<CommandObject>();
		String[] delimitedCommands = commands.split("/");
		
		CommandObject commandObject;
		CommandObject previousCommand = new CommandObject(root);
		
		for(String rawCommandString : delimitedCommands){
			commandObject = new CommandObject(rawCommandString);
			commandObject.setPreviousCommandObject(previousCommand);
			listOfCommands.add(commandObject);
			previousCommand = commandObject;
		}
	}
	
	private String getRestUrl(HttpServletRequest httpServletRequest){
		String localAddress = httpServletRequest.getLocalAddr();
		String contextPath = httpServletRequest.getContextPath();
		int localPort = httpServletRequest.getLocalPort();
		return "http://" + localAddress + ":" + localPort + contextPath + "/admin";
	}

}
