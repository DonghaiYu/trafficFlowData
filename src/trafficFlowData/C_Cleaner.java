package trafficFlowData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 
 * @description 如果一个卡口一天各时段的过车量中超过threshold个记录为0，则认为是无效数据，删除。
 *				如果一个卡口有记录的天数少于minLineNum则认为无效，删除。
 */

public class C_Cleaner {
	
	public static int sum(String line) {
		int sum = 0;
		String[] x = line.split(",");
		for (int i = 0; i < x.length; i++) {
			sum += Integer.parseInt(x[i]);
		}
		return sum;
	}
	
	public static int deadTimes(String line) {
		int sum = 0;
		String[] x = line.split(",");
		
		for (int i = 0; i < x.length; i++) {
			if (Integer.parseInt(x[i]) == 0) {
				sum ++;
			}
			/*if (Integer.parseInt(x[i]) > 500) {
				x[i] = "500";
			}*/
			
		}
		
		return sum;
	}
	
	public static void deleteLine() throws IOException {
		int threshold = 30;
		
		int deleteNum = 0;
		String path = "data/00_000";
		String savePath = "data/clean/";
		File file = new File(path);
	
		String name = file.getName();
		Scanner sc = new Scanner(file);
		FileWriter fw = new FileWriter(savePath+name);
		while (sc.hasNext()) {
			String temp = sc.nextLine();
			if (deadTimes(temp) < threshold) {
				fw.write(temp+"\n");
				
			}else {
				deleteNum++;
			}
		}
		fw.flush();
		fw.close();
		sc.close();
			
			
		
		System.out.println("deleted "+ deleteNum + " lines");
	}
	
	public static void deleteFile() throws IOException {
		int minLineNum = 3;		
		int deleteNum = 0;
		String path = "data/vpr-June/collection/clean";
		File folder = new File(path);
		File[] files = folder.listFiles();
		
		//统计每个卡口有数据的天数情况，以10天为间隔
		Map<Integer, Integer> daysMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < files.length; i++) {
			int lineNum = 0;
			BufferedReader br = new BufferedReader(new FileReader(files[i]));
			while (br.readLine()!=null){
				lineNum++;
			}
			br.close();
			if (daysMap.containsKey(lineNum/10)) {
				int old = daysMap.get(lineNum/10);
				int fresh = old+1;
				daysMap.put(lineNum/10, fresh);
			}else {
				daysMap.put(lineNum/10, 1);
			}
			if (lineNum < minLineNum) {
				System.out.println(files[i].delete());
				deleteNum++;
			}
		}
		System.out.println("deleted "+ deleteNum + " files");
		System.out.println(daysMap.toString());
	}

	public static void main(String[] args) throws IOException {
		
		deleteLine();
		//deleteFile();
		
	}

}
