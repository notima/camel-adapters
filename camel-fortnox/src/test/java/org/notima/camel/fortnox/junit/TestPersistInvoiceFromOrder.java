package org.notima.camel.fortnox.junit;

import static org.junit.Assert.fail;

import java.io.FileReader;
import java.net.URL;

import javax.xml.bind.JAXB;

import org.junit.Test;
import org.notima.camel.fortnox.FortnoxClient;
import org.notima.generic.businessobjects.Order;

public class TestPersistInvoiceFromOrder extends TestCamelFortnox {

	@Test
	public void testPersistInvoiceFromOrder() {

		try {
			
			URL orderUrl = ClassLoader.getSystemResource("Order_example.xml");
			if (orderUrl==null) {
				System.err.println("Test-file Order_example.xml not found");
			}
			Order order = JAXB.unmarshal(new FileReader(orderUrl.getFile()), Order.class);
			
			FortnoxClient client = new FortnoxClient();
			client.persistInvoiceFromCanoncialOrder(orgNo, order, Boolean.FALSE, order.getDocumentDate(), true);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
	}

}
