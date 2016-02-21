package trafficFlowData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
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
	
	public static String USAGE = "java -jar spliter.jar [filePath] [saveFolder] [days] [ids] [targetLength] [cutStartIndex] [cutEndIndex] [totalVectorLength]";
	
	public static void main(String[] args) {		
		
		String  filePath = "result/cleaned_byids/";
		String saveBase = "result/ANNinput/";
		String allday = "06-01,06-02,06-04,06-10,06-15,06-16,06-17,06-18,06-22,06-24,06-25,06-26,06-29,06-30";
		String group = "1_00,371302989030_03,371302981075_01,2_01";
		int outN = 1;
		int startIndex = 12 * 5;//12代表一个小时
		int endIndex = 12 * 3;
		int intervalNum = 20;		
		
		if (args.length > 7) {
			filePath = args[0];
			saveBase = args[1];
			allday = args[2];
			group = args[3];
			outN = Integer.parseInt(args[4]);
			startIndex = Integer.parseInt(args[5]);
			endIndex = Integer.parseInt(args[6]);
			intervalNum = Integer.parseInt(args[7]);
		}else {
			System.out.println("parameters not enough");
			System.out.println(USAGE);
			return;
		}
		
		String[] dayss = allday.split(",");
		List<String> days = java.util.Arrays.asList(dayss);
		
		//delete the IDs that don't have data file
		String[] allIds = group.split(",");
		Set<String> ids = findFiles(allIds, filePath);
		
		Map<String, String[]> idGroups = new HashMap<String, String[]>();
		
		for (String id : ids) {
			String[] bayId = {id};
			idGroups.put(saveBase+id,bayId);
		}
		String[] idsArr = new String[ids.size()];
		ids.toArray(idsArr);
		idGroups.put(saveBase+"group", idsArr);		
		
		for (String saveName : idGroups.keySet()) {
			
			String savePath = saveName;
			String[] forids = idGroups.get(saveName);			
			
			List<Map<String, int[]>> allDayAllBays = new ArrayList<Map<String,int[]>>();
			//<date,vector>for one bay
			
			for (int i = 0; i < forids.length; i++) {
				Map<String, int[]> dayMap = null;
				try {
					dayMap = getVec(filePath+forids[i]);
					allDayAllBays.add(dayMap);
				} catch (FileNotFoundException e) {
					System.out.println("file:"+ saveName +" is not found!");
				}
				
			}
			
			List<List<int [][]>> toSave = new ArrayList<List<int[][]>>();
			List<Integer> saveDays = new ArrayList<Integer>();
			for (int i = 1; i < 31; i++) {				

				String day = String.format("06-%02d",i );
				if (days.contains(day)) {
					List<int[][]> oneDayAllBays = new ArrayList<int[][]>();
					for (Map<String, int[]> oneBayAlldays: allDayAllBays) {
						if (oneBayAlldays.containsKey(day)) {
							int[][] oneDayOneBay = cutVec(oneBayAlldays.get(day), startIndex, endIndex,intervalNum,outN);
							oneDayAllBays.add(oneDayOneBay);
						}else {
							oneDayAllBays.clear();
							System.out.println(saveName + " lack the vector of date:" + day);
							break;
						}
						
					}
					if (oneDayAllBays.size() != 0) {
						saveDays.add(i);
						toSave.add(oneDayAllBays);
					}	
				}				
			}
			System.out.println("for group:"+Arrays.toString(forids)+":days:"+ saveDays.toString());

			try {
				saveGroup(savePath, toSave);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	public static Set<String> findFiles(String[] ids,String folder) {
		File f = new File(folder);
		File[] fs = f.listFiles();
		Set<String> allids = new HashSet<String>();
		for (int i = 0; i < fs.length; i++) {
			allids.add(fs[i].getName());
		}

		Set<String> getids = new HashSet<String>();
		for (int i = 0; i < ids.length; i++) {
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
				for (int j = 0; j < num; j++) {
					fw.write(","+oneDayAllBays.get(j)[i2][x-1]);
				}
				fw.write("\n");
			}
		}
		
		fw.flush();
		fw.close();
	}
	
	
	

}
