package trafficFlowData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Shuffle {

	public static void main(String[] args) throws IOException {
		File readFolder = new File("data/a0");
		String saveFolder = "data/a/";
		File[] fArray = readFolder.listFiles();
		for (int i = 0; i < fArray.length; i++) {
			Scanner sc = new Scanner(fArray[i]);
			String name = fArray[i].getName();
			List<String> list = new ArrayList<String>();
			while (sc.hasNext()) {
				String temp = (String) sc.nextLine();
				list.add(temp);
			}
			Collections.shuffle(list);
			
			File saveFile = new File(saveFolder+name);
			FileWriter fw = new FileWriter(saveFile);
			
			for (int j = 0; j < list.size(); j++) {
				fw.write(list.get(j)+"\n");
			}
			fw.flush();
			fw.close();
		}
	}

}
