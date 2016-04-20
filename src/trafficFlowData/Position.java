package trafficFlowData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Position {
	public static class po implements Comparable<po>{
		public long id;
		public int or;
		public double x;
		public double y;
		public po(String[] item){
			this.id = Long.parseLong(item[3]);
			this.or = Integer.parseInt(item[2]);
			this.x = Double.parseDouble(item[0]);
			this.y = Double.parseDouble(item[1]);
		}
		@Override
		public int compareTo(po o) {
			if (this.id == o.id) {
				return 0;
			}else {
				return id > o.id ? 1 : -1;
			}
			
		}
		
	}
	public static void main(String[] args) throws IOException {
		String fst = "data/p.txt";
		File f = new File(fst);
		Scanner sc = new Scanner(f);
		
		Set<po> dic = new HashSet<Position.po>();
		
		while (sc.hasNext()) {
			String s = (String) sc.nextLine();
			po i = new po(s.split(","));
		
			dic.add(i);
		}
		
		Map<String, String> result = new HashMap<String, String>();
		
		String fsg = "data/fs.txt";
		File fg = new File(fsg);
		Scanner scg = new Scanner(fg);
		while (scg.hasNext()) {
			String s = (String) scg.nextLine();
			StringBuilder vv = new StringBuilder();
			String[] ss = s.split(",");
			for (int i = 0; i < ss.length; i++) {
				String[] temp = ss[i].split("_");
				long id = Long.parseLong(temp[0]);
				int or = Integer.parseInt(temp[1]);
				for(po x : dic){
					if (id == x.id && or == x.or) {
						if (i==0) {
							vv.append("["+x.x+","+x.y+"]");
						}else {
							vv.append(",["+x.x+","+x.y+"]");
						}
						
					}
				}
				
			}
			result.put(s, vv.toString());
		}
		String fss = "data/map.txt";
		File fs = new File(fss);
		FileWriter fw = new FileWriter(fs);
		for(String k : result.keySet()) {
			String v = result.get(k);
			//fw.write(k+"\n");
			fw.write(v+"\n");
		}
		fw.write("\n");
		fw.flush();
	
	}

}
