package org.notima.camel.fortnox.junit;

import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCamelFortnox {
	
	protected Logger	log = LoggerFactory.getLogger(this.getClass());	
	
	protected String orgNo;
	
	@Before
	public void setUp() throws Exception {
		
		URL url = ClassLoader.getSystemResource("test-config.xml");
		
		if (url==null) {
			fail("Can't find configuration file to perform tests. Please copy config-example.xml to test-config.xml and update access details.");
			return;
		}

		
		
	}

	@After
	public void tearDown() throws Exception {
	}

}
