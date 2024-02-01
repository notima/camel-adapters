package org.notima.camel.ubl;

import java.util.Arrays;
import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.notima.generic.businessobjects.Invoice;
import org.notima.generic.ubl.factory.UBL21Converter;
import org.notima.util.NotimaUtil;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DocumentReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.OrderReferenceType;
import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

public class UblCamelClient {

	/**
	 * Converts from a business objects invoice to an UBL21 invoice.
	 * 
	 * @param src
	 * @return
	 */
	@SuppressWarnings("rawtypes")
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
	@SuppressWarnings("rawtypes")
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
			String OCRsource = invoicePrefix.substring(0, 5) + NotimaUtil.fillToLength(factoringInvoiceNo, true, '0', 10);
			invoiceRef = NotimaUtil.toOCRNumberWithLengthCheck(OCRsource);
			dst.setID(invoiceRef);
		}
		
		dst = UBL21Converter.addPaymentMeansBankgiro(dst, bg, invoiceRef, bgName);
		
		return dst;
		
	}
	public InvoiceType addPaymentReferences(
			@Header("contractReference")String contractReference,
			@Header("paymentReference")String paymentReference,
			@Header("POReference")String POReference,
			@Body InvoiceType dst) {
		
		if (dst==null) {
			System.err.println("NOOOOOO INVOICE");
			return null;
		}
		
		if (dst.getIDValue()==null) {
			System.err.println("NOOOOOO IDVALUE");
		}
	
		if (paymentReference!=null && paymentReference.trim().length()>1) {
			dst.setBuyerReference(paymentReference);
		}
		
	
		if (POReference!=null && POReference.trim().length()>1) {
			OrderReferenceType orderRfType = new OrderReferenceType();
			orderRfType.setID(POReference);
			dst.setOrderReference(orderRfType);			
		}

		if (contractReference!=null && contractReference.trim().length()>1) {
			DocumentReferenceType rfType =  new DocumentReferenceType();
			rfType.setID(contractReference);
			List<DocumentReferenceType> lstRfType = Arrays.asList(rfType);
			dst.setContractDocumentReference(lstRfType);			
		}
		
		return dst;
		
	}	
	
}