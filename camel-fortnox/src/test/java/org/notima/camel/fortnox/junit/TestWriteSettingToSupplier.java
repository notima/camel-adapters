package org.notima.camel.fortnox.junit;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.notima.api.fortnox.Fortnox4JSettings;
import org.notima.api.fortnox.FortnoxClient3;
import org.notima.api.fortnox.entities3.CompanySetting;
import org.notima.api.fortnox.entities3.Supplier;

public class TestWriteSettingToSupplier extends TestCamelFortnox {

	public static final String DEFAULT_TEST_SUPPLIER_ORGNO = "555555-5555";
	public static final String DEFAULT_TEST_SETTING_KEY="testKey";
	public static final String DEFAULT_TEST_SETTING_VALUE="testValue";
	
	@Test
	public void testPersistInvoiceFromOrder() {

		try {
			String configFile = ClassLoader.getSystemResource("test-config.xml").getFile();
			
			FortnoxClient3 client = new FortnoxClient3(configFile, new MockKeyProvider(orgNo));
			
			Fortnox4JSettings settings = new Fortnox4JSettings(client);

			Supplier supplier = settings.writeSettingToSupplierByOrgNo(
					DEFAULT_TEST_SUPPLIER_ORGNO, 
					DEFAULT_TEST_SETTING_KEY, 
					DEFAULT_TEST_SETTING_VALUE);
			
			
			if (supplier==null) {
				FortnoxClient3 fc = new FortnoxClient3(configFile, new MockKeyProvider(orgNo));
				CompanySetting cs = fc.getCompanySetting();
				
				log.warn("Supplier {} doesn't exist in client {}. Can't validate TestWriteSettingToSupplier.", DEFAULT_TEST_SUPPLIER_ORGNO, cs.getName());
			} else {
				log.info("Wrote setting {} to supplier no {}", DEFAULT_TEST_SETTING_KEY, supplier.getOrganisationNumber());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		
	}

}
