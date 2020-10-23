package org.notima.camel.ubl;

import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.notima.bg.BgUtil;
import org.notima.generic.businessobjects.Invoice;
import org.notima.generic.ubl.factory.UBL21Converter;

public class UblCamelClient {

	/**
	 * Converts from a business objects invoice to an UBL21 invoice.
	 * 
	 * @param src
	 * @return
	 */
	public InvoiceType convertFromBusinessObjectsInvoice(Invoice src) {
		InvoiceType result = UBL21Converter.convert(src);
		return result;
	}

	/**
	 * Converts from a business objects invoice to a UBL21 credit note.
	 * 
	 * @param src
	 * @return
	 */
	public CreditNoteType convertFromBusinessObjectsCreditNote(Invoice src) {
		CreditNoteType result = UBL21Converter.convertToCreditNote(src);
		return result;
	}
	
	/**
	 * Adds GLN to an invoice
	 * 
	 * @param gln
	 * @param dst
	 * @return
	 */
	public InvoiceType addGlnRecipient(@Header("glnRecipient")String gln, @Body InvoiceType dst) {
		
		if (dst==null) {
			System.err.println("NOOOOOO INVOICE");
			return null;
		}

		if (gln==null || gln.trim().length()==0)
			return dst;
		
		UBL21Converter.setGLNNumber(dst, gln);
		return dst;
		
	}
	
	/**
	 * Adds payment means bankgiro number from header bgRecipientNo
	 * 
	 * @param bg
	 * @param dst
	 * @return
	 */
	public InvoiceType addPaymentMeansBg(
			@Header("bgRecipientNo")String bg,
			@Header("factoringInvoicePrefix")String invoicePrefix,
			@Header("bpDocumentNo")String factoringInvoiceNo,
			@Header("paymentTerm")String paymentTerm,
			@Header("bgRecipientName")String bgName,
			@Body InvoiceType dst) {
		
		if (dst==null) {
			System.err.println("NOOOOOO INVOICE");
			return null;
		}
		
		if (dst.getIDValue()==null) {
			System.err.println("NOOOOOO IDVALUE");
		}
		
		String invoiceRef = dst.getIDValue();
		
		// TODO: This must be customizable
		if ("faktSveaFtg".equals(paymentTerm)) {
			// Get first 5 digits of invoice prefix and add invoice no to the end.
			String OCRsource = invoicePrefix.substring(0, 5) + BgUtil.fillToLength(factoringInvoiceNo, true, '0', 10);
			invoiceRef = BgUtil.toOCRNumberWithLengthCheck(OCRsource);
			dst.setID(invoiceRef);
		}
		
		dst = UBL21Converter.addPaymentMeansBankgiro(dst, bg, invoiceRef, bgName);
		
		return dst;
		
	}
	
}
