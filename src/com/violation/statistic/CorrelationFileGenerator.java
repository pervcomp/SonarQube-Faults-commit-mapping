package com.violation.statistic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import com.violation.helpers.CommitObject;
import com.violation.helpers.HashMapGenerator;

public class CorrelationFileGenerator {
	private String projectName = "";
	private String basePath = "";
	private static LinkedHashMap<String, Integer> indexes;
	private static int indexSha = 0;

	/**
	 * It creates the files projectName_correlation. It contains the delta1,
	 * delta2 and delta1 + delta2
	 * 
	 * @param projectName
	 */
	public CorrelationFileGenerator(String projectName) {
		this.projectName = projectName;
		basePath = "./projects/" + projectName;
		fillMapIndexes();
	}

	/**
	 * It fills map with indexes / fields
	 */
	private void fillMapIndexes() {
		String tempPath = basePath + "/" + projectName + "_measures-and-issues.csv";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader((tempPath)));
			String sCurrentLine = br.readLine();
			String[] array = sCurrentLine.split(",");
			for (int i = 0; i < array.length; i++) {
				if (array[i].equals("git-hash")) {
					indexSha = i;
					break;
				}
			}
			HashMapGenerator hmg = new HashMapGenerator(sCurrentLine);
			indexes = hmg.getHashMapFields();
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * It creates the correlation file
	 */
	public void createCorrelationFile() {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(basePath + "/" + projectName + "_correlation.csv");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		writeHeader(pw);
		pw.println();
		String tempPath = basePath + "/" + projectName + "_BugInducingCommits.csv";
		BufferedReader br;
		String sCurrentLine = "";
		int fail = 0;
		int count = 1;
		try {
			br = new BufferedReader(new FileReader((tempPath)));
			while ((sCurrentLine = br.readLine()) != null) {
				if (!sCurrentLine.startsWith("bug") && !sCurrentLine.isEmpty()) {
					String shaInducing = sCurrentLine.split(";")[3];
					String shaFixing = sCurrentLine.split(";")[0];
					CommitObject inducingCommit = null;
					CommitObject fixingCommit = null;
					CommitObject indMinusOneCommit = null;
					if (!getCommiString(shaInducing).isEmpty())
						inducingCommit = new CommitObject(getCommiString(shaInducing), indexes, shaInducing);
					if (!getCommiString(shaFixing).isEmpty())
						fixingCommit = new CommitObject(getCommiString(shaFixing), indexes, shaFixing);
					if (!getCommitPredString(shaInducing).isEmpty()
							&& !getCommitPredString(shaInducing).contains("measure"))
						indMinusOneCommit = new CommitObject(getCommitPredString(shaInducing), indexes,
								getSha(getCommitPredString(shaInducing)));
					if ((inducingCommit != null && fixingCommit != null) && indMinusOneCommit != null) {
						writeFinalRow(indMinusOneCommit, inducingCommit, fixingCommit, pw);
						fail = 0;
					} else {
						fail++;
						if (fail == 100)
							break;
					}
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(sCurrentLine);
		}
		pw.close();
		stripDuplicatesFromFile(basePath + "/" + projectName + "_correlation.csv");

	}

	private String getCommiString(String sha) {
		String tempPath = basePath + "/" + projectName + "_measures-and-issues.csv";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader((tempPath)));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.contains(sha)) {
					br.close();
					return sCurrentLine;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	private String getCommitPredString(String sha) {
		String tempPath = basePath + "/" + projectName + "_measures-and-issues.csv";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader((tempPath)));
			String sCurrentLine;
			String sCurrentLineTemp = "";
			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.contains(sha)) {
					br.close();
					return sCurrentLineTemp;
				}
				sCurrentLineTemp = sCurrentLine;
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	private static String getSha(String row) {
		String[] array = row.split(",");
		return array[indexSha];

	}

	private static void writeHeader(PrintWriter pw) {
		String finalRow = "ind-1 sha" + ",";
		finalRow += "ind-1 ts" + ",";
		finalRow += "ind sha" + ",";
		finalRow += "ind ts" + ",";
		finalRow += "fixing sha" + ",";
		finalRow += "fixing ts" + ",";
		finalRow += "jira_key " + ",";
		finalRow += "priority " + ",";
		for (String key : indexes.keySet()) {
			finalRow += "∆(ind-1 ind)" + key + ",";
			;
			finalRow += "∆(ind fixing)" + key + ",";
			;
			finalRow += "∆1 + ∆2 " + key + ",";
		}

		pw.println(finalRow);
	}

	private void writeFinalRow(CommitObject indMinusOne, CommitObject ind, CommitObject fixing, PrintWriter pw) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:HH:mm:SS");
		String finalRow = indMinusOne.getSha() + ",";
		finalRow += sdf.format(new Date(indMinusOne.getTimeEpoch() * 1000)) + ",";
		finalRow += ind.getSha() + ",";
		finalRow += sdf.format(new Date(ind.getTimeEpoch() * 1000)) + ",";
		finalRow += fixing.getSha() + ",";
		finalRow += sdf.format(new Date(fixing.getTimeEpoch() * 1000)) + ",";
		finalRow += getFaultKey(fixing.getSha()) + ",";
		finalRow += getPriority(fixing.getSha()) + ",";

		LinkedHashMap<String, Double> indMinusOneMap = indMinusOne.getFieldsValues();
		LinkedHashMap<String, Double> indMap = ind.getFieldsValues();
		LinkedHashMap<String, Double> fixMap = fixing.getFieldsValues();
		NumberFormat numberFormatter = new DecimalFormat("0.00");
		for (String key : indexes.keySet()) {
			if (!key.contains("git-changed-files")) {
				finalRow += numberFormatter.format(indMap.get(key) - indMinusOneMap.get(key)) + ",";
				finalRow += numberFormatter.format(fixMap.get(key) - indMap.get(key)) + ",";
				finalRow += numberFormatter.format(
						(fixMap.get(key) - indMap.get(key)) + (indMap.get(key) - indMinusOneMap.get(key))) + ",";
			} else {
				finalRow += ind.getAmountChangedFiles() - indMinusOne.getAmountChangedFiles() + ",";
				finalRow += fixing.getAmountChangedFiles() - ind.getAmountChangedFiles() + ",";
				finalRow += (ind.getAmountChangedFiles() - indMinusOne.getAmountChangedFiles())
						+ (fixing.getAmountChangedFiles() - ind.getAmountChangedFiles()) + ",";
			}

		}
		pw.println(finalRow);
	}

	/**
	 * It retrieves Faults
	 * 
	 * @param fixingSha
	 * @return
	 */
	private String getFaultKey(String fixingSha) {
		String result = "";
		String tempPath = basePath + "/" + projectName + "_BugFixingCommit.csv";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader((tempPath)));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.startsWith(fixingSha)) {
					result = sCurrentLine.split(";")[3];
					break;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * It retrieves priority from the BugFixingCommits File
	 * 
	 * @param fixingSha
	 * @return
	 */
	private String getPriority(String fixingSha) {
		String result = "";
		String tempPath = basePath + "/" + projectName + "_BugFixingCommit.csv";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader((tempPath)));
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.startsWith(fixingSha)) {
					result = sCurrentLine.split(";")[7];
					break;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * It eliminates duplicates from the correlation file
	 * 
	 * @param filename
	 */
	private void stripDuplicatesFromFile(String filename) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
			Set<String> lines = new HashSet<String>(10000); // maybe should be
															// bigger
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			String header = "";
			for (String unique : lines) {
				if (unique.startsWith("ind"))
					header = unique;
			}
			writer.write(header);
			lines.remove(header);

			for (String unique : lines) {
				writer.write(unique);
				writer.newLine();
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
