package org.notima.camel.processor;

import java.io.File;
import java.io.FileOutputStream;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.mail.MailMessage;

/**
 * Saves attachments from an exchange (that could represent an e-mail).
 * Save path is defined in header SavePath<br>
 * The filename (unless defined in the exchange itself) can be specified by the header SaveFileName.
 * 
 * @author Daniel Tamm
 *
 */
public class AttachmentProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Message in = exchange.getIn();
		if (!(in instanceof MailMessage)) {
			return;
		}
		MailMessage inMail = (MailMessage)in;
		MimeMessage mail = inMail.getBody(MimeMessage.class);
		Object pathObject = in.getHeader("SavePath");
		String savePath = null;
		if (pathObject!=null)
			savePath = pathObject.toString();
		else
			savePath = ".";
		
		String fileName = null;
		Object fileNameObject = in.getHeader("SaveFileName");
		if (fileNameObject!=null)
			fileName = fileNameObject.toString();
		
		if (mail.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) mail.getContent();

			for (int i = 0; i < mimeMultipart.getCount(); i++) {
				
				BodyPart bp = mimeMultipart.getBodyPart(i);
				if (Part.ATTACHMENT.equalsIgnoreCase(bp.getDisposition())) {
					
		             DataHandler dh = bp.getDataHandler();
		             // get the file name
		             String filename = savePath + File.separator + (fileName!=null ? fileName : javax.mail.internet.MimeUtility.decodeText(bp.getFileName()));
		 
		             // get the content and convert it to byte[]
		             byte[] data = exchange.getContext().getTypeConverter()
		                               .convertTo(byte[].class, dh.getInputStream());
		 
		             // write the data to a file
		             FileOutputStream out = new FileOutputStream(filename);
		             out.write(data);
		             out.flush();
		             out.close();
		             
		         }
		     }
		}
		
	}

}
