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
 * @description traffic Flow prediction Experiment Data preparation Part Step 4(use for jar file on server machine during training)
 * @input cleaned traffic flow data for per ID of all day(folder cleaned_byids)
 * @output  SAE input data group by IDs(execute before every SAE training)
 *
 */


public class PrepareData4SAE {
	
	public static String USAGE = "java -jar dataPreparation.jar [filePath] [saveFolder] [groupList] [targetLength] [startIndex] [endIndex] [recordSize] [discretizationBlockSize] [groupSize]";
	
	/*交通流量源数据文件夹，其中各个文件名为卡口名*/
	
	String filePath;
	
	/*重组后的数据存储位置*/
	
	String saveBase;
	
	/*要组合数据的卡口列表，逗号隔开*/
	
	String groupList;
	
	/*每条记录中目标值(预测值）个数*/
	
	int targetLength;
	
	/*每天流量数据的截取开始位置，（之前的数据不用）；默认3:00*/
	
	int startIndex;
	
	/*每天流量数据的截取结束位置，（之后的数据不用）; 默认21:00*/
	
	int endIndex;
	
	/*每条重组数据中每个卡口数据个数*/
	int recordSize;
	
	/*离散数据时的块大小*/
	
	int discretizationBlockSize;
	
	/*需要组合数据的卡口个数*/
	int groupSize;
	
	/**
	 * 构造方法初始化参数
	 */
	public PrepareData4SAE() {
		
		this.filePath = "result/cleaned_byids/";
		this.saveBase = "result/ANNinput/";
		this.groupList = "371300403101_01,371300403102_00";
		this.targetLength = 1;
		this.startIndex = 3 * 60 / 12;
		this.endIndex = 3 * 60 / 12;
		this.recordSize = 20;
		this.discretizationBlockSize = 4;
		this.groupSize = 3;
	}
	
	/**
	 * 运行入口
	 * @param args，外部参数传入
	 */
	public void excute(String[] args) {		
		
		boolean debug = false;
		//参数传入
		if (!debug) {
			if (args.length == 8) {
				filePath = args[0];
				saveBase = args[1];
				groupList = args[2];
				targetLength = Integer.parseInt(args[3]);
				startIndex = Integer.parseInt(args[4]);
				endIndex = Integer.parseInt(args[5]);
				recordSize = Integer.parseInt(args[6]);
				discretizationBlockSize = Integer.parseInt(args[7]);
				groupSize = Integer.parseInt(args[8]);
			}else {
				System.out.println("parameters wrong! usage:");
				System.out.println(USAGE);
				return;
			}
		}
		
		
		//验证分组中卡口是否有有效数据
		List<String> idGroups = getIdGroup(groupList, filePath, groupSize, ",");
		if (idGroups == null) {
			System.out.println("Error! Can't get useful data!");
			return;
		}		
		
		//构造神经网络训练数据
		String savePath = saveBase + "SAE.data";
	
		Map<String,Map<String, int[]>> allBays_allDays = getAllBayAlldays(filePath, idGroups);
		List<List<int [][]>> toSave = getSaveMatrix(allBays_allDays, groupList, startIndex, endIndex, recordSize, targetLength);		

		try {
			saveGroup(savePath, toSave);
		} catch (IOException e) {
			System.out.println("Error! failed to save the rebuild data!!");
		}
			
	}
	
