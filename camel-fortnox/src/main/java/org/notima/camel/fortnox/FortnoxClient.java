package org.notima.camel.fortnox;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Header;
import org.notima.api.fortnox.Fortnox4JSettings;
import org.notima.api.fortnox.FortnoxClient3;
import org.notima.api.fortnox.FortnoxConstants;
import org.notima.api.fortnox.FortnoxException;
import org.notima.api.fortnox.FortnoxInvoiceException;
import org.notima.api.fortnox.FortnoxScopeException;
import org.notima.api.fortnox.clients.FortnoxCredentials;
import org.notima.api.fortnox.entities3.CompanySetting;
import org.notima.api.fortnox.entities3.Currency;
import org.notima.api.fortnox.entities3.Customer;
import org.notima.api.fortnox.entities3.Invoice;
import org.notima.api.fortnox.entities3.InvoicePayment;
import org.notima.api.fortnox.entities3.InvoiceSubset;
import org.notima.api.fortnox.entities3.Invoices;
import org.notima.api.fortnox.entities3.Order;
import org.notima.api.fortnox.entities3.OrderSubset;
import org.notima.api.fortnox.entities3.Orders;
import org.notima.api.fortnox.entities3.Supplier;
import org.notima.api.fortnox.entities3.Voucher;
import org.notima.api.fortnox.entities3.WriteOff;
import org.notima.api.fortnox.entities3.WriteOffs;
import org.notima.api.fortnox.oauth2.FortnoxOAuth2Client;
import org.notima.businessobjects.adapter.fortnox.FortnoxAdapter;
import org.notima.businessobjects.adapter.fortnox.FortnoxConverter;
import org.notima.businessobjects.adapter.fortnox.FortnoxExtendedClient;
import org.notima.generic.businessobjects.BasicBusinessObjectConverter;
import org.notima.generic.businessobjects.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for accessing Fortnox using Camel.
 * 
 * Copyright 2018 Notima System Integration AB (Sweden)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Daniel Tamm
 *
 */
public class FortnoxClient {
	
	// Invoice document number map
	private Map<String,Invoice> 		invoiceMap;
	
	// Invoice Map mapping relevant key to invoice
	private Map<String,List<Invoice>>	invoiceReferenceMap;
	// Order Map mapping relevant key to order
	private Map<String,Order> orderMap;
	// Access Token associated with the maps
	private String mapOrgNo;
	// Company name associated with the access token.
	private String	clientName;
	// Tax id associated with the access token
	private String	taxId;
	// The reference field used for the invoice map
	private String	referenceField;
	
	// Default supplier name if a new supplier is created automatically (@see getSupplierByOrgNo)
	public static String DEFAULT_NEW_SUPPLIER_NAME = "Supplier created by Fortnox4J"; 
	
	// Cache functions
	private String	lastClientOrgNo;
	private FortnoxAdapter 	bof;
	
	private Logger log = LoggerFactory.getLogger(FortnoxClient.class);
	
	private Map<String, Currency> currencies = new TreeMap<String, Currency>();
	

	/**
	 * Retrieves access token if possible
	 * NOTE!! Don't use this method unless you save the access token. If you loose the return value
	 * you'll have to create a new API-code.
	 * 
	 * @param 	clientSecret
	 * @param 	apiCode
	 * @return	The access token.
	 * @throws Exception	If something goes wrong.
	 */
	public FortnoxCredentials retrieveAccessTokenFromApiCode(
			@Header(value="clientId")String clientId,
			@Header(value="clientSecret")String clientSecret, 
			@Header(value="apiCode")String apiCode,
			@Header(value="redirectUri")String redirectUri) throws Exception {
		
		if (clientId==null || clientSecret==null || apiCode==null)
			return null;

		FortnoxCredentials key = FortnoxOAuth2Client.getAccessToken(clientId, clientSecret, apiCode, redirectUri);
		
		return key;
		
	}

	/**
	 * Return true if the given parameter is equal to the last access token used.
	 * 
	 * @param accessToken
	 * @return	True if accesstoken hasn't changed.
	 */
	public boolean isLastAccessToken(String accessToken) {
		if (accessToken==null || lastClientOrgNo==null)
			return false;
		
		return accessToken.equals(lastClientOrgNo);
	}
	
	/**
	 * Keeps a cached business object factory
	 * 
	 * @param orgNo
	 * @return	The current FortnoxAdapter (if none is initialized, it's initialized)
	 * @throws Exception
	 */
	public FortnoxAdapter getFortnoxAdapter(String orgNo) throws Exception {
		
		if (lastClientOrgNo==null || !lastClientOrgNo.equals(orgNo)) {
			// Reset object factory
			bof = null;
		}
		
		if (bof==null) {
			bof = new FortnoxAdapter(orgNo);
			lastClientOrgNo = orgNo;
		}
		
		return bof;
	}
	
	public FortnoxClient3 getCurrentFortnoxClient() {
		return bof.getClient();
	}
	
