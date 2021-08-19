package org.notima.camel.sveawebpay.junit;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.notima.camel.sveawebpay.WebpayAdminCamelClient;

public class TestCreateClientFromFile {

	private TestAdminClientUtil		clientUtil;
	private WebpayAdminCamelClient 	client;
	
	@Before
	public void setUp() throws Exception {
		clientUtil = new TestAdminClientUtil();
	}

	@Test
	public void testCreateClientFromFile() {
		
		client = new WebpayAdminCamelClient();
		try {
			File fileToRead = clientUtil.getCredentialsFile(); 
			client.setJsonFile(fileToRead.getAbsolutePath());
			client.createClientFromFile();
			TestConfig.testLogger.info("Read credentials for " + client.getOrgNo() + " from " + fileToRead.getName());
			
		} catch (Exception e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
		
	}

}