	/**
	 * 按每天各个卡口数据重组数据为矩阵
	 * @param allBays_allDays
	 * @param group
	 * @param startIndex
	 * @param endIndex
	 * @param intervalNum
	 * @param outN
	 * @return <<每个卡口每天的数据切割后的矩阵*（卡口数）>*（天数）>
	 */
	public  List<List<int [][]>> getSaveMatrix(Map<String,Map<String, int[]>> allBays_allDays, String group, int startIndex, int endIndex, int intervalNum, int outN){
		
		List<List<int [][]>> toSave = new ArrayList<List<int[][]>>();//
		List<String> saveDays = new ArrayList<String>();//
		
		//找出指定的卡口共同有数据的日期
		List<Set<String>> allBays_dateList = new ArrayList<Set<String>>();
		for (String id : allBays_allDays.keySet()) {
			Set<String> oneBay_DateList = allBays_allDays.get(id).keySet();
			allBays_dateList.add(oneBay_DateList);
		}
		for (int i = 1; i < allBays_dateList.size(); i++) {
			allBays_dateList.get(0).retainAll(allBays_dateList.get(i));
		}
		
		List<String> usefuleDates = new ArrayList<String>(allBays_dateList.get(0)); 
		Collections.sort(usefuleDates);
		
		
		for (String day : usefuleDates) {
			List<int[][]> allBays_oneDay = new ArrayList<int[][]>();
			for (String bayID: allBays_allDays.keySet()) {
				Map<String, int[]> oneBay_allDays = allBays_allDays.get(bayID);
				if (oneBay_allDays.containsKey(day)) {
					int[][] oneBay_oneDay = cutVec(oneBay_allDays.get(day), startIndex, endIndex, intervalNum, outN);
					allBays_oneDay.add(oneBay_oneDay);					
				}else {
					allBays_oneDay.clear();
					break;
				}				
			}
			
			if (allBays_oneDay.size() != 0) {
				saveDays.add(day);
				toSave.add(allBays_oneDay);
			}
		}
		System.out.println("group:" + group);
		System.out.println("days:"+ saveDays.toString());
		return toSave;
	}
	
	/**
	 * 获取指定的所有卡口的所有天的数据
	 * @param filePath 源文件所在文件夹路径
	 * @param forids 指定卡口列表
	 * @return <卡口ID，<日期，数据数组>>
	 */
	public static Map<String,Map<String, int[]>> getAllBayAlldays(String filePath,List<String> forids){

		Map<String,Map<String, int[]>> allDayAllBays = new HashMap<String, Map<String,int[]>>();
		for (String id : forids) { 
			Map<String, int[]> dayMap = null;
			try {
				dayMap = getVec(filePath + id, ",");
				allDayAllBays.put(id, dayMap);
			} catch (FileNotFoundException e) {
				System.out.println("file:"+ filePath + id + " is not found!");
			}			
		}
		return allDayAllBays;
	}
	
	/**
	 * 查找指定数量的有有效数据的卡口列表，第一个卡口数据无法找到时返回null，则此次数据重组失败。
	 * @param group 某卡口与其相邻卡口组成的列表
	 * @param filePath 数据存储文件夹
	 * @param size 本组数据中包含的卡口数量
	 * @param seperater 分割卡口列表参数的正则表达式
	 * @return
	 */
	public List<String> getIdGroup(String group, String filePath, int size, String seperater){
		
		String[] allIds = group.split(seperater);
		List<String> ids = findFiles(allIds, filePath);
		
		if (ids == null) {
			System.out.println("Error! Can't find the first bay data!");
			return null;
		}else {
			if (ids.size() < size) {
				System.out.println("Warning! There are less than " + size + " neighbors!");
			}else {
				while (ids.size() > size) { 
					ids.remove(size);
				}
			}			
		}		
		return ids;
	}
	
	/**
	 * 从文件夹中查找文件,若第一个文件找不到则返回 null
	 * @param fileNames 文件名称列表
	 * @param folder 要搜索的文件夹路径
	 * @return 找到的文件名称列表
	 */
	public static List<String> findFiles(String[] fileNames,String folder) {
		
		File f = new File(folder);
		File[] fs = f.listFiles();
		
		Set<String> allfiles = new HashSet<String>();
		for (int i = 0; i < fs.length; i++) {
			allfiles.add(fs[i].getName());
		}

		List<String> gottenFiles = new ArrayList<String>();
		for (int i = 0; i < fileNames.length; i++) {
			if(i == 0 && !allfiles.contains(fileNames[0])){
				System.out.println("Error! can't find the first file!");
				return null;
			}
			if (allfiles.contains(fileNames[i])) {
				gottenFiles.add(fileNames[i]);
			}			
		}
		
		return gottenFiles;		
	}
	