	/**
	 * Returns invoices with given payment term.
	 * 
	 * @param orgNo
	 * @param pt
	 * @return
	 * @throws Exception
	 */
	public List<Invoice> getInvoicesWithPaymentTerm(
			@Header(value="orgNo")String orgNo,
			@Header(value="paymentTerm")String pt) throws Exception {
		
		List<Invoice> result = new ArrayList<Invoice>();
		
		if (pt==null) return result;
		
		bof = getFortnoxAdapter(orgNo);
		
		Map<Object,Object> unposted = bof.lookupList(FortnoxAdapter.LIST_UNPOSTED);
		
		// Iterate through the result and find the unposted with given payment term
		
		for (Object invoiceNo : unposted.keySet()) {
			
			org.notima.api.fortnox.entities3.Invoice finvoice = (org.notima.api.fortnox.entities3.Invoice)bof.lookupNativeInvoice((String)invoiceNo);
			
			if (pt.equalsIgnoreCase(finvoice.getTermsOfPayment()) & !finvoice.isNotCompleted() ) {
				result.add(finvoice);
			}
			
		}
		
		return result;
	}
	
	/**
	 * Returns Fortnox invoice
	 * 
	 * @param orgNo
	 * @param invoiceNo
	 * @return
	 */
	public org.notima.api.fortnox.entities3.Invoice getFortnoxInvoice (
			@Header(value="orgNo")String orgNo,
			@Header(value="invoiceNo")String invoiceNo) throws Exception {
		
		bof = getFortnoxAdapter(orgNo);
		
		org.notima.api.fortnox.entities3.Invoice finvoice = (org.notima.api.fortnox.entities3.Invoice)bof.lookupNativeInvoice((String)invoiceNo);

		return finvoice;
		
	}

	/**
	 * Read company settings.
	 * 
	 * @param orgNo				OrgNo
	 * @return					Company settings
	 * @throws Exception		If something goes wrong
	 */
	public CompanySetting getCompanySetting(
			@Header(value="orgNo")String orgNo
			) throws Exception {

		bof = getFortnoxAdapter(orgNo);
		
		CompanySetting cs = bof.getClient().getCompanySetting();
		return cs;
		
	}
	
	/**
	 * Convenience class to get the name of the client
	 * 
	 * @param orgNo		The orgNo
	 * @return		The name of the client.
	 * @throws Exception		If something goes wrong.
	 */
	public String getClientName(
		@Header(value="orgNo")String orgNo
			) throws Exception {

		CompanySetting cs = getCompanySetting(orgNo);
		String result = null;
		if (cs!=null) {
			result = cs.getName();
		}

		return result;
	}
	
	/**
	 * Gets a supplier with given orgNo. If the supplier is missing it's created by default.
	 * 
	 * @param orgNo
	 * @return
	 */
	public Supplier getSupplierByOrgNo(
			@Header(value="orgNo")String orgNo,
			@Header(value="createIfNotFound")Boolean createIfNotFound) throws Exception {
		
		if (createIfNotFound==null) createIfNotFound = Boolean.TRUE;

		bof = getFortnoxAdapter(orgNo);
		
		Supplier supplier = bof.getClient().getSupplierByTaxId(orgNo, true);
		
		if (supplier==null) {
			supplier = new Supplier();
			supplier.setSupplierNumber(orgNo);
			supplier.setOrganisationNumber(orgNo);
			supplier.setName(DEFAULT_NEW_SUPPLIER_NAME + " : " + orgNo);
			supplier = bof.getClient().setSupplier(supplier, true);
		}
		
		return supplier;
		
	}
	
	/**
	 * Returns fortnox invoices using given filter.
	 * 
	 * @param orgNo
	 * @param filter
	 * @return
	 * @throws Exception
	 */
	public List<org.notima.api.fortnox.entities3.Invoice> getFortnoxInvoices(
			@Header(value="orgNo")String orgNo,
			@Header(value="filter")String filter
			) throws Exception {
		
		bof = getFortnoxAdapter(orgNo);
		
		List<org.notima.api.fortnox.entities3.Invoice> result = null;
		
		Invoices invoices = bof.getClient().getInvoices(filter);
		if (invoices!=null && invoices.getInvoiceSubset()!=null) {		
			result = new ArrayList<org.notima.api.fortnox.entities3.Invoice>();
			
			Invoice inv;
			for (InvoiceSubset ii : invoices.getInvoiceSubset()) {
				inv = bof.getClient().getInvoice(ii.getDocumentNumber());
				result.add(inv);
			}
		}
		
		return result;
		
	}

