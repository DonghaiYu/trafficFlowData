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
 * @description traffic Flow prediction Experiment Step 2
 * @input traffic flow data for per ID and per day(folder collection)
 * @output  traffic flow data for per ID and all day(folder byids)
 *
 */
public class b_Collection {
	
	public static String inputPath = "result/collection";
	public static String savePath = "result/byids/";

	public static void main(String[] args) {
		
		File folder = new File(inputPath);
		File[] files = folder.listFiles();
		Map<String, String> collMap = new HashMap<String, String>();
		
		for (int i = 0; i < files.length; i++) {
			String id = files[i].getName().split("_2015-")[0];
			String day = files[i].getName().split("_2015-")[1].replaceAll(".txt", ""); 
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
					strb.append(day+",");
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
