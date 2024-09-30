package org.notima.camel.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.notima.util.RuntimeUtil;

public class PrintLPR
{
    public int listPrinters() throws Exception {
        final List<String> cmd = new ArrayList<String>();
        cmd.add("lpstat");
        cmd.add("-p");
        return RuntimeUtil.runExternalCmd(cmd, null, null);
    }
    
    public List<String> createOptionList(@Header("printOptionStr") final String optionStr) {
        final List<String> options = new ArrayList<String>();
        if (optionStr != null && optionStr.trim().length() > 0) {
            final String[] opts = optionStr.split("\\s");
            for (int i = 0; i < opts.length; ++i) {
                options.add(opts[i]);
            }
        }
        return options;
    }
    
    public int print(final Exchange exchange, @Header("printFileName") final String file, @Header("printPages") final String pages, @Header("printOptions") final List<String> options) throws Exception {
        
    	String printerName = exchange.getProperty("printerName", String.class);
    	
    	if (file == null || file.trim().length() == 0) {
            throw new Exception("Parameter file is mandatory");
        }
        final File f = new File(file);
        if (!f.exists()) {
            throw new Exception(String.valueOf(file) + " not found.");
        }
        final List<String> cmd = new ArrayList<String>();
        cmd.add("lp");
        if (printerName != null && printerName.trim().length() > 0) {
            cmd.add("-d");
            cmd.add(printerName);
        }
        if (options != null && options.size() > 0) {
            for (final String o : options) {
                cmd.add("-o " + o);
            }
        }
        if (pages != null && pages.trim().length() > 0) {
            cmd.add("-P");
            cmd.add("1");
        }
        cmd.add(file);
        final int result = RuntimeUtil.runExternalCmd(cmd, null, null);
        return result;
    }
}

