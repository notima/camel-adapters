package org.notima.camel.fortnox;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.camel.Header;
import org.notima.api.fortnox.Fortnox4JSettings;
import org.notima.api.fortnox.FortnoxClient3;
import org.notima.api.fortnox.entities3.CompanySetting;
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
import org.notima.businessobjects.adapter.fortnox.FortnoxAdapter;
import org.notima.businessobjects.adapter.fortnox.FortnoxConverter;
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
	
	// Invoice Map mapping relevant key to invoice
	private Map<String,Invoice>	invoiceMap;
	// Order Map mapping relevant key to order
	private Map<String,Order> orderMap;
	// Access Token associated with the maps
	private String mapAccessToken;
	// Company name associated with the access token.
	private String	clientName;
	// Tax id associated with the access token
	private String	taxId;
	
	// Default supplier name if a new supplier is created automatically (@see getSupplierByOrgNo)
	public static String DEFAULT_NEW_SUPPLIER_NAME = "Supplier created by Fortnox4J"; 
	
	// Cache functions
	private String	lastClientSecret;
	private String	lastAccessToken;
	private FortnoxAdapter 	bof;
	
	private Logger log = LoggerFactory.getLogger(FortnoxClient.class);
	

	/**
	 * Gets access token if possible
	 * 
	 * @param clientSecret
	 * @param apiCode
	 * @return
	 * @throws Exception
	 */
	public String getAccessToken(
			@Header(value="clientSecret")String clientSecret, 
			@Header(value="apiCode")String apiCode) throws Exception {
		
		if (clientSecret==null || apiCode==null)
			return null;

		FortnoxClient3 client = new FortnoxClient3();
		String accessToken = client.getAccessToken(apiCode, clientSecret);
		
		return accessToken;
		
	}
	
	/**
	 * Keeps a cached business object factory
	 * 
	 * @param accessToken
	 * @param clientSecret
	 * @return
	 * @throws Exception
	 */
	private FortnoxAdapter getFactory(String accessToken, String clientSecret) throws Exception {
		
		if (lastClientSecret==null || lastAccessToken==null ||
				!lastClientSecret.equals(clientSecret) || !lastAccessToken.equals(accessToken)
				) {
			// Reset object factory
			bof = null;
		}
		
		if (bof==null) {
			bof = new FortnoxAdapter(accessToken, clientSecret);
			lastAccessToken = accessToken;
			lastClientSecret = clientSecret;
		}
		
		return bof;
	}
	
	/**
	 * Returns invoices with given payment term.
	 * 
	 * @param clientSecret
	 * @param accessToken
	 * @param pt
	 * @return
	 * @throws Exception
	 */
	public List<Invoice> getInvoicesWithPaymentTerm(
			@Header(value="clientSecret")String clientSecret,
			@Header(value="accessToken")String accessToken,
			@Header(value="paymentTerm")String pt) throws Exception {
		
		List<Invoice> result = new ArrayList<Invoice>();
		
		if (pt==null) return result;
		
		bof = getFactory(accessToken, clientSecret);
		
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
	 * @param clientSecret
	 * @param accessToken
	 * @param invoiceNo
	 * @return
	 */
	public org.notima.api.fortnox.entities3.Invoice getFortnoxInvoice (
			@Header(value="clientSecret")String clientSecret,
			@Header(value="accessToken")String accessToken,
			@Header(value="invoiceNo")String invoiceNo) throws Exception {
		
		bof = getFactory(accessToken, clientSecret);
		
		org.notima.api.fortnox.entities3.Invoice finvoice = (org.notima.api.fortnox.entities3.Invoice)bof.lookupNativeInvoice((String)invoiceNo);

		return finvoice;
		
	}

	/**
	 * Read company settings.
	 * 
	 * @param clientSecret		The client secret
	 * @param accessToken		The access token
	 * @return					Company settings
	 * @throws Exception		If something goes wrong
	 */
	public CompanySetting getCompanySetting(
			@Header(value="clientSecret")String clientSecret,
			@Header(value="accessToken")String accessToken
			) throws Exception {

		bof = getFactory(accessToken, clientSecret);
		
		CompanySetting cs = bof.getClient().getCompanySetting();
		return cs;
		
	}
	
	/**
	 * Convenience class to get the name of the client
	 * 
	 * @param clientSecret		The client secret
	 * @param accessToken		The access token
	 * @return		The name of the client.
	 * @throws Exception		If something goes wrong.
	 */
	public String getClientName(
			@Header(value="clientSecret")String clientSecret,
			@Header(value="accessToken")String accessToken
			) throws Exception {

		CompanySetting cs = getCompanySetting(clientSecret, accessToken);
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
			@Header(value="clientSecret")String clientSecret,
			@Header(value="accessToken")String accessToken,
			@Header(value="orgNo")String orgNo,
			@Header(value="createIfNotFound")Boolean createIfNotFound) throws Exception {
		
		if (createIfNotFound==null) createIfNotFound = Boolean.TRUE;

		bof = getFactory(accessToken, clientSecret);
		
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
	 * @param clientSecret
	 * @param accessToken
	 * @param filter
	 * @return
	 * @throws Exception
	 */
	public List<org.notima.api.fortnox.entities3.Invoice> getFortnoxInvoices(
			@Header(value="clientSecret")String clientSecret,
			@Header(value="accessToken")String accessToken,
			@Header(value="filter")String filter
			) throws Exception {
		
		bof = getFactory(accessToken, clientSecret);
		
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
	 * @param clientSecret
	 * @param accessToken
	 * @return
	 * @throws Exception
	 */
	public List<org.notima.generic.businessobjects.Invoice> getOverdueInvoices(
				@Header(value="clientSecret")String clientSecret,
				@Header(value="accessToken")String accessToken
			) throws Exception {
		
		List<org.notima.generic.businessobjects.Invoice> result = new ArrayList<org.notima.generic.businessobjects.Invoice>();
		
		List<org.notima.api.fortnox.entities3.Invoice> finvoices = getFortnoxInvoices(
				clientSecret, accessToken, FortnoxClient3.FILTER_UNPAID_OVERDUE);
		
		if (finvoices!=null) {
			for (org.notima.api.fortnox.entities3.Invoice ii : finvoices)
			result.add(FortnoxAdapter.convert(ii));
		}
		
		return result;
	}
	
	
	/**
	 * Invoices action on given invoice
	 * 
	 * @param clientSecret
	 * @param accessToken
	 * @param invoiceNo
	 * @param action
	 * @return
	 * @throws Exception
	 */
	public String invoiceAction(
				@Header(value="clientSecret")String clientSecret,
				@Header(value="accessToken")String accessToken,
				@Header(value="invoiceNo")String invoiceNo,
				@Header(value="action")String action			
			) throws Exception {
		
		bof = getFactory(accessToken, clientSecret);
		
		String result = bof.getClient().invoiceGetAction(invoiceNo, action);
		
		return result;
		
	}
	
	/**
	 * Creates a mapping of invoices using the given reference field. Only unpaid invoices are included.
	 * 
	 * @param clientSecret
	 * @param accessToken
	 * @param referenceField	Possible values ExternalInvoiceReference1, ExternalInvoiceReference2, 
	 * 							InvoiceReference, OCR, OrderReference, OurReference, YourOrderNumber, YourReference
	 * @return
	 */
	public Map<String,Invoice> getInvoiceMap(String clientSecret, String accessToken, String referenceField) throws Exception {
		
		if (invoiceMap==null || !accessToken.equals(mapAccessToken)) {

			log.info("Refreshing invoiceMap. This might take some time...");
			
			bof = getFactory(accessToken, clientSecret);
			
			CompanySetting cs = bof.getClient().getCompanySetting();
			clientName = cs.getName();
			taxId = cs.getOrganizationNumber();
			
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
				return invoiceMap;
			
			log.info("{} invoices to map up.", subsetList.size());

			int invoiceCount = 0;
			
			Invoice i;
			for (InvoiceSubset ii : subsetList) {
				i = bof.getClient().getInvoice(ii.getDocumentNumber());
				if ("yourOrderNumber".equalsIgnoreCase(referenceField)) {
					invoiceMap.put(i.getYourOrderNumber(), i);
				} else if ("ExternalInvoiceReference1".equalsIgnoreCase(referenceField)) {
					invoiceMap.put(ii.getExternalInvoiceReference1(), i);
				} else if ("ExternalInvoiceReference2".equalsIgnoreCase(referenceField)) {
					invoiceMap.put(ii.getExternalInvoiceReference2(), i);
				} else if ("InvoiceReference".equalsIgnoreCase(referenceField)) {
					invoiceMap.put(i.getInvoiceReference(), i);
				} else if ("OCR".equalsIgnoreCase(referenceField)) {
					invoiceMap.put(i.getOCR(), i);
				} else if ("OrderReference".equalsIgnoreCase(referenceField)) {
					invoiceMap.put(i.getOrderReference(), i);
				} else if ("OurReference".equalsIgnoreCase(referenceField)) {
					invoiceMap.put(i.getOurReference(), i);
				} else if ("YourReference".equalsIgnoreCase(referenceField)) {
					invoiceMap.put(i.getYourReference(), i);
				}
				
				invoiceCount++;
				if (invoiceCount%100 == 0) {
					log.info("{} invoices mapped...", invoiceCount);
				}
				
			}
			// Associate invoice map access token with access token
			mapAccessToken = accessToken;
			if (log.isDebugEnabled()) {
				log.debug("Cached " + invoiceMap.size() + " invoices for " + taxId + " : " + clientName);
			}
			
		}

		return invoiceMap;
	}

	/**
	 * Creates a mapping of orders using the given reference field. Only non-invoiced orders are included.
	 * 
	 * @param clientSecret
	 * @param accessToken
	 * @param referenceField
	 * @return
	 */
	public Map<String,Order> getOrderMap(String clientSecret, String accessToken, String referenceField) throws Exception {
		
		if (orderMap==null || !accessToken.equals(mapAccessToken)) {

			bof = getFactory(accessToken, clientSecret);
			
			CompanySetting cs = bof.getClient().getCompanySetting();
			clientName = cs.getName();
			taxId = cs.getOrganizationNumber();
			
			orderMap = new TreeMap<String, Order>();
			
			Orders orders = bof.getClient().getOrders(FortnoxClient3.FILTER_INVOICENOTCREATED);
			
			List<OrderSubset> subsetList = orders.getOrderSubset();
			if (subsetList==null)
				return orderMap;
			
			Order i;
			for (OrderSubset ii : subsetList) {
				i = bof.getClient().getOrder(ii.getDocumentNumber());
				if ("yourOrderNumber".equals(referenceField)) {
					orderMap.put(i.getYourOrderNumber(), i);
				}
			}
			// Associate invoice map access token with access token
			mapAccessToken = accessToken;
			if (log.isDebugEnabled()) {
				log.debug("Cached " + orderMap.size() + " invoices for " + taxId + " : " + clientName);
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
	 * @param clientSecret		The client secret
	 * @param accessToken		The accessToken
	 * @param invoiceRef		The actual invoice reference to look for.
	 * @param invoiceRefType	What type of reference this is, meaning the field in the invoice where this reference is found.
	 * 							Possible fields are: invoice or DocumentNumber (equal), ExternalInvoiceReference1,
	 * 							ExternalInvoiceReference2, order (meaning orderNo of order existing in Fortnox), 
	 * 							InvoiceReference, OCR, OrderReference, OurReference,
	 * 							YourOrderNumber, YourReference
	 * 
	 */
	public Invoice getInvoiceToPay(
			@Header(value="clientSecret")String clientSecret,
			@Header(value="accessToken")String accessToken,
			@Header(value="invoiceRef")String invoiceRef,
			@Header(value="invoiceRefType")String invoiceRefType,
			@Header(value="reconciliationDate")Date reconciliationDate) throws Exception {

		bof = getFactory(accessToken, clientSecret);		
		
		Invoice invoice = null;
		String invoiceNo = null;
		String orderNo = null;
		
		if (invoiceRef!=null && invoiceRef.trim().length()>0) {
			// If invoice ref type is invoice no in Fortnox, there's no need 
			// to match from order
			if (invoiceRefType==null || invoiceRefType.equalsIgnoreCase("invoice") || invoiceRefType.equalsIgnoreCase("DocumentNumber")) {
				
				invoiceNo = invoiceRef;
				
			// If reference type is order, the order in Fortnox is looked up
			// to find the related invoice in Fortnox.
			} else if (invoiceRefType.equalsIgnoreCase("order")) {
				
				orderNo = invoiceRef;
				Order order = bof.getClient().getOrder(orderNo);
				if (order.getInvoiceReference()==null || order.getInvoiceReference().trim().length()==0) {
					order = createInvoiceFromOrderNo(bof, orderNo, reconciliationDate);
				}
				
				invoiceNo = order.getInvoiceReference();
				
			} else {
				Order order = null;
				// If reference type is something else, create an invoice map using the given
				// invoice ref type.
				invoice = getInvoiceMap(clientSecret, accessToken, invoiceRefType).get(invoiceRef);
				if (invoice==null) {
					// Check if invoice hasn't been created from order
					order = getOrderMap(clientSecret, accessToken, invoiceRefType).get(invoiceRef);
					if (order!=null) {
						order = createInvoiceFromOrderNo(bof, order.getDocumentNumber(), reconciliationDate);
						invoiceNo = order.getInvoiceReference();
					}
				} else {
					invoiceNo = invoice.getDocumentNumber();
				}
			}
		}
		
		// Get the invoice
		if (invoiceNo!=null && invoice==null) {
			invoice = bof.getClient().getInvoice(invoiceNo);
		}
		
		return invoice;
		
	}
	
	
	
	/**
	 * Pays a customer invoice
	 * 
	 * @param clientSecret
	 * @param accessToken
	 * @param modeOfPayment		Payment Method Code (ie what account is this payment made to)
	 * @param invoice			Use this invoice reference to match the Fortnox Invoice. Not necessary if invoiceId already matches invoice id.
	 * @param payment
	 * @return
	 * @throws Exception
	 */
	public InvoicePayment payCustomerInvoice(
			@Header(value="clientSecret")String clientSecret,
			@Header(value="accessToken")String accessToken,
			@Header(value="modeOfPayment")String modeOfPayment,
			@Header(value="invoice")Invoice invoice,
			Payment payment) throws Exception {
		
		bof = getFactory(accessToken, clientSecret);
		InvoicePayment pmt = null;
		
		if (invoice!=null) {
			payment.setInvoiceNo(invoice.getDocumentNumber());
		} else {
			log.warn("No invoice found for payment " + payment.toString());
			return pmt;
		}
		
		// Check invoice date. Set payment date to invoice date if payment
		// has a date earlier than the invoice.
		// TODO: Change to change the payment voucher after it has been accounted.
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
		
		pmt = bof.getClient().setCustomerPayment(pmt);

		// Book the payment directly if account and mode of payment is set.
		if (pmt.getModeOfPayment()!=null && pmt.getModeOfPaymentAccount()!=null && pmt.getModeOfPaymentAccount()>0) {
			bof.getClient().performAction(true, "invoicepayment", Integer.toString(pmt.getNumber()), FortnoxClient3.ACTION_INVOICE_BOOKKEEP);
		}
		
		return pmt;
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
	 * @param clientSecret
	 * @param accessToken
	 * @param invoice
	 * @return
	 * @throws Exception
	 */
	
	public String persistInvoice(
			
			@Header(value="clientSecret")String clientSecret,
			@Header(value="accessToken")String accessToken,
			Invoice invoice) throws Exception {

		bof = getFactory(accessToken, clientSecret);
		
		Invoice result = (Invoice)bof.persistNativeInvoice(invoice);
		return result.getDocumentNumber();
		
	}

	/**
	 * Persists invoice from order
	 * 
	 * @param clientSecret
	 * @param accessToken
	 * @param order
	 * @param useArticles			If true, articles are used when creating invoice lines.
	 * @param invoiceDate			If non null, this date is used as invoice date.
	 * @param defaultRevenueAccount	If set, this is used as default revenue account for the invoice.
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public org.notima.generic.businessobjects.Invoice persistInvoiceFromCanoncialOrder(
			@Header(value="clientSecret")String clientSecret,
			@Header(value="accessToken")String accessToken,
			@Header(value="canonicalOrder")org.notima.generic.businessobjects.Order order,
			@Header(value="useArticles")Boolean useArticles,
			@Header(value="invoiceDate")Date invoiceDate,
			@Header(value="defaultRevenueAccount")String defaultRevenueAccount
			) throws Exception {
		
		if (order==null || order.getLines()==null || order.getLines().size()==0) {
			throw new Exception("Can't persist invoice from order. The order is either null or missing order lines");
		}

		bof = getFactory(accessToken, clientSecret);		

		if (defaultRevenueAccount!=null)
			bof.setDefaultRevenueAccount(defaultRevenueAccount);
		
		BasicBusinessObjectConverter<Object, Object> bbc = new BasicBusinessObjectConverter();
		org.notima.generic.businessobjects.Invoice invoice = bbc.toInvoice(order);
		
		Customer fortnoxCustomer = persistCustomerFromCanonical(clientSecret, accessToken, order.getBusinessPartner());
		invoice.setBusinessPartner(FortnoxAdapter.convert(fortnoxCustomer));
		if (invoiceDate!=null) {
			invoice.setInvoiceDate(invoiceDate);
		}
		
		bof.getClient().setUseArticles(useArticles!=null && useArticles.booleanValue());
		
		org.notima.generic.businessobjects.Invoice result = (org.notima.generic.businessobjects.Invoice)bof.persist(invoice);
		
		return result;
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public org.notima.api.fortnox.entities3.Customer persistCustomerFromCanonical(
			@Header(value="clientSecret")String clientSecret,
			@Header(value="accessToken")String accessToken,
			@Header(value="businessPartner")org.notima.generic.businessobjects.BusinessPartner bp 
			) throws Exception {
		
		bof = getFactory(accessToken, clientSecret);
		
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
				if (!e.getMessage().contains(bof.getClient().formatTaxId(bp.getTaxId(), bp.isCompany()))) {
					// The message contains the taxId if not found.
					throw e;
				} else {
					result = null;
				}
			}
			// TODO: Update customer information if necessary
		}
		if (result==null) {
			// Customer doesn't exist
			result = bof.getClient().setCustomer(bof.convert(bp));
		}

		return result;
	}
	
	/**
	 * Creates an account transfer voucher
	 * 
	 * @param clientSecret
	 * @param accessToken
	 * @param acctDate
	 * @param totalAmount
	 * @param creditAcct
	 * @param debitAcct
	 * @param description
	 * @param voucherSeries
	 * @return
	 * @throws Exception
	 */
	public String accountTransfer(
		@Header(value="clientSecret")String clientSecret, 
		@Header(value="accessToken")String accessToken,
		@Header(value="reconciliationDate")Date acctDate,
		@Header(value="totalAmount")Double totalAmount,
		@Header(value="creditAcct")String creditAcct,
		@Header(value="debitAcct")String debitAcct,
		@Header(value="transferDescription")String description,
		@Header(value="voucherSeries")String voucherSeries
		) throws Exception {
	
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

		bof = getFactory(accessToken, clientSecret);
		
		FortnoxClient3 client = (FortnoxClient3)bof.getClient();
		voucher = client.setVoucher(voucher);
		
		return voucher.getVoucherSeries() + voucher.getVoucherNumber();
		
	}

	/**
	 * Creates a fee voucher
	 * 
	 * @param clientSecret
	 * @param accessToken
	 * @param acctDate
	 * @param totalAmount
	 * @param vatAmount
	 * @param feeAccount
	 * @param srcAccount
	 * @param vatAccount
	 * @param description
	 * @param voucherSeries
	 * @return
	 * @throws Exception	if something goes wrong.
	 */
	public String accountFee (
			@Header(value="clientSecret")String clientSecret, 
			@Header(value="accessToken")String accessToken,
			@Header(value="reconciliationDate")Date acctDate,
			@Header(value="feeTotal")Double totalAmount,
			@Header(value="feeVat")Double vatAmount,
			@Header(value="feeAccount")String feeAccount,
			@Header(value="srcAccount")String srcAccount,
			@Header(value="vatAccount")String vatAccount,
			@Header(value="feeDescription")String description,
			@Header(value="voucherSeries")String voucherSeries
			) throws Exception {

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
				description);

		bof = getFactory(accessToken, clientSecret);
		
		FortnoxClient3 client = (FortnoxClient3)bof.getClient();
		voucher = client.setVoucher(voucher);
		
		return voucher.getVoucherSeries() + voucher.getVoucherNumber();
		
	}

	/**
	 * Get a single settings on the given supplier
	 * 
	 * @param clientSecret		The client secret
	 * @param accessToken		The access token
	 * @param supplierOrgNo		The org number of supplier where settings are stored.
	 * @param settingKey		The key of the setting.
	 * @return					A map of settings.
	 * @throws Exception		If something goes wrong.
	 */
	public String getSettingFromSupplier(
			@Header(value="clientSecret")String clientSecret, 
			@Header(value="accessToken")String accessToken,
			@Header(value="supplierOrgNo")String supplierOrgNo,
			@Header(value="settingKey")String settingKey) throws Exception {
		
		Map<String,String> map = getSettingsFromSupplier(clientSecret, accessToken, supplierOrgNo);
		if (map!=null) {
			return map.get(settingKey);
		} else {
			return null;
		}
		
	}
	
	/**
	 * Get a map of settings on the given supplier
	 * 
	 * @param clientSecret		The client secret
	 * @param accessToken		The access token
	 * @param supplierOrgNo		The org number of supplier where settings are stored.
	 * @return					A map of settings.
	 * @throws Exception		If something goes wrong.
	 */
	public Map<String, String> getSettingsFromSupplier(
			@Header(value="clientSecret")String clientSecret, 
			@Header(value="accessToken")String accessToken,
			@Header(value="supplierOrgNo")String supplierOrgNo) throws Exception {
		
		bof = getFactory(accessToken, clientSecret);
		FortnoxClient3 client = (FortnoxClient3)bof.getClient();
		Fortnox4JSettings settings = new Fortnox4JSettings(client);
		
		return settings.getSettingsFromSupplierByOrgNo(supplierOrgNo);
		
	}
	
	/**
	 * Writes a setting to supplier
	 * 
	 * @param clientSecret		The client secret.
	 * @param accessToken		The access token
	 * @param supplierOrgNo		Supplier's org no
	 * @param settingKey		Setting key
	 * @param settingValue		Setting value
	 * @return	The supplier if successful. Null if supplier is not found.
	 * @throws Exception 		If something goes wrong
	 */
	public Supplier writeSettingToSupplier(
			@Header(value="clientSecret")String clientSecret, 
			@Header(value="accessToken")String accessToken,
			@Header(value="supplierOrgNo")String supplierOrgNo, 
			@Header(value="settingKey")String settingKey, 
			@Header(value="settingValue")String settingValue) throws Exception {

		bof = getFactory(accessToken, clientSecret);
		FortnoxClient3 client = (FortnoxClient3)bof.getClient();
		Fortnox4JSettings settings = new Fortnox4JSettings(client);
		
		return settings.writeSettingToSupplierByOrgNo(supplierOrgNo, settingKey, settingValue);
	}
	
	
}
