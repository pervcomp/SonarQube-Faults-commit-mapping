package com.violation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

public class MeasuresCommitsDeltaGenerator {
	private  static String basePath = "extraction/TOTAL/";
	private static List<String> indList = new LinkedList<String>();
	private static List<String> fixList = new LinkedList<String>();


	public MeasuresCommitsDeltaGenerator() {
		boolean headerPrinted = false;
		fillCommitsList();
		PrintWriter pw = null;
		List<String> list = null;
		basePath = "extraction/TOTAL/";
		basePath += "TOTAL_measures_with_commits_delta.csv";
		try {
			 pw = new PrintWriter(basePath);
			 basePath = "extraction/TOTAL/";
			 basePath += "TOTAL_measures_with_commits.csv";
			 list = Files.readAllLines((new File(basePath)).toPath(),StandardCharsets.ISO_8859_1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		String predecessor = null;
		for (String line : list){
			if (list.indexOf(line) == 0){
				writeHeader(pw,list);
				headerPrinted = true;
			}
			else if  (predecessor == null){
				writeLine(pw,line,null);
				predecessor = line;
			}
			else{
				writeLine(pw,line,predecessor);
				predecessor = line;
			}
		}
		pw.close();
	}

	/**
	 * It fills the commits lists (fixing-inducing)
	 */
	private static void fillCommitsList() {
		basePath = "extraction/TOTAL/";
		basePath += "TOTAL_cleaned.csv";
		try {
			List<String> allCleaned = Files.readAllLines((new File((basePath)).toPath()),StandardCharsets.ISO_8859_1);
			for (String line : allCleaned) {
				if (allCleaned.indexOf(line) > 0) {
					String shaInd = line.split(",")[3];
					String shaFixing = line.split(",")[5];
					indList.add(shaInd);
					fixList.add(shaFixing);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Print the header
	 * 
	 * @param pw
	 */
	private static void writeHeader(PrintWriter pw, List<String> list) {
		String row = "project" + ",";
		row += "commitSha " + ",";
		row += "commitTs " + ",";
		row += "commitType" + ",";
		String[] array = list.get(0).split(",");
		for (int i = 4; i < array.length; i++) {
			row += "Δ1 " + array[i] + ",";
		}
		pw.println(row);
	}

	private static void writeLine(PrintWriter pw, String toWrite, String predecessor) {
		NumberFormat formatter = new DecimalFormat("0.00");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		String[] arrayToWrite = toWrite.split(",");
		String[] arrayPredecessor = null;
		if (predecessor != null)
			arrayPredecessor = predecessor.split(",");
		String row = arrayToWrite[0] + ", " + arrayToWrite[1] + ", " + arrayToWrite[2] + ", " + arrayToWrite[3] + ", ";
		if (predecessor != null && arrayToWrite[0].equals(arrayPredecessor[0])) {
			for (int i = 4; i < arrayPredecessor.length; i++) {
				double delta = Double.parseDouble(arrayToWrite[i]) - Double.parseDouble(arrayPredecessor[i]);
				row += formatter.format(delta) + ",";
			}
		} else {
			for (int i = 4; i < arrayToWrite.length; i++) {
				row += "0.0,";
			}
		}
		pw.println(row);

	}
}
