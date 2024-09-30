package org.notima.camel.ubl.test;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXB;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.junit.Test;
import org.notima.generic.businessobjects.Invoice;

public class TestBoInvoiceToEInvoice extends CamelBlueprintTestSupport {

	
	@Produce(uri = "direct:convertInvoice")
	protected ProducerTemplate template;
	
    @Override
    protected String getBlueprintDescriptor() {
        return "/OSGI-INF/blueprint/BoInvoiceToEInvoice.xml";
    }

    @Override
    protected String[] loadConfigAdminConfigurationFile() {
        // which .cfg file to use, and the name of the persistence-id
        return new String[]{"src/test/resources/etc/camel-ubl-settings.properties", "camel-ubl-settings"};
    }
	
	@Test
	public void test() throws Exception {
		
		URL url = ClassLoader.getSystemResource("sample-bo-invoice.xml");
		
		Invoice<?> invoice = JAXB.unmarshal(new File(url.getFile()), Invoice.class);
		
		template.sendBody(invoice);
		
		context.getShutdownStrategy().setTimeout(20000);
		
		System.out.println("Sleeping 20 seconds");

        getMockEndpoint("mock:result").expectedMinimumMessageCount(0);

        // assert expectations
        assertMockEndpointsSatisfied();
		
	}
	
	
}
