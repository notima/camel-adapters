package org.notima.camel.fortnox;

import org.apache.camel.Header;
import org.notima.api.fortnox.clients.FortnoxClientInfo;

/**
 * 
 * @author Daniel Tamm
 *
 * @deprecated Use org.notima.api.fortnox.clients.FortnoxClientManager
 */
public class FortnoxClientManager extends org.notima.api.fortnox.clients.FortnoxClientManager {

	@Override
	public FortnoxClientInfo getClientInfoByOrgNo(@Header(value="orgNo")String orgNo) {
		return super.getClientInfoByOrgNo(orgNo);
	}

	
	
}
