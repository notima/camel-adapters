package org.notima.camel.fortnox;

import java.util.ArrayList;
import java.util.List;

import org.notima.api.fortnox.FortnoxConstants;
import org.notima.api.fortnox.entities3.Invoice;
import org.notima.api.fortnox.entities3.InvoiceSubset;
import org.notima.api.fortnox.entities3.Invoices;
import org.notima.businessobjects.adapter.fortnox.FortnoxAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that reads open invoices (unpaid) using the given FortnoxAdapter.
 * 
 */
public class InvoiceListReader {

	private Logger log = LoggerFactory.getLogger(InvoiceListReader.class);	
	private FortnoxAdapter bof;
	private List<InvoiceSubset> subsetList;
	private List<Invoice> invoiceList;
	private int invoiceCount = 0;
	
	public InvoiceListReader(FortnoxAdapter bof) {
		this.bof = bof;
	}
	
	public boolean isEmpty() {
		return subsetList==null || subsetList.size()==0;
	}

	public int subsetCount() {
		return (subsetList==null ? 0 : subsetList.size());
	}
	
	public void populateSubSetList() throws Exception {
		
		Invoices invoices = bof.getClient().getInvoices(FortnoxConstants.FILTER_UNPAID);
		// Get unposted as well
		Invoices unposted = bof.getClient().getInvoices(FortnoxConstants.FILTER_UNBOOKED);
		
		subsetList = invoices.getInvoiceSubset();
		if (subsetList!=null) {
			if (unposted.getInvoiceSubset()!=null)
				subsetList.addAll(unposted.getInvoiceSubset());
		} else {
			subsetList = unposted.getInvoiceSubset();
		}
		
		if (subsetList==null) {
			subsetList = new ArrayList<InvoiceSubset>();
		}
		
	}
	
	public void populateInvoiceListFromSubset() throws Exception {
		
		invoiceList = new ArrayList<Invoice>();
		if (subsetList==null) return;
		
		Invoice i;
		for (InvoiceSubset ii : subsetList) {
			
			i = bof.getClient().getInvoice(ii.getDocumentNumber());
			
			if (i.getBalance()!=null && i.getBalance().equals(Double.valueOf(0))) {
				log.info("Fortnox Invoice " + i.getDocumentNumber() + " has no open balance. Skipping.");
				continue;
			}
			
			invoiceList.add(i);
			
			invoiceCount++;
			if (invoiceCount%100 == 0) {
				log.info("{} invoices retrieved...", invoiceCount);
			}
			
			
		}
		
	}

	public List<InvoiceSubset> getSubsetList() {
		return subsetList;
	}

	public List<Invoice> getInvoiceList() {
		return invoiceList;
	}

	
	
}
