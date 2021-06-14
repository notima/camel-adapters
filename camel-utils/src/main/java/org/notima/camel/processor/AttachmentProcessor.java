package org.notima.camel.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

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
		
	     // the API is a bit clunky so we need to loop
	     Map<String, DataHandler> attachments = exchange.getIn().getAttachments();
	     if (attachments.size() > 0) {
	         for (String name : attachments.keySet()) {
	             DataHandler dh = attachments.get(name);
	             // get the file name
	             String filename = savePath + File.separator + (fileName!=null ? fileName : javax.mail.internet.MimeUtility.decodeText(dh.getName()));
	 
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
