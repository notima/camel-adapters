package org.notima.camel.sveawebpay.junit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.notima.camel.sveawebpay.WebpayAdminCamelClient;

import com.svea.webpay.common.auth.ListOfSveaCredentials;

public class TestAdminClientUtil {

	public static final String DEFAULT_TEST_CREDENTIALS_FILE = "default_credentials.json";
	public static final String USER_TEST_CREDENTIALS_FILE = "user_credentials.json";

	private String	credentialsFileName;
	
	public static ListOfSveaCredentials buildTestCredentialsFromFile() throws IOException {
		
		TestAdminClientUtil util = new TestAdminClientUtil();
		return util.readTestCredentials();
		
	}
	
	private ListOfSveaCredentials readTestCredentials() throws IOException {
		
		File credentialsFile = getCredentialsFile();
		TestConfig.testLogger.info("Using existing credentials file: " + credentialsFile.getAbsolutePath());
		return ListOfSveaCredentials.readFromJsonFile(credentialsFile);
		
	}


	public WebpayAdminCamelClient getTestWebpayAdminCamelClient() throws Exception {
		
		return WebpayAdminCamelClient.buildClientFromFile(getCredentialsFile().getAbsolutePath());
		
	}
	
	public File getCredentialsFile() throws IOException {
		
		// Prefer user defined credentials
		credentialsFileName = USER_TEST_CREDENTIALS_FILE;
		URL url = getClass().getClassLoader().getResource(credentialsFileName);
		if (url==null) {
			// If no user credentials, use default
			credentialsFileName = DEFAULT_TEST_CREDENTIALS_FILE;
			url = getClass().getClassLoader().getResource(credentialsFileName);
		}

		if (url==null) throwFileNotFoundException("No credentials file found");

		File result = new File(url.getFile());
		return result;
		
	}
	
	private void throwFileNotFoundException(String description) throws FileNotFoundException {
		String msg = description + " : " + credentialsFileName;
		throw new FileNotFoundException(msg);
	}
	
}
