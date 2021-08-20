package org.notima.camel.sveawebpay.junit;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.notima.camel.sveawebpay.WebpayAdminCamelClient;
import org.notima.generic.businessobjects.Order;

public class TestGetCreditedOrder {

	private TestAdminClientUtil		clientUtil;
	private WebpayAdminCamelClient 	client;
	private String					checkoutOrderId;
	private Order<?>				order;
	private Object					nativeOrder;
	
	
	@Before
	public void setUp() throws Exception {
		
		clientUtil = new TestAdminClientUtil();
		client = clientUtil.getTestWebpayAdminCamelClient();
		checkoutOrderId = TestConfig.creditedCheckoutOrder;
		
	}

	@Test
	public void testGetCreditedOrder() {
		
		if (checkoutOrderId==null) {
			TestConfig.testLogger.info("Skipping testGetCreditedOrder - No checkoutOrderId to test");
			return;
		}
		
		Assert.assertNotNull(client);
		try {
			order = client.getOrderByCheckoutOrderId(null, checkoutOrderId);
			Assert.assertNotNull("Checkout order " + checkoutOrderId + " not found", order);
			
			nativeOrder = order.getNativeOrder();
			Assert.assertNotNull(nativeOrder);

			if (nativeOrder instanceof org.notima.api.webpay.pmtapi.entity.Order) {
				testValuesOfNativeCheckoutOrder();
			}
			
			
		} catch (Exception e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
		
	}

	private void testValuesOfNativeCheckoutOrder() {
		org.notima.api.webpay.pmtapi.entity.Order checkoutOrder = (org.notima.api.webpay.pmtapi.entity.Order)nativeOrder;
		
		Assert.assertEquals(checkoutOrderId, checkoutOrder.getId().toString());
		
	}
	
}
