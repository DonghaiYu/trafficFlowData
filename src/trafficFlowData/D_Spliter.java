package trafficFlowData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * 
 * @author DonghaiYu
 * @description traffic Flow prediction Experiment Data preparation Part Step 4(use for jar file on server machine during training)
 * @input cleaned traffic flow data for per ID of all day(folder cleaned_byids)
 * @output  ANN input data group by IDs(execute before every ANN training)
 *
 */


public class D_Spliter {
	public static String USAGE = "java -jar spliter.jar [filePath] [saveFolder] [ids] [targetLength] [cutStartIndex] [cutEndIndex] [totalVectorLength] [rankDistance] [groupsize]";
	
	
	public static void excute(String[] args){
		String  filePath = "result/cleaned_byids/";
		String saveBase = "result/ANNinput/";
		String group = "371300403101_01";
		int outN = 1;
		int startIndex = 1;
		//4 * 60 / 12;
		int endIndex = 3 * 60 / 12;
		int intervalNum = 31;
		int rankDistance = 5;
		int gsize = 3;
		if (args.length > 8) {
			filePath = args[0];
			saveBase = args[1];
			group = args[2];
			outN = Integer.parseInt(args[3]);
			startIndex = Integer.parseInt(args[4]);
			endIndex = Integer.parseInt(args[5]);
			intervalNum = Integer.parseInt(args[6]);
			rankDistance = Integer.parseInt(args[7]);
			gsize = Integer.parseInt(args[8]);
		}else {
			System.out.println("parameters not enough");
			System.out.println(USAGE);
			return;
		}
		
		Map<String, String[]> idGroups = getIdGroup(group, filePath, saveBase,gsize);
		//<saveName,IDs>
		
		if (idGroups == null) {
			return;
		}
		
		for (String saveName : idGroups.keySet()) {
			
			String savePath = saveName;
			String[] forids = idGroups.get(saveName);			
			
			Map<String,Map<String, int[]>> allDayAllBays = getAllBayAlldays(filePath, forids);
			Map<String, int[]> oneBayAlldaysMean = getAllBayMeans(allDayAllBays);
			Map<String, int[]> oneBayAlldaysMode = getAllBayModes(allDayAllBays,rankDistance);
			List<List<int [][]>> toSave = getSaveMatrix(allDayAllBays,oneBayAlldaysMean,oneBayAlldaysMode,forids,group, startIndex, endIndex, intervalNum, outN);
			
	
			try {
				saveGroup(savePath, toSave);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
	public static void main(String[] args) {		
		/*String[] argString = {"result/cleaned_byids/","result/ANNinput/","371302981098_03","1","20","20","21","5"};
		File fold = new File("result/cleaned_byids/");
		File[] ls = fold.listFiles();
		for (int i = 0; i < ls.length; i++) {
			String name = ls[i].getName();
			argString[2] = name;
			excute(argString);
			System.out.println(i+"/"+ls.length);
		}*/
		excute(args);
	}
	
	public static Map<String, int[]> getAllBayModes(Map<String,Map<String, int[]>> allDayAllBays,int stage) {
		Map<String, int[]> allBayMode = new HashMap<String, int[]>();
		for (String id : allDayAllBays.keySet()) {
			Map<String, int[]> oneBayAllDay = allDayAllBays.get(id);
			int perDay = oneBayAllDay.get(oneBayAllDay.keySet().toArray()[0].toString()).length;
			int dayNum = oneBayAllDay.keySet().size();
			int[][] matrix = new int[dayNum][perDay];
			int dn = 0;
			int[] mode = new int[perDay];
			for (String day : oneBayAllDay.keySet()) {
				int[] temp = oneBayAllDay.get(day);
				for (int i = 0; i < temp.length; i++) {
					matrix[dn][i] = temp[i]/stage * stage;
				}
				dn ++;
			}
			for (int i = 0; i < perDay; i++) {
				Map<Integer,Integer> cal = new HashMap<Integer,Integer>();
				for (int j = 0; j < dayNum; j++) {
					if (cal.get(matrix[j][i]) != null) {
						int old = matrix[j][i];
						int ne = old + 1;
						cal.put(matrix[j][i], ne);
					}else {
						cal.put(matrix[j][i], 1);
					}
				}
				int m = 0;
				int max = 0;
				for (int key : cal.keySet()) {
					int value = cal.get(key);
					if (value > max) {
						m = key;
						max = value;
					}
				}
				mode[i] = m;
			}
			allBayMode.put(id, mode);
		}
		return allBayMode;
	}
	
	public static Map<String, int[]> getAllBayMeans(Map<String,Map<String, int[]>> allDayAllBays) {
		Map<String, int[]> oneBayAlldaysMean = new HashMap<String, int[]>();
		for (String id : allDayAllBays.keySet()) {
			Map<String, int[]> oneBayAllDay = allDayAllBays.get(id);
			int[] temp = new int[oneBayAllDay.get(oneBayAllDay.keySet().toArray()[0].toString()).length];
			for (String day : oneBayAllDay.keySet()) {
				int[] oneBayoneDay = oneBayAllDay.get(day);

				for (int i = 0; i < oneBayoneDay.length; i++) {
					temp[i] += oneBayoneDay[i];
				}
			}
			int n = oneBayAllDay.size();
			for (int i = 0; i < temp.length; i++) {
				temp[i] /= n;
			}
			oneBayAlldaysMean.put(id, temp);
		}
		return oneBayAlldaysMean;
	}
	
	public static int[][] meanMatrix(int[][] a,int[][] b,int[][] c) {
		int x = a.length;
		int y = a[0].length;
		int[][] result = new int[x][y];
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				result[i][j] = (a[i][j]+b[i][j]+c[i][j])/3;
			}
		}
		return result;
	}
	
	public static List<List<int [][]>> getSaveMatrix(Map<String,Map<String, int[]>> allDayAllBays,Map<String, int[]> oneBayAlldaysMean,Map<String, int[]> oneBayAlldaysMode,String[] ids,String group,int startIndex,int endIndex,int intervalNum,int outN){
		List<List<int [][]>> toSave = new ArrayList<List<int[][]>>();
		List<String> saveDays = new ArrayList<String>();
		List<Set<String>> d = new ArrayList<Set<String>>();
		for (String id : allDayAllBays.keySet()) {
			Set<String> x = allDayAllBays.get(id).keySet();
			d.add(x);
		}
		for (int i = 1; i < d.size(); i++) {
			d.get(0).retainAll(d.get(i));//取交集
		}
		List<String> dd = new ArrayList<String>(d.get(0)); 
		Collections.sort(dd);
		//System.out.println(dd.toString());
		for (String day : dd) {
			List<int[][]> oneDayAllBays = new ArrayList<int[][]>();
			for (String id: ids) {
				Map<String, int[]> oneBayAlldays = allDayAllBays.get(id);
				if (oneBayAlldays.containsKey(day)) {
					int[][] oneDayOneBay = cutVec(oneBayAlldays.get(day), startIndex, endIndex,intervalNum,outN);
					oneDayAllBays.add(oneDayOneBay);

					//*********************
					int[][] oneBayMeans = cutVec(oneBayAlldaysMean.get(id), startIndex, endIndex,intervalNum,outN);
					oneDayAllBays.add(oneBayMeans);
					
					int[][] oneBayModes = cutVec(oneBayAlldaysMode.get(id), startIndex, endIndex,intervalNum,outN);
					oneDayAllBays.add(oneBayModes);
					/*int[][] mm = meanMatrix(oneDayOneBay, oneBayMeans, oneBayModes);
					for (int i = 0; i < 3; i++) {
						oneDayAllBays.add(oneDayOneBay);
					}*/
					//oneDayAllBays.add(mm);
				}else {
					oneDayAllBays.clear();
					break;
				}
				
			}
			if (oneDayAllBays.size() != 0) {
				saveDays.add(day);
				toSave.add(oneDayAllBays);
			}
		}
		System.out.println("group:"+group);
		System.out.println("days:"+ saveDays.toString());
		return toSave;
	}
	
	public static Map<String,Map<String, int[]>> getAllBayAlldays(String filePath,String[] forids){
		Map<String,Map<String, int[]>> allDayAllBays = new HashMap<String, Map<String,int[]>>();
		//<date,vector>for one bay
		
		for (int i = 0; i < forids.length; i++) { 
			Map<String, int[]> dayMap = null;
			try {
				dayMap = getVec(filePath+forids[i]);
				allDayAllBays.put(forids[i], dayMap);
			} catch (FileNotFoundException e) {
				System.out.println("file:"+ filePath+forids[i] +" is not found!");
			}
			
		}
		return allDayAllBays;
	}
	
	public static Map<String, String[]> getIdGroup(String group,String filePath,String saveBase,int size){
		//delete the IDs that don't have data file
		String[] allIds = group.split(",");
		List<String> ids = findFiles(allIds, filePath);
		
		//*******************
		if (ids == null || ids.size()<size) {
			return null;
		}
		while (ids.size() > size) { 
			ids.remove(size);
		}
		
		Map<String, String[]> idGroups = new HashMap<String, String[]>();
		
		String[] idsarr = new String[ids.size()];
		ids.toArray(idsarr);
		String name = "";
		for (int i = 0; i < idsarr.length; i++) {
			if (i == 0) {
				name = idsarr[i];
			}else {
				name = name +"-"+ idsarr[i];
			}
			
		}
		
		idGroups.put(saveBase+"g"+name, idsarr);
		return idGroups;
	}
	
	public static List<String> findFiles(String[] ids,String folder) {
		File f = new File(folder);
		File[] fs = f.listFiles();
		Set<String> allids = new HashSet<String>();
		for (int i = 0; i < fs.length; i++) {
			allids.add(fs[i].getName());
		}

		List<String> getids = new ArrayList<String>();
		for (int i = 0; i < ids.length; i++) {
			if(i == 0 && !allids.contains(ids[i])){
				return null;
			}
			if (allids.contains(ids[i])) {
				getids.add(ids[i]);
			}
			
		}
		
		return getids;
		
	}
	public static Map<String, int[]> getVec(String filename) throws FileNotFoundException{
		File file = new File(filename);
		Scanner sc = new Scanner(file);
		
		Map<String, int[]> dayDataMap = new HashMap<String, int[]>();
		
		while (sc.hasNext()) {
			String temp = sc.nextLine();
			String[] splits = temp.split(",");
			String id = splits[0];
			int[] lineNum = new  int[splits.length-1];
			for (int i = 1; i < splits.length; i++) {
				lineNum[i-1] = Integer.parseInt(splits[i]);
			}
			dayDataMap.put(id, lineNum);
		}
		sc.close();
		return dayDataMap;
	}
	
	public static int[][] getMatrix(String filename) throws FileNotFoundException{
		File file = new File(filename);
		Scanner sc = new Scanner(file);
		Scanner sc2 = new Scanner(file);
		int lNum = 0;
		while (sc2.hasNext()) {
			sc2.nextLine();
			lNum++;
			
		}
		sc2.close();
		
		
		int index = 0;
		int[][] matrix = new int[lNum][];
		
		while (sc.hasNext() && index <= 30) {
			String temp = sc.nextLine();
			System.out.println(temp);
			String[] splits = temp.split(",");
			int[] lineNum = new  int[splits.length];
			for (int i = 0; i < splits.length; i++) {
				lineNum[i] = Integer.parseInt(splits[i]);
			}
			matrix[index] = lineNum;
			index++;
		}
		sc.close();
		return matrix;
	}
	
	public static int[][] cutVec(int[] vector,int start,int end,int interNum,int outN) {
		int length = vector.length;
		int vecNum = length - start - interNum -end + 2;
		int inputN = interNum - outN; 
		int[][] beans = new int[vecNum][inputN+1];
		for (int i = 0; i < vecNum; i++) {
			for (int j = 0; j < inputN; j++) {
				beans[i][j] = vector[i+start+j-1];
			}
			int outSum = 0;
			for (int k = inputN; k < interNum; k++) {
				outSum += vector[i+start+k-1];
			}
			beans[i][inputN] = outSum;
			
		}
		
		return beans;
	}
	
	public static int[][] cut(int[][] matrix,int start,int interNum) {
		int numPerLine = matrix[0].length - start - interNum + 2;
		int x = matrix.length * numPerLine;
		int[][] beans = new int[x][interNum];
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < interNum; j++) {
				beans[i][j] = matrix[i/numPerLine][i%numPerLine+start+j-1];
			}
		}
		return beans;
	}
	
	public static void save(String fileName,int[][] data) throws IOException {
		File saveFile = new File(fileName);
		FileWriter fw = new FileWriter(saveFile);
		
		int num = data[0].length;
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < num; j++) {
				if (j == 0) {
					fw.write(data[i][j]+"");
				}else {
					fw.write(","+data[i][j]);
				}
				
			}
			fw.write("\n");
		}
		fw.flush();
		fw.close();
	}

	public static void saveGroup(String fileName,List<List<int [][]>> toSave) throws IOException {
		File saveFile = new File(fileName);
		FileWriter fw = new FileWriter(saveFile);
		
		//System.out.println("days:"+toSave.size());
		for (int i = 0; i < toSave.size(); i++) {
			List<int[][]> oneDayAllBays = toSave.get(i);
			int num = oneDayAllBays.size();
			int x = oneDayAllBays.get(0)[0].length;
			int y = oneDayAllBays.get(0).length;
			for (int i2 = 0; i2 < y; i2++) {
				for (int j = 0; j < num; j++) {
					for (int k = 0; k < x - 1; k++) {
						if (j == 0 && k == 0) {
							fw.write(oneDayAllBays.get(j)[i2][k] + "");
						}else {
							fw.write(","+oneDayAllBays.get(j)[i2][k]);
						}
					}
					
				}
				/*int xx = 0;
				for (int j = 0; j < num/3; j++) {
					xx += oneDayAllBays.get(j*3)[i2][x-1];
					//fw.write(","+oneDayAllBays.get(j*3)[i2][x-1]);
				}
				xx/=3;
				fw.write(","+xx+"\n");*/
				fw.write(","+oneDayAllBays.get(0)[i2][x-1]+"\n");
				
			}
		}
		
		fw.flush();
		fw.close();
	}
	
	
	
}
