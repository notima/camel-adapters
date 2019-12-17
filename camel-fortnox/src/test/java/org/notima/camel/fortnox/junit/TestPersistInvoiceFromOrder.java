package org.notima.camel.fortnox.junit;

import static org.junit.Assert.fail;

import java.io.FileReader;
import java.net.URL;

import javax.xml.bind.JAXB;

import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.notima.camel.fortnox.FortnoxClient;
import org.notima.generic.businessobjects.Order;

public class TestPersistInvoiceFromOrder {

	private String clientSecret;
	private String accessToken;
	
	@Before
	public void setUp() throws Exception {
		
		FileConfiguration fc = new XMLConfiguration();
		URL url = ClassLoader.getSystemResource("test-config.xml");
		fc.setURL(url);
		fc.load();
		
		clientSecret = fc.getString("clientSecret");
		accessToken = fc.getString("accessToken");
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {

		try {
			
			URL orderUrl = ClassLoader.getSystemResource("Order_example.xml");
			if (orderUrl==null) {
				System.err.println("Test-file Order_example.xml not found");
			}
			Order order = JAXB.unmarshal(new FileReader(orderUrl.getFile()), Order.class);
			
			FortnoxClient client = new FortnoxClient();
			client.persistInvoiceFromCanoncialOrder(clientSecret, accessToken, order, Boolean.FALSE, order.getDocumentDate(), "3051");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
	}

}
