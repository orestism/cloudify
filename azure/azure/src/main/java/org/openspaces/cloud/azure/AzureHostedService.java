package org.openspaces.cloud.azure;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

public class AzureHostedService extends AzureConfigExe {

	public AzureHostedService(File azureConfigExeFile, File encUtilExeFile, String subscriptionId, String certificateThumbprint, boolean verbose) {
        super(azureConfigExeFile, encUtilExeFile, subscriptionId, certificateThumbprint, verbose);
    }

	/**
     * @param azureHostedServiceDescription 
	 * @see http://msdn.microsoft.com/en-us/library/gg441304.aspx
     */
    public void createHostedService(
            String name, 
            String label,
            String location, 
            String description) throws InterruptedException, AzureDeploymentException {
    	
    	Collection<String> locations = new HashSet<String>(Arrays.asList(listLocations()));
    	if (!locations.contains(location)) {
    		throw new AzureDeploymentException("The location " + location + " is invalid. Use one of the following locations: " + locations);
    	}
    	
        executeAzureConfig(
            argument(CREATE_HOSTED_SERVICE_FLAG),
            argument(HOSTED_SERVICE_FLAG, name),
            argument(LABEL_FLAG, label),
            argument(LOCATION_FLAG, location),
            argument(DESCRIPTION_FLAG,description)
        );
    }
    
    /**
     * @see http://msdn.microsoft.com/en-us/library/ee460781.aspx
     */
    public String[] listHostedServices() throws InterruptedException, AzureDeploymentException {
    	
        return executeAzureConfig(
            argument(LIST_HOSTED_SERVICES_FLAG)
        ).split(Pattern.quote("\n"));
    }
    
    /**
     * @see http://msdn.microsoft.com/en-us/library/gg441293.aspx
     */
    public String[] listLocations() throws InterruptedException, AzureDeploymentException {
    	return executeAzureConfig(
                argument(LIST_LOCATIONS_FLAG)
            ).split(Pattern.quote("\n"));
    	
    }
    /**
     * @see http://msdn.microsoft.com/en-us/library/ee460788.aspx
     */
    public String[] listCertificateThumbprints(String hostedServiceName) throws InterruptedException, AzureDeploymentException {
	    return executeAzureConfig(
	    		argument(LIST_CERTIFICATES_FLAG),
	    		argument(HOSTED_SERVICE_FLAG,hostedServiceName)
	    	).split(Pattern.quote("\n"));
    }

	/**
	 * @see http://msdn.microsoft.com/en-us/library/ee460817.aspx
	 */
	public void addCertificate(String hostedServiceName, File certificateFile, String certificateFilePassword) throws InterruptedException, AzureDeploymentException {
		executeAzureConfig(
	    		argument(ADD_CERTIFICATE_FLAG),
	    		argument(HOSTED_SERVICE_FLAG,hostedServiceName),
	    		argument(CERTIFICATE_FILE_FLAG,certificateFile.getAbsolutePath()),
	    		argument(CERTIFICATE_FILE_PASSWORD_FLAG,certificateFilePassword)
	    );
	}
	
	/**
	 * @return the thumbprint of the specified public certificate file
	 * @throws InterruptedException 
	 * @throws AzureDeploymentException 
	 */
	public String getCertificateThumbprint(File certFile) throws AzureDeploymentException, InterruptedException {
		String thumbprint = executeEncUtil("-thumbprint","-cert",certFile.getAbsolutePath());
		if (thumbprint.endsWith("\n")) {
			thumbprint = thumbprint.substring(0, thumbprint.length()-1);
		}
		return thumbprint.trim();
	}

}
