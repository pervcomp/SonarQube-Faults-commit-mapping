package com.violation.helpers;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HeadersFinder {

	private  String[] metricsInt = {"classes","files","directories","functions","comment_lines","comment_lines_density","complexity","file_complexity","class_complexity","function_complexity","duplicated_lines",
		"bugs","lines","ncloc","lines_to_cover","line_coverage","package","missing_package_info","statements","violations","sqale_rating","open_issues","reliability_remediation_effort","reliability_rating",
		"security_remediation_effort","security_rating","development_cost","vulnerabilities"
	};  
	
	private   String  path = "./projects/";

	public HeadersFinder(){}
	
	
	public List<String> getHeaders(){
		File file = new File("./projects");
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
		listHeaders.add("git-changed-files");
		for (String project : projects){
	        try {
	        	String newPath = project;
	            BufferedReader br = new BufferedReader(new FileReader(newPath));
	            String s = br.readLine();
	            s = s.replaceAll("\"", "");
	            String[] array = s.split(",");
	            for (String st : array){
	            	if (st.startsWith("code_smells") || st.startsWith("squid")){
	            		if (!listHeaders.contains(st))
	            			listHeaders.add(st);
	            		}	
	            }
	            br.close();
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
		}
		return listHeaders;

	}

	
	
	
	
}