	/**
	 * Gets overdue invoices
	 * 
	 * @param orgNo
	 * @return
	 * @throws Exception
	 */
	public List<org.notima.generic.businessobjects.Invoice> getOverdueInvoices(
		@Header(value="orgNo")String orgNo
			) throws Exception {
		
		List<org.notima.generic.businessobjects.Invoice> result = new ArrayList<org.notima.generic.businessobjects.Invoice>();
		
		List<org.notima.api.fortnox.entities3.Invoice> finvoices = getFortnoxInvoices(
				orgNo, FortnoxClient3.FILTER_UNPAID_OVERDUE);
		
		if (finvoices!=null) {
			for (org.notima.api.fortnox.entities3.Invoice ii : finvoices)
			result.add(FortnoxAdapter.convertToCanonicalInvoice(ii));
		}
		
		return result;
	}
	
	
	/**
	 * Invoices action on given invoice
	 * 
	 * @param orgNo
	 * @param invoiceNo
	 * @param action
	 * @return
	 * @throws Exception
	 */
	public String invoiceAction(
				@Header(value="orgNo")String orgNo,
				@Header(value="invoiceNo")String invoiceNo,
				@Header(value="action")String action			
			) throws Exception {
		
		bof = getFortnoxAdapter(orgNo);
		
		String result = bof.getClient().invoiceGetAction(invoiceNo, action);
		
		return result;
		
	}
	
	/**
	 * Creates a mapping of invoices using the given reference field. Only unpaid invoices are included.
	 * 
	 * @param orgNo
	 * @param referenceField	Possible values ExternalInvoiceReference1, ExternalInvoiceReference2, 
	 * 							InvoiceReference, OCR, OrderReference, OurReference, YourOrderNumber, YourReference
	 * 
	 * @see org.notima.api.fortnox.FortnoxConstants
	 * @return
	 */
	public Map<String,List<Invoice>> getInvoiceMap(String orgNo, String referenceField, String invoiceRefRegEx) throws Exception {
		
		if (invoiceReferenceMap==null || !orgNo.equals(mapOrgNo)) {

			this.referenceField = referenceField;
			
			log.info("Refreshing invoiceMap using " + referenceField + ". This might take some time...");
			
			bof = getFortnoxAdapter(orgNo);
			
			CompanySetting cs = bof.getClient().getCompanySetting();
			clientName = cs.getName();
			taxId = cs.getOrganizationNumber();
			
			invoiceReferenceMap = new TreeMap<String, List<Invoice>>();
			invoiceMap = new TreeMap<String, Invoice>();
			
			Invoices invoices = bof.getClient().getInvoices(FortnoxClient3.FILTER_UNPAID);
			// Get unposted as well
			Invoices unposted = bof.getClient().getInvoices(FortnoxClient3.FILTER_UNBOOKED);
			
			List<InvoiceSubset> subsetList = invoices.getInvoiceSubset();
			if (subsetList!=null) {
				if (unposted.getInvoiceSubset()!=null)
					subsetList.addAll(unposted.getInvoiceSubset());
			} else {
				subsetList = unposted.getInvoiceSubset();
			}

			// If there's no unposted or unpaid invoices return empty invoiceMap
			if (subsetList==null)
				return invoiceReferenceMap;
			
			log.info("{} invoices to map up.", subsetList.size());

			Pattern re = null;
			Matcher m = null;
			
			if (invoiceRefRegEx!=null && invoiceRefRegEx.trim().length()>0) {
				re = Pattern.compile(invoiceRefRegEx);
			}
			
			int invoiceCount = 0;
			
			Invoice i;
			List<Invoice> existing = null;
			String refInFortnox = null;
			for (InvoiceSubset ii : subsetList) {
				i = bof.getClient().getInvoice(ii.getDocumentNumber());
				
				if (i.getBalance()!=null && i.getBalance().equals(Double.valueOf(0))) {
					log.info("Fortnox Invoice " + i.getDocumentNumber() + " has no open balance. Skipping.");
					continue;
				}
				
				refInFortnox = getInvoiceReference(i);

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
					existing = invoiceReferenceMap.get(refInFortnox); 
					if (existing==null) {
						existing = new ArrayList<Invoice>();
						invoiceReferenceMap.put(refInFortnox, existing);
					}
					existing.add(i);
					
				} else {
					log.info("Fortnox Invoice " + i.getDocumentNumber() + " has no reference in [" + referenceField + "].");
				}
				
				// Add to invoice map
				invoiceMap.put(i.getDocumentNumber(), i);
				
				invoiceCount++;
				if (invoiceCount%100 == 0) {
					log.info("{} invoices mapped...", invoiceCount);
				}
				
			}
			// Associate invoice map access token with access token
			mapOrgNo = orgNo;
			if (log.isDebugEnabled()) {
				log.debug("Cached " + invoiceReferenceMap.size() + " invoices for " + taxId + " : " + clientName);
			}
			
		}

