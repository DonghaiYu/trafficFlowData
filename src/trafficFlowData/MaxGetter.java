package trafficFlowData;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MaxGetter {

	public static void main(String[] args) throws FileNotFoundException {
		File folder = new File("data/vpr-June/collection/individual");
		File[] files = folder.listFiles();
		int t =0;
		int last = 0;
		String message = null;
		Map<Integer, Integer> coll = new HashMap<Integer, Integer>();
		for (int i = 0; i < files.length; i++) {
			Scanner sc = new Scanner(files[i]);
			int max = 0;
			int tep = 0;
			boolean hascal = false;
			while (sc.hasNext()) {
				
				String temp = (String) sc.nextLine();
				int num = Integer.parseInt(temp.split(",")[1].trim());
				if (num > max) {
					max = num;
					message = files[i].getName()+"\n"+temp;
				}
				/*if (tep == 0) {
					last=num;
				}else {
					if (Math.abs(num-last) > 300 && !hascal) {
						t++;
						hascal=true;
						coll.put(last, num);
						
					}
					last=num;
				}
				tep++;*/
			}
			sc.close();
			/*if (hascal) {
				boolean x =files[i].renameTo(new File("E:\\javacode\\trafficFlowData\\data\\vpr-June\\collection\\0"+files[i].getName()));
				System.out.println(x);
			}*/
			int index = max/100; 
			if (coll.containsKey(index)) {
				int temp = coll.get(index);
				temp+=1;
				coll.put(index, temp);
			}else {
				coll.put(index, 1);
			}
			//System.out.println(i);
		}
		System.out.println(t);
		System.out.println(coll.toString());
	}

}
