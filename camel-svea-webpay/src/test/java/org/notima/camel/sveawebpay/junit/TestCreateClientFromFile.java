package org.notima.camel.sveawebpay.junit;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.notima.camel.sveawebpay.WebpayAdminCamelClient;

import com.svea.webpay.common.auth.SveaCredential;

public class TestCreateClientFromFile {

	private TestAdminClientUtil		clientUtil;
	private WebpayAdminCamelClient 	client;
	private List<SveaCredential> 	credentials;
	private int						numberOfCredentials;
	private File					fileToRead;
	
	@Before
	public void setUp() throws Exception {
		clientUtil = new TestAdminClientUtil();
	}

	/**
	 * Test to create a client from file.
	 * 
	 * Make sure credentials are fetched and that at least basic values are included.
	 * 
	 */
	@Test
	public void testCreateClientFromFile() {
		
		try {
			fileToRead = clientUtil.getCredentialsFile(); 
			client = WebpayAdminCamelClient.buildClientFromFile(fileToRead.getAbsolutePath());
			
			credentials = client.getCredentials();
			testNumberOfCredentials();
			testIfCredentialsContainValues();
			
		} catch (Exception e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
		
	}

	
	private void testNumberOfCredentials() {
		
		numberOfCredentials = credentials.size();
		
		TestConfig.testLogger.info("Read " + numberOfCredentials + " from " + fileToRead.getName());
		Assert.assertTrue("No credentials found in " + fileToRead.getName(), numberOfCredentials > 0);
		
	}
	
	private void testIfCredentialsContainValues() {
		
		for (SveaCredential credential : credentials) {
			Assert.assertTrue("Mandatory values missing in credential", testBasicValuesOfCredential(credential));
		}
		TestConfig.testLogger.info("Mandatory values check passed.");
		
	}
	
	
	private boolean testBasicValuesOfCredential(SveaCredential credential) {
		return credential.hasUsernameAndPassword() || credential.hasMerchantIdAndSecretWord();
	}
	
}
