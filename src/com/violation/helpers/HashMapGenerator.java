package com.violation.helpers;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HashMapGenerator {
	private String header;
	private LinkedHashMap<String,Integer> indexes = new LinkedHashMap<String,Integer>();
	private List<String> fields = null; 

		
	public HashMapGenerator(String header){
		this.header = header.replace("\"", "");
		HeadersFinder hf = new HeadersFinder();
		fields = hf.getHeaders();
		for (String field : fields){
			indexes.put(field, 999);
		}
		fillHashMap();
	}
	
	private void fillHashMap(){
		String[] array = header.split(",");
		for (String field : fields){
			for (int i = 0; i<array.length;i++){
				if (array[i].equals(field)){
					indexes.put(field, i);
				}
			}
		}
	}

	
	public LinkedHashMap<String,Integer> getHashMapFields(){
		return indexes;
	}
	

	
}
