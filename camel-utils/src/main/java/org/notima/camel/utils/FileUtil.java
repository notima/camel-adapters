package org.notima.camel.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.camel.Header;


/**
 * Utility class for reading files
 * 
 * @author Daniel Tamm
 *
 */
public class FileUtil {

	public static final String DEFAULT_FILE_ENCODING = "UTF-8";

	
	
	/**
	 * Returns file name only
	 * 
	 * @param filePath
	 * @return
	 */
	public String getFileName(String filePath) {
		
		File f = new File(filePath);
		
		return f.getName();
		
	}
	
	/**
	 * Returns file name only and without suffix
	 */
	public String getFileNameWoSuffix(String filePath) {
		
		File f = new File(filePath);
		String name = f.getName();
		int idx = name.lastIndexOf(".");
		if (idx>0) {
			return name.substring(0, idx);
		} else {
			return name;
		}
		
	}
	
	/**
	 * Reads a UTF-8 file to 
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public StringBuffer readFile(@Header(value="filePath")String filePath,
						  @Header(value="fileEncoding")String fileEncoding
			) throws IOException {
		
		if (fileEncoding==null || fileEncoding.trim().length()==0)
			fileEncoding = DEFAULT_FILE_ENCODING;
		
		BufferedReader textReader = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(filePath), Charset.forName(fileEncoding)));

		String line;
		StringBuffer fileBody = new StringBuffer();
		while((line = textReader.readLine())!=null) {
			fileBody.append(line + "\r\n");
		}
		
		textReader.close();
		
		return fileBody;
		
	}
	
	/**
	 * Reads a binary file
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public byte[] readBinaryFile(@Header(value="filePath")String filePath) throws IOException {
		
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(filePath))));
		
		byte[] binary = new byte[dis.available()];
		
		dis.readFully(binary);
		dis.close();
		
		return binary;
	}
	
	/**
	 * Merges a list of string buffers to one single stringbuffer.
	 * 
	 * @param list
	 * @return
	 */
	public StringBuffer mergeStringBufferList(List<StringBuffer> list) {
		
		StringBuffer result = new StringBuffer();
		
		if (list==null) return null;
		
		for (StringBuffer b : list) {
			if (b!=null) {
				result.append(b.toString());
				result.append("\n");
			}
		}
		return result;
		
	}
	
	
}
