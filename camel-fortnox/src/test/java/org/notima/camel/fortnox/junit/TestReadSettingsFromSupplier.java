package org.notima.camel.fortnox.junit;

import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;
import org.notima.api.fortnox.Fortnox4JSettings;
import org.notima.api.fortnox.FortnoxClient3;

public class TestReadSettingsFromSupplier extends TestCamelFortnox {

	public static final String DEFAULT_TEST_SUPPLIER_ORGNO = "555555-5555";
	public static final String DEFAULT_TEST_SETTING_KEY="testKey";
	public static final String DEFAULT_TEST_SETTING_VALUE="testValue";
	
	@Test
	public void testPersistInvoiceFromOrder() {

		try {
			
			FortnoxClient3 cl3 = new FortnoxClient3(accessToken, clientSecret);
			
			Fortnox4JSettings set = new Fortnox4JSettings(cl3);
			
			Map<String,String> settings = set.getSettingsFromSupplierByOrgNo(DEFAULT_TEST_SUPPLIER_ORGNO);
			
			
			if (settings==null) {
				log.warn("No settings found on supplier {}. Can't validate TestWriteSettingToSupplier.", DEFAULT_TEST_SUPPLIER_ORGNO);
			} else {
				log.info("Read settings from supplier with orgno {}", DEFAULT_TEST_SUPPLIER_ORGNO);
				for (String s : settings.keySet()) {
					log.info("Found setting {} => {}", s, settings.get(s));
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
	}

}
