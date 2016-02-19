package trafficFlowData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * 
 * @author DonghaiYu
 * @description traffic Flow prediction Experiment Data preparation Part Step 1 (pass record data to traffic flow data)
 * @input raw data of cars' pass records(folder rawData)
 * @output  traffic flow data for per ID of per day(folder collection)
 *
 */

public class A_DataGetter {
	
	public static String resultBase = "result/";
	public static int defaultInterval = 60 * 5;//default time  interval(5min)
	
	/**
	 * @return  Map<String, int[][]>(id,traffic flow per interval)
	 * @param interValue  time interval for the traffic flow
	 * @param datafile  raw data's filename
	 * @param ids   detector IDs to statistic traffic flow
	 */
	@SuppressWarnings("deprecation")
	public static Map<String, int[][]> getTimeFlow(List<String> ids,int interValue,String datafile,String plateType){
		
		FileReader fr = null;
		try {
			fr = new FileReader(datafile);
		} catch (FileNotFoundException e) {
			System.out.println("can't find raw datafile:"+datafile);
			System.exit(1);
		}
		
		Scanner sc = new Scanner(fr);
		String content = null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int interval = interValue;
		
		if(interval <= 0 || interval >= (12 * 60 * 60)){
			interval = 60 * 5;//default 5 minutes
		}
		int timeinter = 60 * 60 / interval;
		int num[][] = new int[24][timeinter];
		
		Map<String, int[][]> map = new HashMap<String, int[][]>();
		Set<String> idnum = null;
		if (ids != null) {
			idnum = new HashSet<String>(ids); 
		}
		
			
		while(sc.hasNext()){
			content = sc.nextLine();
			String[] spl = content.split(",");
			try {
				Date date = formatter.parse(spl[5]);
				String idString = spl[2].trim();
				//String plateTypeString = spl[1].trim();
				String orientation = spl[3].trim();

				int hour = date.getHours();
				int minu = date.getMinutes();
				int sec = date.getSeconds();
				
				String idKey = idString + "_" + orientation;
				
				if(ids == null || ids != null && idnum != null && idnum.contains(idString) ){
					int[][] tempnum = map.get(idKey);
					if (tempnum == null) {
						int[][] tf = new int[24][timeinter];
						tf[hour][(minu * 60 + sec) / interval]++;
						map.put(idKey, tf);
					}else {
						tempnum[hour][(minu * 60 + sec) / interval]++;
						map.put(idKey, tempnum);
					}				
				}
				
			} catch (ParseException e) {
				System.out.println("date format error:" + content);
			}
		}		
		sc.close();
		try {
			fr.close();
		} catch (IOException e) {
			System.out.println("file close error!");
		}
		
		return map;
		
	}
	
	public static void saveResult(String savef,int[][] num,int inter){
		File f = new File(savef);
		FileWriter fw = null;
		try {
			fw = new FileWriter(f);
		} catch (IOException e1) {
			System.out.println("can't write file:" + f.getName());
		}
		for(int i=0;i<24;i++){
			for(int j=0;j<(60 * 60 /inter);j++){
				try {
					fw.write(String.format("%02d", i)+":"+ String.format("%02d", (j * inter / 60)) +","+num[i][j]+"\n");
				} catch (IOException e) {
					System.out.println("write file error:" + savef);
				}
			}
		}
		try {
			fw.flush();
			fw.close();
		} catch (IOException e) {
			System.out.println("write file error:" + f.getName());
			System.exit(1);
		}
	}
	
	public static void getTimeFlow(String folder,List<String> ids,int interval){
		
		File foldFile = new File(folder);	
		File[] dataFiles = foldFile.listFiles();
		for(File dataFile : dataFiles){
			String dataFileName = dataFile.getName();
			if(dataFile.isFile() && dataFileName.contains(".txt")){
				System.out.println("getting data from raw datfile:" + dataFileName);
								
				Map<String,int[][]> map = A_DataGetter.getTimeFlow(ids,interval, folder+"/"+ dataFileName,null);
				
				if (map.size() == 1 && map.get("00") != null) {
					A_DataGetter.saveResult(resultBase+"/collection/"+dataFile.getName(), map.get("00"), interval);
				}else {
					for (String id : map.keySet()){
						A_DataGetter.saveResult(resultBase+"/collection/" + id + "_" + dataFileName, map.get(id), interval);
					}
				}
											
			}
		}
		
	}
	
