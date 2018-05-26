package com.violation;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.ListSelectionEvent;

import com.violation.jiraAnalyser.Application;
import com.violation.statistic.CorrelationFileGenerator;
import com.violation.statistic.StdFileGenerator;

public class ViolationFinder {
	
	public static void main(String[] args) {
		//args = 0 => all projects otherwise just projects names inserted in the args
		List<String> projects = getProjectsNames();
		if (args.length > 0)
			projects = Arrays.asList(args);
		for (String project : projects){
			System.out.println("Analysing project: " + project);
			String sourceCodeRepository = getGitUrl(project);
			try {
				Application a = new Application(project, sourceCodeRepository);
				a.mineData();
				//Step 1: downloads all jira issues. Step skipped 
				if (existListIssues(project)){
					System.out.println("List Issues found: step skipped. If you want to regenerate, please cancel them");
				}
				else{
					a.downloadIssues();
				}
				//Step 2: creates BugFixingCommit File  
				if (existBugFixingCommits(project)){
					System.out.println("List Bug Fixing found: step skipped. If you want to regenerate, please cancel them");
				}
				else{
					a.calculateBugFixingCommits();
				}
				//Step 3: creates BugInducingCommit File	
				if (existBugInducingCommits(project)){
					System.out.println("List Bug Inducing found: step skipped. If you want to regenerate, please cancel them");
				}
				else{
					a.calculateBugInducingCommits();
				}
				//Step 4: creates Correlation File per project
				CorrelationFileGenerator cfg = new CorrelationFileGenerator(project);
				cfg.createCorrelationFile();
			
			
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//Step 5: combinates to One-File TOTAL
		combineCorretionToOneFile();
		
		//Step 6: std File delta1 + delta2
		StdFileGenerator std = new StdFileGenerator();
		std.generateStd();
		
		//Step 7: Cleaner
		Cleaner c = new Cleaner();
		c.getCleanedTotalFile(projects);
		
		MeasuresCommitsGenerator mcg = new MeasuresCommitsGenerator(projects);
		
	}
	
	/*
	 * It gets the gitHub URL reading it from the properties file.
	 */
	private static String getGitUrl(String projectName){
			Path path = FileSystems.getDefault().getPath("./projects/"+projectName+"/"+projectName+".properties");
			List<String> list = null;
			try {
				list = Files.readAllLines(path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (String line : list)
				if (line.startsWith("sonar.github.repository"))
					return line.split("=")[1];

		return "";
	}
	
	private static void combineCorretionToOneFile(){
		boolean firstDone = false;
		try {
		PrintWriter pw = new PrintWriter("./projects/"+"/TOTAL/TOTAL_correlation.csv");
		File file = new File("./projects");
		String[] projects = file.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
		
		for (String project : projects){
			if (project.contains("TOTAL"))
				continue;
			String basePath = "./projects/";
			List<String> lines = Files.readAllLines((new File(basePath+"/"+project+"/"+project+"_correlation.csv").toPath()));
			if (firstDone){
				lines.remove(0);
			}
			else{
				pw.println("project,"+lines.get(0));
				lines.remove(0);
				firstDone = true;
			} 
			for (String line : lines){
				pw.println(project + ","+ line+",");
			}
			
		}
		pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/*
	 * It gets the list of the projects.
	 */
	private static List<String> getProjectsNames(){
		List<String> projectsList = new LinkedList<String>();
		File[] directories = new File("./projects").listFiles(File::isDirectory);
		for (File d : directories)
				if (!d.getName().equals("TOTAL")){
					projectsList.add(d.getName());
				}
		return projectsList;
	}
	
	/*
	 * Checks whether file like BugFixingCommits have already been analysed
	 * @param projectName
	 * @return
	 */
	private static boolean existBugFixingCommits(String projectName){
		File f = new File("projects/"+projectName+"/"+projectName+"_BugFixingCommits");
		return f.exists();
	}
	
	/*
	 * Checks whether file like BugFixingCommits have already been analysed
	 * @param projectName
	 * @return
	 */
	private static boolean existBugInducingCommits(String projectName){
		File f = new File("Project/"+projectName+"/"+projectName+"_BugInducingCommits");
		return f.exists();
	}
	
	/*
	 * Checks whether file like BugFixingCommits have already been analysed
	 * @param projectName
	 * @return
	 */
	private static boolean existListIssues(String projectName){
		File f = new File("Project/"+projectName+"/"+projectName+"_0");
		return f.exists();
	}
	
	
	

}
