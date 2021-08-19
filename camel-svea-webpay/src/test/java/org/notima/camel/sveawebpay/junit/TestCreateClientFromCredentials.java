package org.notima.camel.sveawebpay.junit;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.svea.webpay.common.auth.ListOfSveaCredentials;

public class TestCreateClientFromCredentials {

	private ListOfSveaCredentials 	credentials;
	
	@Before
	public void setUp() throws Exception {
		credentials = TestAdminClientUtil.buildTestCredentialsFromFile();
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
