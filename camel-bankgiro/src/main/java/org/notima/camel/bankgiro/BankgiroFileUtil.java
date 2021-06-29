package org.notima.camel.bankgiro;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Set;

import org.apache.camel.Header;
import org.notima.bg.BgMaxFile;
import org.notima.bg.LbFile;

public class BankgiroFileUtil {

	/**
	 * Returns BG-number (with zero-padding removed)
	 * 
	 * @param fullFileName		- Must be a BGMax-file
	 * @return
	 */
	public String getBgMaxRecipient(
			
			@Header(value="fullFileName")String fullFileName,
			@Header(value="Charset")String charset) throws Exception {
		
		if (charset==null || charset.trim().length()==0) {
			charset = "Cp850";
		}
		Charset cs = Charset.forName(charset); 

		BgMaxFile inFile = new BgMaxFile();
		
		inFile.readFromFile(new File(fullFileName), cs);
		
		Set<String> recipients = inFile.getBgRecipients();
//		if (recipients.size()>1) {
//			throw new Exception("More than one recipient per file not yet supported.");
//		}
		
		if (recipients.isEmpty())
			return null;
		
		return recipients.iterator().next();
		
	}

	
	/**
	 * Splits LB-files if necessary
	 * 
	 * @param	fullFileName		The name of the file to be processed
	 * @param	destDir				Where to put the processed files.
	 * @param	charset				Charset
	 */
	public void splitLbFiles(
			
			@Header(value="fullFileName")String fullFileName,
			@Header(value="destDir")String destDir,
			@Header(value="Charset")String charset)	throws Exception {
		
		if (charset==null || charset.trim().length()==0) {
			charset = "Cp850";
		}
		Charset cs = Charset.forName(charset); 
		
		LbFile.splitToFiles(new File(fullFileName), new File(destDir), cs);
		
	}
	
	
}
