package org.notima.camel.utils;

import java.util.List;
import java.util.ArrayList;

public class PrintLPRTest
{
    public static void main(final String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: PrintLPRTest printerName fileName [pages]");
            return;
        }
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
            lp.print(args[0], args[1], pages, options);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
