package org.notima.camel.sveawebpay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.camel.Header;
import org.notima.api.webpay.pmtapi.PmtApiClientRF;
import org.notima.generic.businessobjects.Invoice;
import org.notima.generic.businessobjects.Order;
import org.notima.generic.businessobjects.Payment;

import com.svea.businessobjects.paymentgw.SveaPmtGwBusinessObjectFactory;
import com.svea.businessobjects.pmtadmin.SveaPmtAdminBusinessObjectFactory;
import com.svea.businessobjects.sveaadmin.SveaAdminBusinessObjectFactory;
import com.svea.businessobjects.sveaadmin.SveaAdminConverter;
import com.svea.webpay.common.auth.ListOfSveaCredentials;
import com.svea.webpay.common.auth.SveaCredential;
import com.svea.webpay.common.conv.JsonUtil;
import com.svea.webpay.common.reconciliation.PaymentReport;
import com.svea.webpay.common.reconciliation.PaymentReportDetail;
import com.svea.webpay.common.reconciliation.PaymentReportGroup;
import com.svea.webpayadmin.WebpayAdminClient;
import com.svea.webpayadmin.WebpayAdminClientMain;
import com.svea.webpayadminservice.client.CreateOrderRequest;
import com.svea.webpayadminservice.client.CreateOrderResponse2;
import com.svea.webpayadminservice.client.DeliveryResponse;
import com.svea.webpayadminservice.client.OrderType;

