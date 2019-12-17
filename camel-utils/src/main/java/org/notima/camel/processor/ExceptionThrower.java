package org.notima.camel.processor;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Throws an exception with given message
 * 
 * Header ExceptionMessage
 * Header ExceptionType
 * 
 * @author daniel
 *
 */
public class ExceptionThrower implements Processor {

	
	@Override
	public void process(Exchange exchange) throws Exception {

		Map<String,Object> headers = exchange.getIn().getHeaders();

		String exceptionMessage = (String)headers.get("ExceptionMessage");
		String exceptionType = (String)headers.get("ExceptionType");

		Exception e = null;
		
		if (exceptionType!=null) {
 
		} else {
			if (exceptionMessage!=null)
				e = new Exception(exceptionMessage);
			else
				e = new Exception();
		}

		if (e!=null)
			throw e;
		
	}	
	
}
