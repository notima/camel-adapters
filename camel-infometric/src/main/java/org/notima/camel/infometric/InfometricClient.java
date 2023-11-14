package org.notima.camel.infometric;

import java.io.IOException;
import java.text.ParseException;

import org.apache.camel.Header;
import org.notima.businessobjects.adapter.infometric.BillingFileToInvoiceList;
import org.notima.businessobjects.adapter.infometric.InfometricAdapter;
import org.notima.generic.businessobjects.InvoiceList;

public class InfometricClient {
	
	/**
	 * Converts a CSV (semicolon separated) body to a list of OrderInvoice
	 * 
	 * @param productKey			The product key to use for the billing line
	 * @param unitPrice					The price per unit
	 * @param invoiceLineText		The text to describe the product. Dates are appended to this line.
	 * @param fileContent			The actual file to be parsed.
	 * @return						A list.
	 * @throws IOException 			If something goes wrong.
	 * @throws ParseException       If the numbers can't be parsed.
	 */
	public InvoiceList convertBillingFileToOrderInvoices(
								@Header(value="productKey")String productKey, 
								@Header(value="unitPrice")double unitPrice, 
								@Header(value="invoiceLineText")String invoiceLineText, 
								String fileContent) throws IOException, ParseException        {
	
		BillingFileToInvoiceList ia = new BillingFileToInvoiceList(null,null);
		return (ia.splitBillingFile(productKey, unitPrice, invoiceLineText, fileContent));
	}
	
}
