package com.violation.helpers;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommitObject {
	
	private String sha;
	private long timeEpoch;
	private LinkedHashMap <String,Integer> indexes;
	private LinkedHashMap <String,Double> fieldsValues =  new LinkedHashMap<>();
	private int amountChangedFiles = 0;

	public CommitObject(String inducingRow, LinkedHashMap <String,Integer> indexes, String sha){
		this.sha = sha;
		if (inducingRow.isEmpty())
			return;
		this.indexes = indexes;	
		inducingRow = inducingRow.replace("\"\"", "");
		String[] array = inducingRow.split("\",");
		for (int i = 0; i< array.length; i++){
			array[i] =  array[i].replace("\"", "");	
			
		}
		
		timeEpoch=Instant.parse (array[0]).getEpochSecond();
		for (String field : indexes.keySet()){
			if (!field.startsWith("git-changed-files")){
				if (indexes.get(field) == 999)
					fieldsValues.put(field, 0.0);
				else{
					
					fieldsValues.put(field,Double.parseDouble(array[indexes.get(field)]));	
				}
			}
			else{
			    if (indexes.get(field) == 999)
			    	System.out.println();
				
				String changedFiles = array[indexes.get(field)];

				if (!changedFiles.isEmpty())
					if (!changedFiles.contains(";"))
						amountChangedFiles = 1;
					else
						amountChangedFiles = changedFiles.split(";").length;
					
			}
		}
		
	}

	public int getAmountChangedFiles(){
		return amountChangedFiles;
	}
	
	public LinkedHashMap <String,Double> getFieldsValues(){
		return fieldsValues;
	}
	
	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	public long getTimeEpoch() {
		return timeEpoch;
	}

	public void setTimeEpoch(long timeEpoch) {
		this.timeEpoch = timeEpoch;
	}






	
	
	
	
	
	
}
