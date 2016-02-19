package trafficFlowData;

import java.io.File;

public class sdfas {	
	public static void main(String[] ar){
		//System.out.println("032_02_2015-06-08".replaceFirst("0", ""));
		/*File path = new File("data/vpr-June/collection");
		File[] fs = path.listFiles();
		for (int i = 0; i < fs.length; i++) {
			if (fs[i].getName().startsWith("0") && fs[i].isFile()) {
				String newName = fs[i].getName().replaceFirst("0", "");
				boolean x = fs[i].renameTo(new File("E:\\javacode\\trafficFlowData\\data\\vpr-June\\collection\\individual\\"+newName));
				System.out.println(x);
			}
			
		}*/
		StringBuilder sb = new StringBuilder();
		sb.append("dafs\ndfasdf");
		sb.insert(0, "0");
		System.out.println(sb.toString());
		
	}
	
}