	/**
	 * 读取指定源数据文件的流量数据，以天为单位
	 * @param filename 
	 * @return <日期(天）， 流量数据数组>
	 * @throws FileNotFoundException
	 */
	public static Map<String, int[]> getVec(String filename, String seperater) throws FileNotFoundException{
		
		File file = new File(filename);
		Scanner sc = new Scanner(file);
		
		Map<String, int[]> dayDataMap = new HashMap<String, int[]>();
		
		while (sc.hasNext()) {
			String temp = sc.nextLine();
			String[] splits = temp.split(seperater);
			String date = splits[0];
			int[] lineNum = new int[splits.length-1];
			for (int i = 1; i < splits.length; i++) {
				lineNum[i-1] = Integer.parseInt(splits[i]);
			}
			dayDataMap.put(date, lineNum);
		}
		sc.close();
		return dayDataMap;
	}
	
		
	/**
	 * 切割一天的原始数据，将向量切割重组为矩阵
	 * @param vector 一天的流量数据
	 * @param start 切割开始位置，即此向量中舍弃开头多少个数据
	 * @param end  切割结束位置，即此向量中舍弃从后数多少个数据
	 * @param interNum  切割窗口大小
	 * @param outN  目标值取切割窗口之后几个数的和
	 * @return 按时间片段组成的流量记录矩阵
	 */
	public int[][] cutVec(int[] vector, int start, int end, int interNum, int outN) {
		
		int length = vector.length;
		if ((length - start - end) <= 0) {
			System.out.println("Wrong! wrong parameters when cutting vector");
			return null;
		}
		int vecNum = length - start - interNum - end - outN + 1;
	
		int[][] beans = new int[vecNum][interNum + 1];
		for (int i = 0; i < vecNum; i++) {
			for (int j = 0; j < interNum; j++) {
				beans[i][j] = vector[i + start + j];
			}
			int outSum = 0;
			for (int k = interNum; k < (interNum + outN); k++) {
				outSum += vector[i+start+k];
			}
			beans[i][interNum] = outSum;			
		}
		
		return beans;
	}
	
	/**
	 * 将重组的矩阵存储
	 * @param fileName 存储文件路径
	 * @param toSave 需要存储的矩阵
	 * @throws IOException
	 */
	public void saveGroup(String fileName, List<List<int [][]>> toSave) throws IOException {
		
		File saveFile = new File(fileName);
		FileWriter fw = new FileWriter(saveFile);
		
		for (int i = 0; i < toSave.size(); i++) {
			List<int[][]> oneDayAllBays = toSave.get(i);
			int bayNum = oneDayAllBays.size();
			int vecLength = oneDayAllBays.get(0)[0].length;
			int vecNum = oneDayAllBays.get(0).length;
			
			for (int j = 0; j < vecNum; j++) {
				for (int k = 0; k < bayNum; k++) {
					for (int w = 0; w < vecLength - 1; w++) {
						if (k == 0 && w == 0) {
							fw.write(oneDayAllBays.get(k)[j][w] + "");
						}else {
							fw.write(","+oneDayAllBays.get(k)[j][w]);
						}
					}
					
				}

				fw.write(","+oneDayAllBays.get(0)[j][vecLength-1]+"\n");				
			}
		}
		
		fw.flush();
		fw.close();
	}
	
	public static void main(String[] args) {		
		
		PrepareData4SAE dataPre = new PrepareData4SAE();
		dataPre.excute(args);
		/*int[] a = {1,2,3,4,5,6,7,8,9,10};
		int[][] b = dataPre.cutVec(a,1, 2, 3, 2);
		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < b[0].length; j++) {
				System.out.print(b[i][j] + " ");
			}
			System.out.println("\n");
		}*/
	}
	
}