/**
 * Class for accessing Svea Webpay APIs using camel.
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
public class WebpayAdminCamelClient {
	
	private PaymentReport 	report;
	private String			orgNo;
	private String			jsonFile;
	private List<SveaCredential>	credentials;

	/**
	 * Create a new instance of a svea client using the body as configuration.
	 * 
	 * @param orgNo	Org Number. If not null and the orgnumber isn't in the configuration orgNo is used.
	 * @param body	Expected format is json-format of ListOfSveaCredentials
	 * @return		If the body is empty, null is returned.
	 */
	public static WebpayAdminCamelClient createClientFromJsonBody(
			@Header(value="orgNo")String orgNo,
			String body) {
		
		if (body==null || body.trim().length()==0)
			return null;
		
		WebpayAdminCamelClient client = new WebpayAdminCamelClient();
		if (orgNo!=null && (client.getOrgNo()==null || client.getOrgNo().trim().length()==0)) {
			client.setOrgNo(orgNo);
		}
		client.setCredentialsFromJsonBody(body);
		
		return client;
	}

	/**
	 * Creates an empty svea client.
	 */
	public WebpayAdminCamelClient() {
		report = new PaymentReport();
		credentials = new ArrayList<SveaCredential>();
	}


	/**
	 * Creates a client from identity username and password.
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public static WebpayAdminCamelClient createClientFromIdentity(
			@Header(value="identityUser")String username, 
			@Header(value="identityPass")String password) throws Exception {
		
		if (username==null || password==null) return null;
		
		WebpayAdminClient adminClient = new WebpayAdminClient();
		
		WebpayAdminCamelClient result = new WebpayAdminCamelClient();
		ListOfSveaCredentials list = adminClient.getCredentialsByIdentity(username, password);
		if (list!=null)
			result.credentials = list.getCredentials();
		
		return result;
	}
	
	/**
	 * Creates a client using the file path set in setJsonFile.
	 * 
	 * @return	A client.
	 * @throws Exception
	 */
	public WebpayAdminCamelClient createClientFromFile() throws Exception {
		if (jsonFile==null) throw new Exception("No jsonFile specified. Set property jsonFile");
		File f = new File(jsonFile);
		if (!f.exists())
			throw new Exception("Can't find jsonFile " + jsonFile);
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		StringBuffer fileContent = new StringBuffer();
		String line;
		while((line = br.readLine())!=null) {
			fileContent.append(line);
		}
		return createClientFromJsonBody(null, fileContent.toString());
	}
	
	/**
	 * Sets client credentials from jsonConfig.
	 * 
	 * @param json
	 * @return
	 */
	public WebpayAdminCamelClient setFromJsonBody(@Header(value="jsonConfig")String json) {
		setCredentialsFromJsonBody(json);
		return this;
	}
	
	/**
	 * Sets orgNo and resets report and credentials.
	 * 
	 * @param orgNo
	 */
	public WebpayAdminCamelClient setOrgNo(@Header(value="orgNo")String orgNo) {
		this.orgNo = orgNo;
		// Clear credentials
		credentials.clear();
		report = new PaymentReport();
		return this;
	}
	
	public String getOrgNo() {
		return orgNo;
	}

	
	/**
	 * Sets credentials from body in json format 
	 * 
	 * @param body
	 */
	public void setCredentialsFromJsonBody(String body) {

		ListOfSveaCredentials credList = JsonUtil.gson.fromJson(body, ListOfSveaCredentials.class);
		credentials = credList.getCredentials();
		
	}

	/**
	 * Gets credentials for this client.
	 * 
	 * @return
	 */
	public List<SveaCredential> getCredentials() {
		return credentials;
	}

	/**
	 * Removes all credentials but the given one
	 */
	public static void removeAllCredentialsBut(
			@Header(value="client")WebpayAdminCamelClient client,
			@Header(value="credAccountNo")String accountNo) {
		
		if (client.credentials==null || accountNo==null) {
			return;
		}
		
		SveaCredential keepMe = null;
		for (SveaCredential sc : client.credentials) {
			if (accountNo.equals(sc.getAccountNo())) {
				keepMe = sc;
				break;
			}
		}
		client.credentials.clear();
		if (keepMe!=null) {
			client.credentials.add(keepMe);
		}
		
	}
	
	/**
	 * Removes credential with given accountNo.
	 * 
	 * @param accountNo		AccountNo to remove
	 */
	public static void removeCredential(
			@Header(value="client")WebpayAdminCamelClient client, 
			@Header(value="credAccountNo")String accountNo) {
		if (client.credentials==null || accountNo==null)
			return;
		SveaCredential removeMe = null;
		for (SveaCredential sc : client.credentials) {
			if (accountNo.equals(sc.getAccountNo())) {
				removeMe = sc;
				break;
			}
		}
		if (removeMe!=null) {
			client.credentials.remove(removeMe);
		}
	}

	/**
	 * Sets / overrides options on given credential.
	 * If the options are null no change is made
	 * 
	 * @param client
	 * @param accountNo
	 * @param includeKickbacks
	 * @param skipTaxId
	 * @param skipEmail
	 * @param enrichFromInvoice
	 */
	public static void overrideOptions(
			@Header(value="client")WebpayAdminCamelClient client, 
			@Header(value="credAccountNo")String accountNo,
			@Header(value="includeKickbacks")String includeKickbacks,
			@Header(value="skipTaxId")String skipTaxId,
			@Header(value="skipEmail")String skipEmail,
			@Header(value="enrichFromInvoice")String enrichFromInvoice) {

		if (client.credentials==null || accountNo==null)
			return;
		
		// If all parameters are null, do nothing
		if (includeKickbacks==null && skipTaxId==null && skipEmail==null && enrichFromInvoice==null)
			return;
			
		SveaCredential credential = null;
		for (SveaCredential sc : client.credentials) {
			if (accountNo.equals(sc.getAccountNo())) {
				credential = sc;
				break;
			}
		}
		if (credential!=null) {
			
			if (includeKickbacks!=null) {
				credential.setIncludeKickbacks("Y".equalsIgnoreCase(includeKickbacks));
			}
			if (skipTaxId!=null) {
				credential.setSkipTaxId("Y".equalsIgnoreCase(skipTaxId));
			}
			if (skipEmail!=null) {
				credential.setSkipEmail("Y".equalsIgnoreCase(skipEmail));
			}
			if (enrichFromInvoice!=null) {
				credential.setEnrichFromInvoice("Y".equalsIgnoreCase(enrichFromInvoice));
			}
			
		}
		
	}
	
	/**
	 * Adds credentials from camel headers.
	 * @return
	 */
	public SveaCredential addCredentials(
			@Header(value="accountNo")String accountNo,
			@Header(value="username")String username,
			@Header(value="password")String password
			) {
		
		SveaCredential sc = getCredentialFromAccountNo(accountNo);
		if (sc==null) {
			sc = new SveaCredential();
			sc.setAccountNo(accountNo);
			credentials.add(sc);
		}
		sc.setUsername(username);
		sc.setPassword(password);
		return sc;
		
	}

	public Invoice<?> getInvoice(String accountNo, String invoiceNo) throws Exception {
		
		Invoice<?> result = null;
		
		SveaCredential cr = getCredentialFromAccountNo(accountNo);
		
		if (cr!=null) {

			SveaAdminBusinessObjectFactory bo = new SveaAdminBusinessObjectFactory();
			bo.initCredentials(cr);
			
			result = bo.lookupInvoice(invoiceNo);
			
		}
		
		return result;
		
	}
	
	/**
	 * Returns order data given Svea's order id.
	 * 
	 * @param accountNo		- Client ID
	 * @param orderId
	 * @param includeCancelledRows - If set to true, include cancelled rows.
	 * @return
	 * @throws Exception
	 */
	public Order<?> getOrder(
			@Header(value="accountNo")String accountNo,
			@Header(value="orderId")String orderId,
			@Header(value="includeCancelledRows")Boolean includeCancelledRows) throws Exception {

		Order<?> result = null;
		
		SveaCredential cr = getCredentialFromAccountNo(accountNo);
		if (cr!=null) {

			SveaAdminBusinessObjectFactory bo = new SveaAdminBusinessObjectFactory();
			if (includeCancelledRows!=null) {
				bo.setSetting(SveaAdminBusinessObjectFactory.SETTING_INCLUDE_CANCELLED_ROWS, includeCancelledRows.toString());
			}
			bo.initCredentials(cr);
			
			result = bo.lookupOrder(orderId);
			
		}
		
		return result;
	}
	
	/**
	 * Returns order data given client's order id
	 * 
	 * 
	 */
	public Order getOrderByClientOrderId(
			@Header(value="accountNo")String accountNo,
			@Header(value="clientOrderId")String clientOrderId) throws Exception {
		
		Order result = null;
		if (clientOrderId==null) return null;

		SveaCredential cr = getCredentialFromAccountNo(accountNo);
		if (cr!=null) {

			SveaAdminBusinessObjectFactory bo = new SveaAdminBusinessObjectFactory();
			bo.initCredentials(cr);
			
			result = bo.findByClientOrderId(clientOrderId);
			
		}
		
		return result;
		
	}
	
	
	/**
	 * Returns order data given checkout order id.
	 * 
	 * @param	accountNo			Can be null, but if there are many merchantIds on the same orgNo, it should be set.
	 * @param 	checkoutOrderId		The checkout orderId to lookup.
	 */
	public Order getOrderByCheckoutOrderId(
			@Header(value="accountNo")String accountNo,
			@Header(value="checkoutOrderId")String checkoutOrderId
			) throws Exception {
		
		SveaPmtAdminBusinessObjectFactory sof = new SveaPmtAdminBusinessObjectFactory();
		SveaCredential cr = getCheckoutCredential(accountNo);
		if (cr==null) throw new Exception("No credentials configured for fetching orders using checkoutOrderId.");
		sof.init(cr.getServer(), cr.getMerchantId(), cr.getSecretWord());

		Order result = sof.lookupOrder(checkoutOrderId);
		
		return result;
	}
	
	/**
	 * Returns order data given checkout order id
	 */
	public Order getOrderByTransactionId(@Header(value="accountNo")String accountNo, 
			@Header(value="transactionId")String transactionId
			) throws Exception {
		
		SveaPmtGwBusinessObjectFactory sof = new SveaPmtGwBusinessObjectFactory();
		SveaCredential cr = getCredentialFromAccountNo(accountNo);
		if (cr==null || cr.getCardMerchantId()==null) throw new Exception("No credentials configured for fetching orders using transactionId.");
		sof.init(Integer.parseInt(cr.getCardMerchantId()), cr.getCardSecretWord());

		Order result = sof.lookupOrder(transactionId);
		
		return result;
	}
	
	
	
	/**
	 * Creates and order from an OrderInvoice object.
	 * 
	 * @param src
	 * @return
	 */
	public CreateOrderResponse2 createOrder(
			@Header(value="accountNo")String accountNo, 
			Order src) throws Exception  
	{

		CreateOrderResponse2 result = null;
		
		SveaCredential cr = getCredentialFromAccountNo(accountNo);
		if (cr!=null) {

			SveaAdminBusinessObjectFactory bo = new SveaAdminBusinessObjectFactory();
			bo.initCredentials(cr);
			
			CreateOrderRequest req = bo.toSveaCreateOrderRequest(src, null);
			
			result = bo.getAdminClient().createOrder(req);
		}
		
		return result;
		
	}
	
	/**
	 * @param accountNo
	 * @return	Returns credentials using accountNo (identity) as key.
	 */
	public SveaCredential getCredentialFromAccountNo(@Header(value="accountNo")String accountNo) {
		
		for (SveaCredential cr : credentials) {
			
			if (accountNo.equals(cr.getAccountNo())) {
				return cr;
			}
			
		}
		return null;
		
	}

	/**
	 * Creates / compares configuration based on login (identity account)
	 */
	public String createJsonConfigFromServices(
			@Header(value="clientUsername")String username, 
			@Header(value="clientPassword")String password
			) throws Exception {
		
		WebpayAdminClient client = new WebpayAdminClient();
		
		ListOfSveaCredentials result = client.getCredentialsByIdentity(username, password);
		
		return JsonUtil.gson.toJson(result);
	}
	
	
	/**
	 * Return the checkout credential that has a specified merchant id 
	 * 
	 * @param 		merchantId	The merchantId
	 * @return		A checkout credential with specified merchant id. Null if not found.
	 */
	public SveaCredential getCheckoutCredential(Long merchantId) {
		
		if (merchantId!=null) {
			for (SveaCredential cr : credentials) {
				if (cr.getMerchantId()!=null && Long.parseLong(cr.getMerchantId()) == merchantId) {
					return cr;
				}
			}
		} else {
			// Merchant id is null, return first found
			return getCheckoutCredential((String)null);
		}
		
		return null;
	}
	
	/**
	 * @param	accountNo		AccountNo if there are many merchantIds to choose from. Can be null.
	 * 
	 * @return	Returns checkout credentials (if they exist)
	 */
	public SveaCredential getCheckoutCredential(String accountNo) {
		
		// If specified on account, use that
		if (accountNo!=null) {
			for (SveaCredential cr : credentials) {
				if (cr.getAccountNo().equals(accountNo) && cr.getMerchantId()!=null && cr.getMerchantId().trim().length()>4) {
					return cr;
				}
			}
		}
		
		// If not found on specified account, try any
		for (SveaCredential cr : credentials) {
			if (cr.getMerchantId()!=null && cr.getMerchantId().trim().length()>4) {
				return cr;
			}
		}
		return null;
	}
	
	/**
	 * @param	accountNo	AccountNo if there are many merchantIds to choose from. Can be null.
	 * 
	 * @return	Returns card merchant ID. NOTE! The card merchant ID must be defined in the same credential as the merchant ID.
	 */
	public String	getCardMerchantId(String accountNo) {
		SveaCredential cr = getCheckoutCredential(accountNo);
		if (cr!=null)
			return cr.getCardMerchantId();
		else 
			return null;
	}
	
	
	/**
	 * Delivers checkout order
	 * 
	 * @param merchantId			The merchantId that the checkout order belongs to (can be null).
	 * @param checkoutOrderNo		The checkout order to deliver.
	 * @return						Status of order delivered.
	 * @throws Exception
	 */
	public String deliverCheckoutOrder(Long merchantId, String checkoutOrderNo) throws Exception {
		
		if (credentials==null || getCheckoutCredential(merchantId)==null) throw new Exception("No checkout credential");
		
		PmtApiClientRF client = new PmtApiClientRF();
		
		// Find the credential for checkout
		SveaCredential cr = getCheckoutCredential(merchantId);
		client.init(cr.getServer(), cr.getMerchantId(), cr.getSecretWord());
		
		String result = client.deliverCompleteOrder(Long.parseLong(checkoutOrderNo));
		
		return result;
	}
	
	/**
	 * Delivers an order (non-checkout).
	 * 
	 * 
	 * @param accountNo
	 * @param orderNo
	 * @return
	 * @throws Exception
	 */
	public DeliveryResponse deliverOrder(
			@Header(value="accountNo")String accountNo,
			@Header(value="orderType")String orderType,
			@Header(value="orderNo")String orderNo) throws Exception {
		
		if (credentials==null) throw new Exception("No credentials");
		
		DeliveryResponse result = null;

		SveaCredential cr = getCredentialFromAccountNo(accountNo);
		if (cr!=null) {
			SveaAdminBusinessObjectFactory bo = new SveaAdminBusinessObjectFactory();
			bo.initCredentials(cr);
			OrderType ot = OrderType.fromValue(orderType);
			result = bo.deliverOrder(Long.parseLong(orderNo), ot);
			return result;
		} else {
			return null;
		}
		
	}

	/**
	 * Generates a date minus given number of days.
	 * 
	 * @param days
	 * @return
	 */
	public String getTodayMinusDaysStr(@Header(value="days")Integer days) {
		
		if (days==null) days = 0;
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -days);
		String fromDateStr = JsonUtil.dfmt.format(cal.getTime());
		
		return fromDateStr;
		
	}
	
	/**
	 * Gets a list of dates generated from lastReconcileDate
	 * Saturdays and sundays are excluded.
	 * 
	 * If both reconcileDate and lastReconcileDate is null, the day before yesterday is used.
	 * 
	 * @param	reconcileDate		If set, only this date is returned.
	 * @param	lastReconcileDate	If set and reconcileDate is null, all dates from lastReconcileDate until yesterday are returned.
	 */
	public List<String> getReconcileDates(
			@Header(value="reconcileDate") String reconcileDate, 
			@Header(value="lastReconcileDate")String lastReconcileDate) throws Exception {
		
		List<String> result = new ArrayList<String>();
		if (reconcileDate!=null && reconcileDate.trim().length()>0) {
			result.add(reconcileDate);
			return result;
		}
		
		if (lastReconcileDate==null || lastReconcileDate.trim().length()==0) {
			lastReconcileDate = getTodayMinusDaysStr(2);
		}
		java.util.Date lastDate = JsonUtil.dfmt.parse(lastReconcileDate);
		
		Calendar cal = Calendar.getInstance();
		// Set to yesterday
		cal.add(Calendar.DATE, -1);
		java.util.Date untilDate = cal.getTime(); 
		
		// Set calendar to
		cal.setTime(lastDate);
		cal.add(Calendar.DATE, 1);
		int dow = 0;
		while(cal.getTime().before(untilDate)) {
			dow = cal.get(Calendar.DAY_OF_WEEK);
			if (dow!=Calendar.SATURDAY && dow!=Calendar.SUNDAY) 
				result.add(JsonUtil.dfmt.format(cal.getTime()));
			cal.add(Calendar.DATE,1);
		}
		
		return result;
		
	}
	
	/**
	 * Reads payments from given client.
	 * 
	 * @param orgNo					The org number of the recipient
	 * @param clientName			The client name of the recipient
	 * @param fromDateStr			Read payments from this date
	 * @param untilDateStr			Read payments until this date
	 * 
	 * @return	The payment report. The report is pruned before it's returned
	 */
	public PaymentReport readPayments(@Header(value="orgNo")String orgNo,
							 @Header(value="clientName")String clientName,
							 @Header(value="fromDate")String fromDateStr, 
							 @Header(value="untilDate")String untilDateStr) throws Exception {
		
		if (fromDateStr==null) {
			
			fromDateStr = getTodayMinusDaysStr(2);
			
		}
		
		// Parse dates
		java.util.Date untilDate = null;
		java.util.Date fromDate = JsonUtil.dfmt.parse(fromDateStr);
		if (untilDateStr!=null) {
			untilDate = JsonUtil.dfmt.parse(untilDateStr);
		} else {
			untilDate = fromDate;
		}
		
		WebpayAdminClientMain reportFactory = new WebpayAdminClientMain();
		
		reportFactory.setCredentials(credentials);
		
		reportFactory.initClients(false);
		
		reportFactory.setFromDate(fromDate);
		reportFactory.setUntilDate(untilDate);
		
		report = reportFactory.fillReport();
		report.setTaxId(orgNo);
		if (report.getOrgName()==null || report.getOrgName().trim().length()==0) {
			if (clientName!=null) {
				// Remove asterisks
				clientName = clientName.replaceAll("\\*", "");
				report.setOrgName(clientName);
			}
		}

		// Prune the report to remove all empty report groups.
		report.pruneReport();
		
		return report;
		
	}

	/**
	 * 
	 * @return		Returns the first reconciliation date in the first payment report group
	 * 				Returns null if there is no report group.
	 */
	public String getFromDate() {
		if (report==null || report.getPaymentReportGroup()==null || report.getPaymentReportGroup().size()<1)
			return null;
		return report.getPaymentReportGroup().get(0).getReconciliationDate();
	}
	
	/**
	 * Returns the complete report in JSON.
	 * 
	 * @return
	 */
	public String getReportAsJson() {
		
		return JsonUtil.PaymentReportToJson(report);
		
	}
	
	/**
	 * Returns the report
	 * 
	 * @return
	 */
	public PaymentReport getReport() {
		return report;
	}

	/**
	 * Converts a SveaPayment Group to Business Object Payments
	 * 
	 * @param	group					The group to be converted
	 * @param	retriesOnly				Only include payments marked for retry
	 * @param	feesOnPaymentLevel		Book fees on payment level (instead of total for the payment type).
	 * 
	 * @throws ParseException 
	 */
	public List<Payment<PaymentReportDetail>> convertPaymentGroupToPayments(PaymentReportGroup group, 
			@Header(value="processRetryPaymentsOnly")Boolean retriesOnly,
			@Header(value="feesOnPaymentLevel")Boolean feesOnPaymentLevel
			) throws ParseException {
		
		// Make sure all accounting numbers are set
		SveaCredential cre = getCredentialFromAccountNo(group.getPaymentTypeReference());
		WebpayAdminClient.updateUndefinedFeeAccountsFromCredential(cre, group);
		
		if (feesOnPaymentLevel==null)
			feesOnPaymentLevel = Boolean.TRUE;
		SveaAdminConverter conv = new SveaAdminConverter();
		List<Payment<PaymentReportDetail>> result = conv.convert(group, 
				retriesOnly!=null ? retriesOnly.booleanValue() : false, feesOnPaymentLevel.booleanValue());
		return result;
	}

	/**
	 * Removes reference to native payment (if this object is sent to a context)
	 * where the native reference isn't known.
	 * 
	 * @param detail
	 * @return
	 */
	public Payment removeNativeReference(Payment detail) {
		if (detail!=null) {
			detail.setNativePayment(null);
		}
		return detail;
	}
	
	/**
	 * Marks given entry for retry
	 * 
	 * @param paymentDetail
	 * @param retry
	 * @return
	 */
	public PaymentReportDetail markForRetry(
			@Header(value="paymentDetail")PaymentReportDetail paymentDetail, 
			@Header(value="retry")Boolean retry) {
		
		if (paymentDetail!=null) {
			paymentDetail.setRetry(retry);
		}
		
		return paymentDetail;
	}
	
	/**
	 *	Returns true if the payment report has retry-entries.
	 *	If retry entries are found, the group flag "processRetryPaymentsOnly" is set to 
	 *  true for every group with retry entries. 
	 */
	public boolean hasRetryEntries(@Header(value="paymentReport")PaymentReport report) {
		if (report==null)
			return false;
		if (report.getPaymentReportGroup()==null || report.getPaymentReportGroup().size()==0)
			return false;
		
		boolean hasRetryEntries = false;
		
		for (PaymentReportGroup gr : report.getPaymentReportGroup()) {

			if (gr.getPaymentReportDetail()==null || gr.getPaymentReportDetail().size()==0)
				continue;

			for (PaymentReportDetail d : gr.getPaymentReportDetail()) {
				if (d!=null && d.getRetry()!=null && d.getRetry()) {
					gr.setProcessRetryPaymentsOnly(Boolean.TRUE);
					hasRetryEntries = true;
					continue;
				}
			}
			
		}
		return hasRetryEntries;
	}
	
	/**
	 * Creates a SveaPmtAdminBusinessObjectFactory
	 * 
	 * @param serverName
	 * @param merchantId
	 * @param secretWord
	 * @return
	 */
	public SveaPmtAdminBusinessObjectFactory createSveaPmtAdminBusinessObjectFactory(
			@Header(value="serverName")String serverName, 
			@Header(value="merchantId")String merchantId, 
			@Header(value="secretWord")String secretWord) {
		
		SveaPmtAdminBusinessObjectFactory f = new SveaPmtAdminBusinessObjectFactory();
		f.init(serverName, merchantId, secretWord);
		return f;
		
	}

	public String getJsonFile() {
		return jsonFile;
	}

	public void setJsonFile(String jsonFile) {
		this.jsonFile = jsonFile;
	}
	
	
	
}
