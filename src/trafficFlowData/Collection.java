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
 * @author donghaiyu
 * @description Step  ,
 *
 */
public class Collection {

	public static void main(String[] args) throws IOException {
		String path = "data/vpr-June/collection/individual/";
		//System.out.println("32_02_2015-06-01".split("_2015")[0]);
		File folder = new File(path);
		File[] files = folder.listFiles();
		Map<String, String> collMap = new HashMap<String, String>();
		for (int i = 0; i < files.length; i++) {
			String id = files[i].getName().split("_2015-")[0];
			String day = files[i].getName().split("_2015-")[1].replaceAll(".txt", ""); 
			Scanner sc = new Scanner(files[i]);
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
			//System.out.println(strb.toString());
			
			collMap.put(id, strb.toString());
		}
		//System.out.println(collMap.get("301_03"));
		String savePath = "data/vpr-June/collection/byids/";
		for (String id : collMap.keySet()) {
			File f = new File(savePath+id);
			FileWriter fw = new FileWriter(f);
			fw.write(collMap.get(id));
			fw.flush();
			fw.close();
			
		}
	}

}
