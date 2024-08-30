package org.notima.camel.fortnox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.notima.api.fortnox.entities3.Invoice;

/**
 * A reference map for a specified reference field
 * 
 */
public class InvoiceReferenceMap {

	private String referenceField;
	
	// Map of references to invoices.
	private Map<String, List<Invoice>> invoiceReferenceMap = new TreeMap<String, List<Invoice>>();
	
	// Map of document number to invoice
	private Map<String, Invoice> invoiceMap = new TreeMap<String, Invoice>();

	public InvoiceReferenceMap(String referenceField) {
		this.referenceField = referenceField;
	}
	
	public String getReferenceField() {
		return referenceField;
	}

	public void addInvoiceToMap(String reference, Invoice i) {
		
		List<Invoice> il = invoiceReferenceMap.get(reference);
		if (il==null) {
			il = new ArrayList<Invoice>();
			invoiceReferenceMap.put(reference, il);
		}
		il.add(i);
		invoiceMap.put(i.getDocumentNumber(), i);
		
	}
	
	public List<Invoice> getInvoicesWithReference(String reference) {
		
		if (invoiceReferenceMap==null) {
			return new ArrayList<Invoice>();
		}
		
		List<Invoice> result = invoiceReferenceMap.get(reference);
		
		if (result!=null) 
			return result;
		else
			return new ArrayList<Invoice>();
		
	}
	
}
