package org.notima.camel.fortnox;

import javax.xml.bind.JAXBException;

import org.apache.camel.Header;
import org.notima.api.fortnox.FortnoxUtil;
import org.notima.api.fortnox.clients.FortnoxApiClient;
import org.notima.api.fortnox.clients.FortnoxClientInfo;
import org.notima.api.fortnox.clients.FortnoxClientList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to manage several different clients.
 * 
 * @author Daniel Tamm
 *
 */
public class FortnoxClientManager {

	private Logger log = LoggerFactory.getLogger(FortnoxClientManager.class);	
	
	private FortnoxClientList clientList;

	public FortnoxClientManager() {}
	
	/**
	 * Instantiates this client manager by reading clients from given file.
	 * 
	 * @param fortnoxClientsFile	An XML-file containing client data
	 */
	public FortnoxClientManager(String fortnoxClientsFile) {

		readClientsFromFile(fortnoxClientsFile);
		
	}
	
	public Boolean readClientsFromFile(String fortnoxClientsFile) {
		
		try {
			clientList = FortnoxUtil.readFortnoxClientListFromFile(fortnoxClientsFile);
			return clientList!=null ? Boolean.TRUE : Boolean.FALSE; 
		} catch (JAXBException e) {
			log.warn("Can't read Fortnox Client file: {} ", fortnoxClientsFile);
			log.debug(e.getMessage());
			return Boolean.FALSE;
		}
		
	}

	/**
	 * Gets client info by using the org no as key.
	 * 
	 * If client secret is supplied or can be derived from the list of Api Clients, the secret is also
	 * returned.
	 * 
	 * @param orgNo
	 * @return		A FortnoxClientInfo record.
	 */
	public FortnoxClientInfo getClientInfoByOrgNo(@Header(value="orgNo")String orgNo) {
		if (clientList==null) {
			log.warn("No Fortnox Clients available.");
			return null;
		}
		
		FortnoxClientInfo result = clientList.getClientInfoByOrgNo(orgNo);
		
		if (result!=null) {
			// Check and see if client secret is available
			if (result.getClientSecret()==null || result.getClientSecret().trim().length()==0) {
				// Lookup API client
				FortnoxApiClient apic = clientList.getApiClientById(result.getClientId());
				if (apic!=null) {
					result.setClientSecret(apic.getClientSecret());
				}
			}
		}
		
		return result;
	}
	
}