		return invoiceReferenceMap;
	}

	public String getInvoiceReference(Invoice i) {

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
	
	/**
	 * Creates a mapping of orders using the given reference field. Only non-invoiced orders are included.
	 * 
	 * @param orgNo
	 * @param referenceField
	 * @param refRegEx
	 * @return
	 */
	public Map<String,Order> getOrderMap(String orgNo, String referenceField, String refRegEx) throws Exception {
		
		if (orderMap==null || !orgNo.equals(mapOrgNo)) {

			bof = getFortnoxAdapter(orgNo);
			
			CompanySetting cs = bof.getClient().getCompanySetting();
			clientName = cs.getName();
			taxId = cs.getOrganizationNumber();
			
			orderMap = new TreeMap<String, Order>();
			
			Orders orders = bof.getClient().getOrders(FortnoxClient3.FILTER_INVOICENOTCREATED);
			
			List<OrderSubset> subsetList = orders.getOrderSubset();
			if (subsetList==null)
				return orderMap;
			
			Pattern re = null;
			Matcher m = null;
			
			if (refRegEx!=null && refRegEx.trim().length()>0) {
				re = Pattern.compile(refRegEx);
			}
			
			Order i;
			int orderCount = 0;
			String fortnoxRef = null;
			for (OrderSubset ii : subsetList) {
				i = bof.getClient().getOrder(ii.getDocumentNumber());
				if ("yourOrderNumber".equals(referenceField)) {
					fortnoxRef = i.getYourOrderNumber();
				}
				
				// Apply regex if needed
				if (re!=null && fortnoxRef!=null) {
					m = re.matcher(fortnoxRef);
					if (m.matches()) {
						// If the matcher contains a group
						if (m.groupCount()>0) {
							fortnoxRef = m.group(1);
						} else {
							fortnoxRef = m.group();
						}
					}
				}
				if (fortnoxRef!=null) {
					orderMap.put(fortnoxRef, i);
				} else {
					log.debug("No ref found for order " + i.getDocumentNumber());
				}
				
				orderCount++;
				if (orderCount%100 == 0) {
					log.info("{} orders mapped...", orderCount);
				}
				
				
			}
			// Associate invoice map access token with access token
			mapOrgNo = orgNo;
			if (log.isDebugEnabled()) {
				log.debug("Cached " + orderMap.size() + " orders for " + taxId + " : " + clientName);
			}
			
		}

		return orderMap;
	}
	

	/**
	 * Helper method for easier bean access from blueprint.
	 * 
	 * @return	This client.
	 */
	public FortnoxClient getThis() {
		return this;
	}
	
	/**
	 * Gets the invoice to pay
	 * 
	 * @param orgNo
	 * @param invoiceRef		The actual invoice reference to look for.
	 * @param invoiceRefType	What type of reference this is, meaning the field in the invoice where this reference is found.
	 * 							Possible fields are: invoice or DocumentNumber (equal), ExternalInvoiceReference1,
	 * 							ExternalInvoiceReference2, order (meaning orderNo of order existing in Fortnox), 
	 * 							InvoiceReference, OCR, OrderReference, OurReference,
	 * 							YourOrderNumber, YourReference
	 * @param invoiceRefRegEx
	 * 
	 */
	public synchronized List<Invoice> getInvoiceToPay(
			@Header(value="orgNo")String orgNo,
			@Header(value="invoiceRef")String invoiceRef,
			@Header(value="invoiceRefType")String invoiceRefType,
			@Header(value="reconciliationDate")Date reconciliationDate,
			@Header(value="refRegEx")String invoiceRefRegEx) throws Exception {
		
		bof = getFortnoxAdapter(orgNo);		
		
		List<Invoice> invoices = null;
		String invoiceNo = null;
		String orderNo = null;
		
		if (invoiceRef!=null && invoiceRef.trim().length()>0) {

			Order order = null;

			// If invoice ref type is invoice no in Fortnox, there's no need 
			// to match from order
			if (invoiceRefType==null || invoiceRefType.equalsIgnoreCase("invoice") || invoiceRefType.equalsIgnoreCase("DocumentNumber")) {
				
				invoiceNo = invoiceRef;
				
			// If reference type is order, the order in Fortnox is looked up
			// to find the related invoice in Fortnox.
			} else if (invoiceRefType.equalsIgnoreCase("order")) {
				
				log.debug("Looking up invoice for order " + orderNo);
				
				orderNo = invoiceRef;
				try {
					order = bof.getClient().getOrder(orderNo);
					if (order.getInvoiceReference()==null || order.getInvoiceReference().trim().length()==0) {
						order = createInvoiceFromOrderNo(bof, orderNo, reconciliationDate);
					}
					invoiceNo = order.getInvoiceReference();
				} catch (Exception ee) {

					// Obviously we couldn't use the orderNo to lookup.
					log.info("Can't lookup order [" + orderNo + "]: " + ee.getMessage());
					
				}
				
			} else {
				
				// If reference type is something else, create an invoice map using the given
				// invoice ref type.
				invoices = getInvoiceMap(orgNo, invoiceRefType, invoiceRefRegEx).get(invoiceRef);
				if (invoices==null) {
					
					 if (!invoiceRefType.toLowerCase().contains("invoice") && !invoiceRefType.toLowerCase().contains("ocr")) { 
						
						// Check if invoice hasn't been created from order
						order = getOrderMap(orgNo, invoiceRefType, invoiceRefRegEx).get(invoiceRef);
						if (order!=null) {
							order = createInvoiceFromOrderNo(bof, order.getDocumentNumber(), reconciliationDate);
							invoiceNo = order.getInvoiceReference();
						}
						
					 }
				}
			}
		}
		
		// Get the invoice if there's an invoice no but not found in the invoice map.
		if (invoiceNo!=null && invoices==null) {
			Invoice invoice = bof.getClient().getInvoice(invoiceNo);
			if (invoice!=null) {
				invoices = new ArrayList<Invoice>();
				invoices.add(invoice);
			}
		}
		
		return invoices;
		
	}
	
	
	
	/**
	 * Pays a customer invoice
	 * 
	 * @param orgNo				Org no
	 * @param modeOfPayment		Payment Method Code (ie what account is this payment made to, betalningsvillkor)
	 * @param invoice			The Fortnox invoice to be paid.
	 * @param bookkeepPayment	If false, the payments are not bookkept (only preliminary).
	 * @param payment			The payment to be applied
	 * @return
	 * @throws Exception
	 */
	public InvoicePayment payCustomerInvoice(
			@Header(value="orgNo")String orgNo,
			@Header(value="modeOfPayment")String modeOfPayment,
			@Header(value="invoice")Invoice invoice,
			@Header(value="bookkeepPayment")Boolean bookkeepPayment,
			Payment payment) throws Exception {
		
		// TODO: Use FortnoxClient3.payCustomerInvoice to avoid duplicating code
		
		bof = getFortnoxAdapter(orgNo);
		InvoicePayment pmt = null;
		
		if (invoice!=null) {
			payment.setInvoiceNo(invoice.getDocumentNumber());
		} else {
			log.warn("No invoice found for payment " + payment.toString());
			return pmt;
		}
		
		if (bookkeepPayment==null) {
			bookkeepPayment=Boolean.TRUE;
		}
		
		// Check invoice date. Set payment date to invoice date if payment
		// has a date earlier than the invoice.
		// If this behavior is unwanted, correct the accounting before calling payCustomerInvoice.
		Date invoiceDate = FortnoxClient3.s_dfmt.parse(invoice.getInvoiceDate()); 
		if (invoiceDate.after(payment.getPaymentDate())) {
			payment.setPaymentDate(invoiceDate);
		}
		
		pmt = FortnoxConverter.toFortnoxPayment(payment);
		
		// Round off if necessary (not EUR)
		if (pmt.getCurrency()==null || !pmt.getCurrency().equals("EUR")) {
		
			double sumWriteOffs = 0d;
			if (pmt.getWriteOffs()!=null && pmt.getWriteOffs().getWriteOff()!=null) {
				for (WriteOff wo : pmt.getWriteOffs().getWriteOff()) {
					sumWriteOffs += wo.getAmount();
				}
			}
			double diff = pmt.getAmount() + sumWriteOffs - invoice.getBalance();
			
			if (diff!=0 && Math.abs(diff)<1.0) {
				// Create a write off (rounding record)
				WriteOff wo = new WriteOff();
				wo.setCurrency(pmt.getCurrency());
				wo.setAmount(-diff);
				wo.setAccountNumber(bof.getRevenueAccount(FortnoxClient3.ACCT_ROUNDING));
				if (pmt.getWriteOffs()==null) {
					WriteOffs wofs = new WriteOffs();
					pmt.setWriteOffs(wofs);
					List<WriteOff> wofsl = wofs.getWriteOff();
					if (wofsl==null) {
						wofsl = new ArrayList<WriteOff>();
						wofs.setWriteOff(wofsl);
					}
					wofsl.add(wo);
				}
			}
		}
		
		// Set mode of payment if set
		if (modeOfPayment!=null) {
			pmt.setModeOfPayment(modeOfPayment);
		}
		
		if (!invoice.isBooked()) {
			bof.getClient().performAction(true, "invoice", Integer.toString(pmt.getInvoiceNumber()), FortnoxClient3.ACTION_INVOICE_BOOKKEEP);
		}
		
		// Make sure the payment isn't empty
		if (pmt.getAmount()==0d && (pmt.getWriteOffs()==null || pmt.getWriteOffs().getWriteOff()==null || pmt.getWriteOffs().getWriteOff().isEmpty())) {
			log.info("Payment for invoice " + pmt.getInvoiceNumber() + " is empty. Not processing.");
			return pmt;
		}
		
		// Blank the currency field since it's read-only
		if (!pmt.isDefaultAccountingCurrency()) {
			// Get currency rate if not set
			if (!pmt.hasCurrencyRate()) {
				Currency currency = getCurrency(pmt.getCurrency());
				if (currency!=null) {
					pmt.setCurrencyRate(currency.getBuyRate());
				}
			}
			pmt.currencyConvertBeforeCreation();
		}
		// Clear currency field
		pmt.setCurrency(null);
		
		pmt = bof.getClient().setCustomerPayment(pmt);

		// Book the payment directly if account and mode of payment is set.
		if (bookkeepPayment.booleanValue() && pmt!=null && pmt.getModeOfPayment()!=null && pmt.getModeOfPaymentAccount()!=null && pmt.getModeOfPaymentAccount()>0) {
			bof.getClient().performAction(true, "invoicepayment", Integer.toString(pmt.getNumber()), FortnoxClient3.ACTION_INVOICE_BOOKKEEP);
			removeFromInvoiceMapIfFullyPaid(pmt.getInvoiceNumber().toString());
		}
		
		return pmt;
	}
	
	private Currency getCurrency(String code) throws Exception {
		code = code.toUpperCase();
		Currency currency = currencies.get(code);
		if (currency==null) {
			currency = getCurrentFortnoxClient().getCurrency(code);
			if (currency!=null) {
				currencies.put(code, currency);
			}
		}
		return currency;
	}
	
	private void removeFromInvoiceMapIfFullyPaid(String invoiceNumber) {
		
		if (invoiceMap==null || invoiceMap.isEmpty()) return;
		
		try {
			Invoice invoice = bof.getClient().getInvoice(invoiceNumber);
			if (invoice.getBalance()==0) {
				// Remove from invoice map
				invoiceMap.remove(invoice.getDocumentNumber());
				removeFromReferenceMap(invoice);
			}
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		
	}
	
	private void removeFromReferenceMap(Invoice invoice) {
		
		String reference = getInvoiceReference(invoice);
		List<Invoice> invoices = invoiceReferenceMap.get(reference);
		if (invoices!=null && invoices.size()>0) {
			int removeIdx = -1;
			for (int i=0; i<invoices.size(); i++) {
				if (invoice.getDocumentNumber().equals(invoices.get(i).getDocumentNumber())) {
					removeIdx = i;
					break;
				}
			}
			if (removeIdx>=0) {
				invoices.remove(removeIdx);
				log.info("Invoice " + invoice.getDocumentNumber() + " with external reference " + reference + " was fully paid and removed from reference map.");
			}
		}
		
	}
	
	/**
	 * Creates an invoice from order number.
	 * 
	 * @param bof
	 * @param orderNo
	 * @param invoiceDate
	 * @return
	 * @throws Exception
	 */
	private Order createInvoiceFromOrderNo(FortnoxAdapter bof, String orderNo, Date invoiceDate) throws Exception {
		
		Order result = bof.getClient().orderPutAction(orderNo, FortnoxClient3.ACTION_ORDER_CREATEINVOICE);
		Invoice invoice = bof.getClient().getInvoice(result.getInvoiceReference());
		invoice.setInvoiceDate(FortnoxClient3.s_dfmt.format(invoiceDate));
		bof.persistNativeInvoice(invoice);
		bof.getClient().performAction(true, "invoice", invoice.getDocumentNumber(), FortnoxClient3.ACTION_INVOICE_BOOKKEEP);
		return result;
		
	}
	
	/**
	 * Persists invoice 
	 * 
	 * @param orgNo
	 * @param invoice
	 * @return
	 * @throws Exception
	 */
	
	public String persistInvoice(
			@Header(value="orgNo")String orgNo,
			Invoice invoice) throws Exception {

		bof = getFortnoxAdapter(orgNo);
		
		Invoice result = (Invoice)bof.persistNativeInvoice(invoice);
		return result.getDocumentNumber();
		
	}

	/**
	 * Persists invoice from order
	 * 
	 * @param orgNo
	 * @param order
	 * @param useArticles			If true, articles are used when creating invoice lines.
	 * @param invoiceDate			If non null, this date is used as invoice date.
	 * @param defaultRevenueAccount	If set, this is used as default revenue account for the invoice.
	 * @return
	 * @throws Exception
	 * @throws FortnoxInvoiceException
	 * @throws FortnoxScopeException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public org.notima.generic.businessobjects.Invoice persistInvoiceFromCanoncialOrder(
			@Header(value="orgNo")String orgNo,
			@Header(value="canonicalOrder")org.notima.generic.businessobjects.Order order,
			@Header(value="useArticles")Boolean useArticles,
			@Header(value="invoiceDate")Date invoiceDate,
			@Header(value="defaultRevenueAccount")String defaultRevenueAccount
			) throws FortnoxInvoiceException, FortnoxScopeException, Exception {
		
		if (order==null || order.getLines()==null || order.getLines().size()==0) {
			throw new Exception("Can't persist invoice from order. The order is either null or missing order lines");
		}

		bof = getFortnoxAdapter(orgNo);		

		if (defaultRevenueAccount!=null)
			bof.setDefaultRevenueAccount(defaultRevenueAccount);
		
		BasicBusinessObjectConverter<Object, Object> bbc = new BasicBusinessObjectConverter();
		org.notima.generic.businessobjects.Invoice invoice = bbc.toInvoice(order);
		
		Customer fortnoxCustomer = persistCustomerFromCanonical(orgNo, order.getBusinessPartner());
		invoice.setBusinessPartner(FortnoxAdapter.convertToBusinessPartner(fortnoxCustomer));
		if (invoiceDate!=null) {
			invoice.setInvoiceDate(invoiceDate);
		}
		
		bof.getClient().setUseArticles(useArticles!=null && useArticles.booleanValue());
		
		org.notima.generic.businessobjects.Invoice result = (org.notima.generic.businessobjects.Invoice)bof.persist(invoice);
		
		return result;
		
	}

	@SuppressWarnings("rawtypes")
	public org.notima.api.fortnox.entities3.Customer persistCustomerFromCanonical(
			@Header(value="orgNo")String orgNo,
			@Header(value="businessPartner")org.notima.generic.businessobjects.BusinessPartner bp 
			) throws Exception {

		Customer result = lookupExistingCustomerUsingCanonical(orgNo, bp);
		if (result==null) {
			// Customer doesn't exist
			result = bof.getClient().setCustomer(FortnoxAdapter.convertFromBusinessPartner(bp));
		}

		return result;
	}
	
	
	@SuppressWarnings("rawtypes")
	public org.notima.api.fortnox.entities3.Customer lookupExistingCustomerUsingCanonical(
			@Header(value="orgNo")String orgNo,
			@Header(value="businessPartner")org.notima.generic.businessobjects.BusinessPartner bp 
		)  throws Exception {

		bof = getFortnoxAdapter(orgNo);
		
		Customer result = null;
		if (bp.getbPartnerId()!=0) {
			// Try lookup
			result = bof.getClient().getCustomerByCustNo(Integer.toString(bp.getbPartnerId()));
			
			// Double check tax id if it exists
			if (bp.getTaxId()!=null && bp.getTaxId().trim().length()>0 && result.getOrganisationNumber()!=null && result.getOrganisationNumber().trim().length()>0) {
				
				if (!bof.getClient().formatTaxId(bp.getTaxId(), bp.isCompany()).equals(bof.getClient().formatTaxId(result.getOrganisationNumber(), bp.isCompany()))) {
					log.warn("Customer number " + bp.getbPartnerId() + " and org no " + bp.getTaxId() + " doesn't match. Lookup using tax id");
					// Try looking up using tax id
					try {
						result = bof.getClient().getCustomerByTaxId(bp.getTaxId(), bp.isCompany());
						bp.setbPartnerId(Integer.parseInt(result.getCustomerNumber()));
					} catch (Exception e) {
						if (!e.getMessage().contains(bof.getClient().formatTaxId(bp.getTaxId(), bp.isCompany()))) {
							// The message contains the taxId if not found.
							throw e;
						} else {
							result = null;
						}
					}
				}
			}
			// TODO: Update customer information if necessary
		} else if (bp.getIdentityNo()!=null && bp.getIdentityNo().trim().length()>0) {
			result = bof.getClient().getCustomerByCustNo(bp.getIdentityNo());
		};
		if (result==null && bp.getTaxId()!=null && bp.getTaxId().trim().length()>0) {
			// Try longer lookup
			try {
				result = bof.getClient().getCustomerByTaxId(bp.getTaxId(), bp.isCompany());
			} catch (Exception e) {
				if (e.getMessage()==null || !e.getMessage().contains(FortnoxClient3.formatTaxId(bp.getTaxId(), bp.isCompany()))) {
					// The message contains the taxId if not found.
					throw e;
				} else {
					result = null;
				}
			}
			// TODO: Update customer information if necessary
		}
		return result;
		
	}
	
	
	/**
	 * Creates an account transfer voucher
	 * 
	 * @param orgNo
	 * @param acctDate
	 * @param totalAmount
	 * @param creditAcct
	 * @param debitAcct
	 * @param description
	 * @param voucherSeries
	 * @param srcCurrency
	 * @return
	 * @throws Exception
	 * @throws FortnoxException	if something goes wrong.
	 * @throws FortnoxScopeException	if something goes wrong.
	 */
	public String accountTransfer(
		@Header(value="orgNo")String orgNo, 
		@Header(value="reconciliationDate")Date acctDate,
		@Header(value="totalAmount")Double totalAmount,
		@Header(value="creditAcct")String creditAcct,
		@Header(value="debitAcct")String debitAcct,
		@Header(value="transferDescription")String description,
		@Header(value="voucherSeries")String voucherSeries,
		@Header(value="srcCurrency")String srcCurrency
		) throws Exception, FortnoxException, FortnoxScopeException {
	
		Date trxDate = null;
		if (acctDate==null) {
			trxDate = Calendar.getInstance().getTime();
		} else {
			trxDate = acctDate;
		}
		
		FortnoxConverter ftx = new FortnoxConverter();
		
		Voucher voucher = ftx.createSingleTransactionVoucher(
				voucherSeries,
				trxDate,
				creditAcct, 
				debitAcct, 
				(double)totalAmount,
				description);

		bof = getFortnoxAdapter(orgNo);
		
		FortnoxExtendedClient fec = new FortnoxExtendedClient(bof);
		voucher = fec.accountFortnoxVoucher(voucher, srcCurrency, 0);
		
		return voucher.getVoucherSeries() + voucher.getVoucherNumber();
		
	}

	/**
	 * Creates a fee voucher
	 * 
	 * @param orgNo
	 * @param acctDate
	 * @param totalAmount
	 * @param vatAmount
	 * @param feeAccount
	 * @param srcAccount
	 * @param vatAccount
	 * @param description
	 * @param voucherSeries
	 * @param srcCurrency		The source currency of the voucher.
	 * @return
	 * @throws Exception	if something goes wrong.
	 * @throws FortnoxException	if something goes wrong.
	 * @throws FortnoxScopeException	if something goes wrong.
	 */
	public String accountFee (
			@Header(value="orgNo")String orgNo, 
			@Header(value="reconciliationDate")Date acctDate,
			@Header(value="feeTotal")Double totalAmount,
			@Header(value="feeVat")Double vatAmount,
			@Header(value="feeAccount")String feeAccount,
			@Header(value="srcAccount")String srcAccount,
			@Header(value="vatAccount")String vatAccount,
			@Header(value="feeDescription")String description,
			@Header(value="voucherSeries")String voucherSeries,
			@Header(value="costCenter")String costCenter,
			@Header(value="srcCurrency")String srcCurrency
			) throws FortnoxException, FortnoxScopeException, Exception {

		Date trxDate = null;
		if (acctDate==null) {
			trxDate = Calendar.getInstance().getTime();
		} else {
			trxDate = acctDate;
		}
		
		FortnoxConverter ftx = new FortnoxConverter();
		
		Voucher voucher = ftx.createSingleCostWithVatTransactionVoucher(
				voucherSeries,
				trxDate,
				srcAccount, 
				feeAccount, 
				vatAccount, 
				(double)totalAmount, 
				(double)vatAmount, 
				description,
				costCenter);

		bof = getFortnoxAdapter(orgNo);
		FortnoxExtendedClient fec = new FortnoxExtendedClient(bof);
		voucher = fec.accountFortnoxVoucher(voucher, srcCurrency, 0);
		
		return voucher.getVoucherSeries() + voucher.getVoucherNumber();
		
	}

	/**
	 * Get a single settings on the given supplier
	 * 
	 * @param orgNo				The client org no
	 * @param supplierOrgNo		The org number of supplier where settings are stored.
	 * @param settingKey		The key of the setting.
	 * @return					A map of settings.
	 * @throws Exception		If something goes wrong.
	 */
	public String getSettingFromSupplier(
			@Header(value="orgNo")String orgNo, 
			@Header(value="supplierOrgNo")String supplierOrgNo,
			@Header(value="settingKey")String settingKey) throws Exception {
		
		Map<String,String> map = getSettingsFromSupplier(orgNo, supplierOrgNo);
		if (map!=null) {
			return map.get(settingKey);
		} else {
			return null;
		}
		
	}
	
	/**
	 * Get a map of settings on the given supplier
	 * 
	 * @param orgNo				The client org no
	 * @param supplierOrgNo		The org number of supplier where settings are stored.
	 * @return					A map of settings.
	 * @throws Exception		If something goes wrong.
	 */
	public Map<String, String> getSettingsFromSupplier(
			@Header(value="orgNo")String orgNo, 
			@Header(value="supplierOrgNo")String supplierOrgNo) throws Exception {
		
		bof = getFortnoxAdapter(orgNo);
		FortnoxClient3 client = (FortnoxClient3)bof.getClient();
		Fortnox4JSettings settings = new Fortnox4JSettings(client);
		
		return settings.getSettingsFromSupplierByOrgNo(supplierOrgNo);
		
	}
	
	/**
	 * Writes a setting to supplier
	 * 
	 * @param clientOrgNo		The client org no
	 * @param supplierOrgNo		Supplier's org no
	 * @param settingKey		Setting key
	 * @param settingValue		Setting value
	 * @return	The supplier if successful. Null if supplier is not found.
	 * @throws Exception 		If something goes wrong
	 */
	public Supplier writeSettingToSupplier(
			@Header(value="clientOrgNo")String clientOrgNo, 
			@Header(value="accessToken")String accessToken,
			@Header(value="supplierOrgNo")String supplierOrgNo, 
			@Header(value="settingKey")String settingKey, 
			@Header(value="settingValue")String settingValue) throws Exception {

		bof = getFortnoxAdapter(clientOrgNo);
		FortnoxClient3 client = (FortnoxClient3)bof.getClient();
		Fortnox4JSettings settings = new Fortnox4JSettings(client);
		
		return settings.writeSettingToSupplierByOrgNo(supplierOrgNo, settingKey, settingValue);
	}
	
	
}
