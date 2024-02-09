package org.notima.camel.fortnox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.notima.api.fortnox.entities3.Invoice;
import org.notima.generic.businessobjects.TaxSubjectIdentifier;

/**
 * Class that keeps track of invoices that belong to a specific tax subject.
 * 
 */
public class TaxSubjectInvoiceMapper {

	private String fortnoxClientOrgNo;
	
	// Tax subject identifier map
	private Map<TaxSubjectIdentifier, List<Invoice>>	invoiceTaxSubjectMap = new TreeMap<TaxSubjectIdentifier, List<Invoice>>();
	
	public TaxSubjectInvoiceMapper(String fortnoxClientOrgNo) {
		if (fortnoxClientOrgNo==null) this.fortnoxClientOrgNo = "";
		this.fortnoxClientOrgNo = fortnoxClientOrgNo.trim();
	}
	
	public boolean isFortnoxClientOrgNo(String orgNo) {
		if (orgNo==null) orgNo = "";
		return fortnoxClientOrgNo.equals(orgNo.trim());
	}

	public void addInvoice(Invoice invoice) {
		
		String country = "SE";
		if (invoice.getCountry()!=null && invoice.getCountry().trim().length()>0) {
			country = invoice.getCountry().toUpperCase();
		}
		
		TaxSubjectIdentifier tsi = TaxSubjectIdentifier.createBusinessTaxSubject(
				invoice.getOrganisationNumber(), 
				country, 
				invoice.getCustomerName());
		
		List<Invoice> invoices = invoiceTaxSubjectMap.get(tsi);
		if (invoices==null) {
			invoices = new ArrayList<Invoice>();
			invoiceTaxSubjectMap.put(tsi, invoices);
		}
		invoices.add(invoice);
		
	}
	
	/**
	 * 
	 * @param tsi
	 * @return		Returns invoices for a specific tax subject identifier.
	 */
	public List<Invoice> getInvoicesForTaxSubject(TaxSubjectIdentifier tsi) {
		
		List<Invoice> result = invoiceTaxSubjectMap.get(tsi);
		if (result==null) {
			return new ArrayList<Invoice>();
		} else {
			return result;
		}
		
	}
	
	
}
