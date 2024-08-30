package org.notima.camel.fortnox;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.notima.api.fortnox.FortnoxConstants;
import org.notima.api.fortnox.entities3.Invoice;
import org.notima.businessobjects.adapter.fortnox.FortnoxAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * A class for keeping one or more invoice mappers for specific fields.
 * 
 */
public class InvoiceReferenceMapper {

	private FortnoxAdapter 	bof;

	private String orgNo;
	
	private Map<String, InvoiceReferenceMap>  referenceMapsByReferenceField = new TreeMap<String, InvoiceReferenceMap>();
	private InvoiceListReader	invoiceListReader;
	private InvoiceReferenceMap	selectedReferenceMap;
	private TaxSubjectInvoiceMapper		taxSubjectInvoiceMapper;
	
	private Logger log = LoggerFactory.getLogger(InvoiceReferenceMapper.class);

	public InvoiceReferenceMapper(FortnoxAdapter bof) {
		this.orgNo = bof.getCurrentTenant().getTaxId();
		this.bof = bof;
		taxSubjectInvoiceMapper = new TaxSubjectInvoiceMapper(orgNo);
	}
	
	public boolean isFortnoxClientOrgNo(String orgNo) {
		if (orgNo==null) orgNo = "";
		return this.orgNo.equals(orgNo.trim());
	}
	
	private void populateInvoiceReader() throws Exception {
		if (invoiceListReader==null) {
			invoiceListReader = new InvoiceListReader(bof);
			log.info("Populating invoice list for " + orgNo + ". This might take some time...");
			invoiceListReader.populateSubSetList();
			
			log.info("{} invoices to map up.", invoiceListReader.subsetCount());
			invoiceListReader.populateInvoiceListFromSubset();
		}
	}
	
	public InvoiceReferenceMap selectInvoiceMap(String referenceField, String invoiceRefRegEx) throws Exception {
		
		selectedReferenceMap = referenceMapsByReferenceField.get(referenceField);
		
		if (selectedReferenceMap==null) {

			selectedReferenceMap = new InvoiceReferenceMap(referenceField);
			// Add to map
			referenceMapsByReferenceField.put(referenceField, selectedReferenceMap);

			log.info("Added invoiceMap using " + referenceField + ". This might take some time...");

			populateInvoiceReader();
			
			if (invoiceListReader.isEmpty()) {
				return selectedReferenceMap;
			}
			
			Pattern re = null;
			Matcher m = null;
			
			if (invoiceRefRegEx!=null && invoiceRefRegEx.trim().length()>0) {
				re = Pattern.compile(invoiceRefRegEx);
			}
			
			String refInFortnox = null;
			for (Invoice i : invoiceListReader.getInvoiceList()) {
				
				// Add to tax subject map
				taxSubjectInvoiceMapper.addInvoice(i);
				
				refInFortnox = getInvoiceReference(referenceField, i);

				// Apply regex if needed
				if (re!=null && refInFortnox!=null) {
					m = re.matcher(refInFortnox);
					if (m.matches()) {
						// If the matcher contains a group
						if (m.groupCount()>0) {
							refInFortnox = m.group(1);
						} else {
							refInFortnox =m.group();
						}
					}
				}
				
				if (refInFortnox!=null && refInFortnox.trim().length()>0) {
					refInFortnox = refInFortnox.trim();
					selectedReferenceMap.addInvoiceToMap(refInFortnox, i);
				} else {
					log.info("Fortnox Invoice " + i.getDocumentNumber() + " has no reference in [" + referenceField + "].");
				}
				
			}
			
		}

		return selectedReferenceMap;
		
	}
	
	private String getInvoiceReference(String referenceField, Invoice i) {

		String refInFortnox = null;
		
		if (FortnoxConstants.YOURORDERNUMBER.equalsIgnoreCase(referenceField)) {
			refInFortnox = i.getYourOrderNumber();
		} else if (FortnoxConstants.EXTREF1.equalsIgnoreCase(referenceField)) {
			refInFortnox = i.getExternalInvoiceReference1();
		} else if (FortnoxConstants.EXTREF2.equalsIgnoreCase(referenceField)) {
			refInFortnox = i.getExternalInvoiceReference2();
		} else if (FortnoxConstants.INVOICEREF.equalsIgnoreCase(referenceField)) {
			refInFortnox = i.getInvoiceReference();
		} else if (FortnoxConstants.OCR.equalsIgnoreCase(referenceField)) {
			refInFortnox = i.getOCR();
		} else if (FortnoxConstants.ORDERREF.equalsIgnoreCase(referenceField)) {
			refInFortnox = i.getOrderReference();
		} else if (FortnoxConstants.OURREF.equalsIgnoreCase(referenceField)) {
			refInFortnox = i.getOurReference();
		} else if (FortnoxConstants.YOURREF.equalsIgnoreCase(referenceField)) {
			refInFortnox = i.getYourReference();
		}
		
		return refInFortnox;
		
	}
	
	
}
