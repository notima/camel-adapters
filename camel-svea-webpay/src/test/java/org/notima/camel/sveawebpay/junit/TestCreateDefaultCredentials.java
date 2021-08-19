package org.notima.camel.sveawebpay.junit;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.svea.webpay.common.auth.ListOfSveaCredentials;

public class TestCreateDefaultCredentials {

	private ListOfSveaCredentials credentials;
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testCreateDefaultCredentials() {

		try {
			credentials = TestAdminClientUtil.buildTestCredentialsFromFile();
		} catch (IOException io) {
			// If we're here
			if (io instanceof FileNotFoundException) {

				credentials = ListOfSveaCredentials
						.buildFromMerchantIdAndSecret(TestConfig.merchantId, TestConfig.secretWord);
				credentials.setOrgNameForAllCredentials(TestConfig.orgName);
				credentials.setOrgNoForAllCredentials(TestConfig.orgNo);
				if (TestConfig.clientUsername!=null) {
					credentials.setUsernameAndPasswordForAllCredentials(TestConfig.clientUsername, TestConfig.clientPassword);
				}
				
				try {
					
					credentials.saveToJsonFile(getDefaultCredentialsFile().getAbsolutePath());
					
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
				
			} else {
				fail(io.getMessage());
			}
			
		}		
		
	}

	private File getDefaultCredentialsFile() throws Exception {
		
		String path = "src/test/resources";
		File file = new File(path);
		if (!file.exists() || !file.canWrite()) {
			throw new Exception(file.getAbsolutePath() + " doesn't exist or isn't writable.");
		}
		File defaultCredentialsFile = new File(file.getAbsolutePath() + File.separator + TestAdminClientUtil.DEFAULT_TEST_CREDENTIALS_FILE);
		return defaultCredentialsFile;
		
	}
	
}