	public static void getTimeFlowByPType(String folder,List<String> ids,String PType){
		
		File foldFile = new File(folder);	
		File[] datas = foldFile.listFiles();
		for(File file : datas){
			if(file.isFile() && file.getName().contains(".txt")){
				
				int inter = 300;
								
				Map<String,int[][]> map = A_DataGetter.getTimeFlow(null,inter, folder+"/"+file.getName(),"1");
				System.out.println(file.getName()+" ## "+ map.get("00"));
											
			}
		}
		
	}

	public static void getSum(String folder) throws IOException{
		
		File foldFile = new File(folder);	
		File[] datas = foldFile.listFiles();
		for(File file : datas){
			if(file.isFile() && file.getName().contains(".txt")){
				LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
				lnr.skip(file.length());
				int lnum = lnr.getLineNumber();
				System.out.print(lnum+",");
			}
		}
		
	}
	
	public static List<String> getIds(String filename){
		List<String> list = null;
		Set<String> idSet = new HashSet<String>();

		FileReader fr = null;
		try {
			fr = new FileReader(filename);
		} catch (FileNotFoundException e) {
			System.out.println("�Ҳ�������Դ�ļ�"+filename);
			System.exit(1);
		}
		
		Scanner sc = new Scanner(fr);
		while(sc.hasNext()){
			String temp = sc.nextLine();
			idSet.add(temp.trim());
		
		}
		list = new ArrayList<String>(idSet);
		System.out.println("totally "+list.size()+" ids");
		
		return list;
	}
	
	public static List<String> getIdOr(String filename){
		List<String> list = null;
		Set<String> idSet = new HashSet<String>();

		FileReader fr = null;
		try {
			fr = new FileReader(filename);
		} catch (FileNotFoundException e) {
			System.out.println("�Ҳ�������Դ�ļ�"+filename);
			System.exit(1);
		}
		
		Scanner sc = new Scanner(fr);
		while(sc.hasNext()){
			String temp = sc.nextLine();
			idSet.add(temp.trim());
		
		}
		list = new ArrayList<String>(idSet);
		System.out.println("totally "+list.size()+" ids");
		
		return list;
	}
	
	public static  Map<String, Integer>  maxFlowOneDay(List<String> ids,String folderpath,String month,String day){
		Map<String, Integer>  idMax = new HashMap<String, Integer>();
		for(String id : ids){
			
			String max = A_DataGetter.maxFlowFromFile(folderpath+"/"+id+"_2015-"+month+"-"+day+".txt");
			System.out.println("id:"+ id+"//max:"+max);
			if (max != null) {
				idMax.put(id, Integer.parseInt(max.trim()));
			}
		}
		
		
		System.out.println(idMax.toString());
		return idMax;
	}
	
	public static String maxFlowFromFile(String  path){
		int max = 0;
		String t = null;
		FileReader fr = null;
		try {
			fr = new FileReader(path);
		} catch (FileNotFoundException e) {
			System.out.println("can't find:"+path);
			return null;
		}
		
		Scanner sc = new Scanner(fr);
		while(sc.hasNext()){
			String[] temp = sc.nextLine().trim().split(",");
			int i = Integer.parseInt(temp[1]);
			
			if(i > max){
				max = i;
				t = temp[0];
			}
		}
		sc.close();
		try {
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return t+"/"+ max;
		return max+"";
	}

	public static void main(String[] args) throws IOException {
		
		List<String> ids = null;
		//ids = DataGetter.getIds("data/ids.txt");
		
		A_DataGetter.getTimeFlow("data/rawData/vpr-June",ids,defaultInterval);
		
	}

}
