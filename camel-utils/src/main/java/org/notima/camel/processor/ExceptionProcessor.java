package org.notima.camel.processor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

public class ExceptionProcessor implements Processor {

	public static final String NO_STACK_TRACE = "NoStackTrace";
	
	@Override
	public void process(Exchange exchange) throws Exception {

		Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
				Exception.class);
		
		Map<String,Object> headers = exchange.getIn().getHeaders();

		boolean stackTrace = true;
		
		if (headers!=null && headers.containsKey(NO_STACK_TRACE)) {
			stackTrace = !(Boolean)headers.get(NO_STACK_TRACE);
		}
		
		if (e!=null) {
			Message msg = exchange.getOut();
			msg.setHeaders(headers);
			if (stackTrace) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				msg.setHeader("exceptionTrace", errors.toString());
			}
			msg.setBody(exchange.getIn().getBody());
		}
		
	}	
	
}
