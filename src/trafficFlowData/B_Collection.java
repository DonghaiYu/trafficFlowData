package trafficFlowData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 
 * @author DonghaiYu
 * @description traffic Flow prediction Experiment Data preparation Part Step 2 (Collection traffic flow data by ID)
 * @input traffic flow data for per ID of per day(folder collection)
 * @output  traffic flow data for per ID of all day(folder byids)
 *
 */
public class B_Collection {
	
	public static String inputPath = "result/collection";
	public static String savePath = "result/byids/";

	public static void main(String[] args) {
		
		File folder = new File(inputPath);
		File[] files = folder.listFiles();
		Map<String, String> collMap = new HashMap<String, String>();
		
		for (int i = 0; i < files.length; i++) {
			String fName = files[i].getName();
			String[] id_day = null;
			String year = null;
			if (fName.contains("_2015-")) {
				id_day = fName.split("_2015-");
				year = "2015-";
			}else if (fName.contains("_2016-")) {
				id_day = fName.split("_2016-");
				year = "2016-";
			}
			String id = id_day[0];
			String date = year+id_day[1].replaceAll(".txt", ""); 
			
			Scanner sc = null;
			try {
				sc = new Scanner(files[i]);
			} catch (FileNotFoundException e) {
				System.out.println("can't scan file:" + files[i].getName());
				System.exit(1);
			}
			StringBuilder strb = new StringBuilder();
			int num = 0;
			while (sc.hasNext()) {
				String temp = (String) sc.next();
				if (num == 0) {
					strb.append(date+",");
					strb.append(temp.split(",")[1]);
				}else {
					strb.append(",");
					strb.append(temp.split(",")[1]);
				}				
				num++;
			}
			strb.append("\n");
			sc.close();
			if (collMap.containsKey(id)) {
				String old = collMap.get(id);
				strb.insert(0, old);				
			}		
			collMap.put(id, strb.toString());
		}

		
		for (String id : collMap.keySet()) {
			File f = new File(savePath+id);
			FileWriter fw;
			try {
				fw = new FileWriter(f);
				fw.write(collMap.get(id));
				fw.flush();
				fw.close();
			} catch (IOException e) {
				System.out.println("can't write file!");
			}
						
		}
	}

}
