package trafficFlowData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
 * @description traffic Flow prediction Experiment Data preparation Part Step 4(use for jar file)
 * @input cleaned traffic flow data for per ID of all day(folder cleaned_byids)
 * @output  ANN input data group by IDs(execute before every ANN training)
 *
 */


public class D_Spliter {
	
	public static String USAGE = "java -jar spliter.jar [filePath] [saveFolder] [days] [ids] [targetLength] [cutStartIndex] [cutEndIndex] [totalVectorLength]";
	
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
			/*System.out.println(y+" y");
			System.out.println(x+" x");
			System.out.println(num+" num");*/
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
	
	
	public static void main(String[] args) {		
		
		String  filePath = "data/vpr-June/collection/byids/";		
		String saveBase = "data/a1/";
		String allday = "06-01,06-02,06-04,06-10,06-15,06-16,06-17,06-18,06-22,06-24,06-25,06-26,06-29,06-30";
		String group = "1_00,371302989030_03,371302981075_01,2_01";
		int outN = 1;
		int startIndex = 12 * 5;//12代表一个小时
		int endIndex = 12 * 3;
		int intervalNum = 21;		
		
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

		Map<String, String[]> idGroups = new HashMap<String, String[]>();
		
		String[] ids = group.split(",");
		idGroups.put(saveBase+"group", ids);
		for (int i = 0; i < ids.length; i++) {
			String[] bayId = {ids[i]};
			idGroups.put(saveBase+ids[i],bayId);
		}
		
		
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
			//System.out.println("idnum:"+allDayAllBays.size());
			List<List<int [][]>> toSave = new ArrayList<List<int[][]>>();
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
						toSave.add(oneDayAllBays);
					}
					
				}
				
			}
			
			try {
				saveGroup(savePath, toSave);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*int[][] matrix = getMatrix(filePath);
		int[][] cut = cut(matrix, startIndex, intervalNum);
		save(savePath, cut);*/
		/*Map<String, int[]> dayMap = getVec(filePath);
		System.out.println(dayMap.get("06-01").toString());
		int[][] c = cutVec(dayMap.get("06-01"), startIndex, intervalNum);*/

		/*int num = c[0].length;
		for (int i = 0; i < 3; i++) {
		
			for (int j = 0; j < num; j++) {
				System.out.print(c[i][j]+" ");
			}
			System.out.println("");
		}*/
		/*String[] id1 = {"371302989030_03"}; 
		String[] id2 = {"1_00"}; 
		String[] id3 = {"371302981075_01"}; 
		String[] id4 = {"2_01"}; */
		
		/*String[] ids2 = {"1_00","1_02","2_02","2_01"}; 
		String[] id21 = {"1_02"}; 
		String[] id22 = {"1_00"}; 
		String[] id23 = {"2_02"}; 
		String[] id24 = {"2_01"}; 
		
		String[] idx = {"56_00","1_00","35_00","8_01"}; 
		String[] idx1 = {"56_00"}; 
		String[] idx2 = {"1_00"}; 
		String[] idx3 = {"35_00"}; 
		String[] idx4 = {"8_01"}; */
		
		
		/*idGroups.put(saveBase+"01_g1", ids);
		idGroups.put(saveBase+"01_g11", id1);
		idGroups.put(saveBase+"01_g12", id2);
		idGroups.put(saveBase+"01_g13", id3);
		idGroups.put(saveBase+"01_g14", id4);*/
		
		/*idGroups.put(saveBase+"01_g2", ids2);
		idGroups.put(saveBase+"01_g21", id21);
		idGroups.put(saveBase+"01_g22", id22);
		idGroups.put(saveBase+"01_g23", id23);
		idGroups.put(saveBase+"01_g24", id24);
		
		idGroups.put(saveBase+"01_gx", idx);
		idGroups.put(saveBase+"01_gx1", idx1);
		idGroups.put(saveBase+"01_gx2", idx2);
		idGroups.put(saveBase+"01_gx3", idx3);
		idGroups.put(saveBase+"01_gx4", idx4);*/
		
	}

}
