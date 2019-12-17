package org.notima.camel.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Processor;

/**
 * Processes a destination specification
 * 
 * @author Daniel Tamm
 *
 */
public class DestinationProcessor implements Processor{

	private Logger logger = Logger.getLogger(DestinationProcessor.class.getCanonicalName()); 
	
	public Pattern sftpPattern = Pattern.compile("^sftp://(.*?)@(.*?):(\\d+)/(.*?)(\\?.*){0,1}");
	// 1 = user
	// 2 = host
	// 3 = port
	// 4 = path
	// 5 = options
	
	/**
	 * Splits a reconcileDestinations string into a list
	 * 
	 * @param reconcileDestinations
	 * @return
	 */
	public List<String> splitDestinations(@Header(value="reconcileDestinations")String reconcileDestinations) {

		List<String> result = new ArrayList<String>();
		
		if (reconcileDestinations==null) return result;
		
		String[] list = reconcileDestinations.split(";");
		result.addAll(Arrays.asList(list));

		return result;
	}
	
	public List<String> splitFormats(@Header(value="reconcileFormats")String reconcileFormats) {
		
		List<String> result = new ArrayList<String>();
		if (reconcileFormats==null) return result;
		
		String[] list = reconcileFormats.split(";");
		result.addAll(Arrays.asList(list));
		
		return result;
			
	}
	
	/**
	 * This method expects a header named reconcileDestination
	 * 
	 * Depending on the reconcile destination necessary headers are set.
	 * 
	 */
	@Override
	public void process(Exchange exchange) throws Exception {

		Map<String,Object> headers = exchange.getIn().getHeaders();
		
		String reconcileFormats = (String)headers.get("reconcileFormats");
		String reconcileFormat = null;
		
		// Get split index
		Integer idx = exchange.getProperty(Exchange.SPLIT_INDEX, Integer.class);
		
		// Find what format
		List<String> formats = splitFormats(reconcileFormats);
		if (formats.size()>idx) {
			reconcileFormat = formats.get(idx);
		}
		
		// If reconcile format is something else than null, see what we should convert to
		if (reconcileFormat!=null) {
			headers.put("reconcileFormat", reconcileFormat);
		} else {
			headers.remove("reconcileFormat");
		}
		
	}

}
