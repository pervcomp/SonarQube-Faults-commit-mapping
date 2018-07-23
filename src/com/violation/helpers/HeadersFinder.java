package com.violation.helpers;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HeadersFinder {

	private  String[] metricsInt = {"classes","files","directories","functions","comment_lines"};

	
	private   String  path = "./extraction/";

	public HeadersFinder(){}
	
	
	public List<String> getHeaders(){
		File file = new File("./extraction");
		String[] projects = file.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
		
		List<String> listHeaders = new LinkedList<String>();
		listHeaders.addAll(Arrays.asList(metricsInt));
		listHeaders.add("major_violations");
		listHeaders.add("minor_violations");
		listHeaders.add("info_violations");
		listHeaders.add("cognitive_complexity");
		for (String project : projects){
			if (project.equals("TOTAL"))
				continue;
	        try {
	        	String newPath = "./extraction/"+project+"/"+project+"_measures-and-issues-cleaned.csv";
	        	System.out.println(newPath);
	            String s =  Files.readAllLines(new File(newPath).toPath(),StandardCharsets.ISO_8859_1).get(0);
	            s = s.replaceAll("\"", "");
	            String[] array = s.split(",");
	            for (String st : array){
	            	if (st.startsWith("code_smells") || st.startsWith("squid")){
	            		if (!listHeaders.contains(st))
	            			listHeaders.add(st);
	            		}	
	            }
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
		}
		return listHeaders;

	}

	
	
	
	
}
