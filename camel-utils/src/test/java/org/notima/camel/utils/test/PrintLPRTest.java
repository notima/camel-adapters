package org.notima.camel.utils.test;

import java.util.List;

import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.notima.camel.utils.PrintLPR;
import org.apache.camel.Exchange;
import java.util.ArrayList;

public class PrintLPRTest
{
    public static void main(final String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: PrintLPRTest printerName fileName [pages]");
            return;
        }
        
        DefaultCamelContext ctx = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(ctx);
        exchange.setProperty("printerName", args[0]);
        
        final PrintLPR lp = new PrintLPR();
        final List<String> options = new ArrayList<String>();
        
        options.add("zeMediaTracking=Mark");
        options.add("PageSize=Custom.100x25");
        options.add("zeLabelTop=-40");
        String pages = null;
        if (args.length > 2) {
            pages = args[2];
        }
        try {
            lp.listPrinters();
            lp.print(exchange, args[1], pages, options);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
