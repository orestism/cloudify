/*******************************************************************************
 * Copyright 2011 GigaSpaces Technologies Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.openspaces.cloud.azure.shell.commands;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.openspaces.cloud.azure.AzureDeploymentStatus;
import org.openspaces.cloud.azure.shell.AzureUtils;
import org.openspaces.shell.commands.AbstractGSCommand;
import org.openspaces.shell.commands.CLIException;

/**
 * Installs applications consisting of one or more services
 * 
 * @author itaif
 * @author dank
 * @since 8.0.4
 *
 */
@Command(scope = "azure", name = "teardown-app", description = "Installs an application consisting of one or more services. If you specify a folder path it will be packed and deployed. If you sepcify a service archive, the shell will deploy that file.")
public class AzureTeardownApplication extends AbstractGSCommand {

	private static final Logger logger = Logger.getLogger(AzureTeardownApplication.class.getName());
	@Option(required = true, name = "-azure-svc", description = "The Azure Hosted Service name. Default: [application name]")
	String azureHostedServiceName = null;

	@Option(required = false, name = "-azure-slot", description = "The Azure Deployment slot (staging or production). Default: staging")
	String azureDeploymentSlotName = "staging";
	
	@Option(required = false, name = "-timeout", description = "The number of minutes to wait until the operation is done. Defaults to 5 minutes.")
	int timeoutInMinutes=5;
	
	@Option(required = false, name = "-progress", description = "The polling time interval in minutes, used for checking if the operation is done. Defaults to 1 minute. Use together with the -timeout option")
	int progressInMinutes=1;
	
	@Override
	protected Object doExecute() throws Exception {
		
       if (timeoutInMinutes < 0) {
            throw new CLIException("-timeout cannot be negative");
        }
        
        if (progressInMinutes < 1) {
            throw new CLIException("-progress must be positive");
        }
        
        if (timeoutInMinutes > 0 && timeoutInMinutes < progressInMinutes) {
            throw new CLIException("-timeout must be bigger than -progress");
        }
        
        long end = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(timeoutInMinutes);
        if (end < 0) {
            throw new CLIException("-timeout caused an overflow. please use a smaller value");
        }
	    
		Properties properties = AzureUtils.getAzureProperties();
		String subscriptionId = AzureUtils.getProperty(properties, "subscriptionId");
		String certificateThumbprint = AzureUtils.getProperty(properties, "certificateThumbprint");
		
		logger.info("Deleting deployment");
	
		AzureDeploymentWrapper azureDeploymentWrapper = new AzureDeploymentWrapper();
		azureDeploymentWrapper.setVerbose(verbose);
		azureDeploymentWrapper.setProgressInMinutes(progressInMinutes);
		azureDeploymentWrapper.setAzureHostedServiceName(azureHostedServiceName);
		azureDeploymentWrapper.setAzureDeploymentSlotName(azureDeploymentSlotName);
		azureDeploymentWrapper.setCertificateThumbprint(certificateThumbprint);
		azureDeploymentWrapper.setSubscriptionId(subscriptionId);
		
		azureDeploymentWrapper.stopDeployment();
		azureDeploymentWrapper.waitForAzureDeploymentStatus(AzureDeploymentStatus.Suspended, 5000, millisUntil(end), TimeUnit.MILLISECONDS);
		
        azureDeploymentWrapper.deleteDeployment();
        if (timeoutInMinutes == 0) {
        	return "Started application teardown.";	
        }
         
        azureDeploymentWrapper.waitForAzureDeploymentStatus(AzureDeploymentStatus.NotFound, millisUntil(end), TimeUnit.MILLISECONDS);
    	return "Completed application teardown.";
    }
	
    private static long millisUntil(long end) throws TimeoutException {
        long millisUntilEnd = end - System.currentTimeMillis();
        if (millisUntilEnd < 0) {
            throw new TimeoutException("Azure application bootsrap timed-out");
        }
        return millisUntilEnd;
    }
}
