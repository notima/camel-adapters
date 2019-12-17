package org.notima.camel.sveawebpay;

import java.io.File;

import org.apache.camel.Header;

import com.svea.webpay.common.reconciliation.PaymentReport;

/**
 * Utility methods for routes.
 * 
 * @author Daniel Tamm
 *
 */
public class SveaCamelUtil {

	
	/**
	 * If vatNo is non-null, use VAT-no for orgNo, otherwise use orgNo
	 * 
	 * @param report
	 * @param orgNo
	 * @param vatNo
	 * @return
	 */
	public PaymentReport chooseVatNoOrgNo(
			@Header(value="paymentReport")PaymentReport report, 
			@Header(value="orgNo")String orgNo,
			@Header(value="vatNo")String vatNo
			) {
		
		if (report==null)
			return null;
		
		if (vatNo!=null && vatNo.trim().length()>0) {
			report.setTaxId(vatNo);
		} else {
			report.setTaxId(orgNo);
		}
		
		return report;
	}
	
	
	/**
	 * Clean up file name from separators
	 */
	public String cleanUpFileName(@Header(value="outFileName")String fileName) {
		
		if (fileName==null) 
			return null;
		
		if (fileName.contains(File.separator)) {
			fileName = fileName.replace(File.separatorChar, '-');
		}
		
		return fileName;
		
	}
	
	
}
