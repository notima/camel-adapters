package org.notima.camel.sveawebpay.junit;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Class that handles test properties
 * 
 * @author Daniel Tamm
 *
 */
public class TestConfig {

	public static Logger testLogger = Logger.getLogger("org.notima.camel.sveawebpay.junit");
	
	public static String	orgNo;
	public static String	orgName;
	public static String	clientUsername;
	public static String	clientPassword;
	public static String	merchantId;
	public static String	secretWord;
	
	public static String	DEFAULT_CONFIG_FILE = "default-config.properties";
	public static String	USER_CONFIG_FILE = "user-config.properties";
	
	static {
		
		// Check if the file is there
		URL url = ClassLoader.getSystemResource(USER_CONFIG_FILE);
		if (url==null) {
			url = ClassLoader.getSystemResource(DEFAULT_CONFIG_FILE);
		}
		
		if (url!=null) {
			Properties props = new Properties();
			try {
				props.load(new FileReader(url.getFile()));
				
				orgNo = props.getProperty("orgNo");
				orgName = props.getProperty("orgName");
				clientUsername = props.getProperty("clientUsername");
				clientPassword = props.getProperty("clientPassword");
				merchantId = props.getProperty("merchantId");
				secretWord = props.getProperty("secretWord");
				
			} catch (FileNotFoundException e) {
				testLogger.severe("No default-config.properties file found in classpath / path");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			testLogger.severe("No default-config.properties file found in classpath / path");
		}
		
	}
	
}
