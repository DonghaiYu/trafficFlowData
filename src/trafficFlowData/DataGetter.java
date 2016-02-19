package trafficFlowData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
/**
 * 
 * @author donghaiyu
 * @description Step 1
 * @input raw data of cars' pass records
 * @output 
 *
 */
public class DataGetter {
	 
	
	
	/**
	 * @return  Map<String, int[][]>(id,��������inte���ͳ����)
	 * @param inte ͳ��������ʱ����������Ϊ��λ��
	 * @param datafile ��ȡ��Դ�����ļ�·����
	 * @param ids   ����id�б�
	 */
	@SuppressWarnings("deprecation")
	public static Map<String, int[][]> getTimeFlow(List<String> ids,int inte,String datafile,String plateType){
		
		FileReader fr = null;
		try {
			fr = new FileReader(datafile);
		} catch (FileNotFoundException e) {
			System.out.println("�Ҳ�������Դ�ļ�"+datafile);
			System.exit(1);
		}
		
		Scanner sc = new Scanner(fr);
		String content = null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int interval = inte;
		
		if(interval <= 0 || interval >= (12 * 60 * 60)){
			interval = 60 * 5;//Ĭ��ʱ����
		}
		int timeinter = 60 * 60 / interval;
		int num[][] = new int[24][timeinter];
		
		Map<String, int[][]> map = new HashMap<String, int[][]>();
		Set<String> idnum = new HashSet<String>(ids);
		/*if(ids != null){
			for(String id : ids){
				map.put(id, new int[24][timeinter]);
			}
		}*/
		
		while(sc.hasNext()){
			content = sc.nextLine();
			String[] spl = content.split(",");
			try {
				Date date = formatter.parse(spl[5]);
				String idString = spl[2].trim();
				String plateTypeString = spl[1].trim();
				String orientation = spl[3].trim();
				//System.out.println(idString);
				int hour = date.getHours();
				int minu = date.getMinutes();
				int sec = date.getSeconds();
				
				String idKey = idString + "_" + orientation;
				
				if(ids != null ){
					
					//if(idnum.contains(idString)){
						int[][] tempnum = map.get(idKey);
						if (tempnum == null) {
							map.put(idKey, new int[24][timeinter]);
						}else {
							tempnum[hour][(minu * 60 + sec) / interval]++;
							map.put(idKey, tempnum);
						}						
					//}
					
				}else{				
					num[hour][(minu * 60 + sec) / interval]++;										
				}
				
			} catch (ParseException e) {
				System.out.println("�޷���ȡʱ�䣡from��" + content);
			}
		}
		
		sc.close();
		if(ids == null){
			map.put("00", num);
		}
		return map;
		
	}
	
	public static void saveResult(String savef,int[][] num,int inter){
		File f = new File(savef);
		FileWriter fw = null;
		try {
			fw = new FileWriter(f);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(int i=0;i<24;i++){
			for(int j=0;j<(60 * 60 /inter);j++){
				try {
					//System.out.println(i+":"+j+","+num[i][j]);
					fw.write(String.format("%02d", i)+":"+ String.format("%02d", (j * inter / 60)) +","+num[i][j]+"\n");
				} catch (IOException e) {
					System.out.println("�ļ�д�����file:" + savef);
				}
			}
		}
		try {
			fw.flush();
			fw.close();
		} catch (IOException e) {
			System.exit(1);
		}
	}
	
	public static void getTimeFlow(String folder,List<String> ids){
		
		File foldFile = new File(folder);	
		File[] datas = foldFile.listFiles();
		for(File file : datas){
			if(file.isFile() && file.getName().contains(".txt")){
				
				int inter = 300;//时间间隔，单位为秒
								
				Map<String,int[][]> map = DataGetter.getTimeFlow(ids,inter, folder+"/"+file.getName(),null);
				System.out.println("get finished,to save");
				if (map.size() == 1 && map.get("00") != null) {
					DataGetter.saveResult(folder+"/collection/"+file.getName(), map.get("00"), inter);
				}else {
					for (String id : map.keySet()){
						DataGetter.saveResult(folder+"/collection/individual/" + id + "_" + file.getName(), map.get(id), inter);
					}
				}
											
			}
		}
		
	}
	
	public static void getTimeFlowByPType(String folder,List<String> ids,String PType){
		
		File foldFile = new File(folder);	
		File[] datas = foldFile.listFiles();
		for(File file : datas){
			if(file.isFile() && file.getName().contains(".txt")){
				
				int inter = 300;
								
				Map<String,int[][]> map = DataGetter.getTimeFlow(null,inter, folder+"/"+file.getName(),"1");
				System.out.println(file.getName()+" ## "+ map.get("00"));
											
			}
		}
		
	}

	public static void getSum(String folder) throws IOException{
		
		File foldFile = new File(folder);	
		File[] datas = foldFile.listFiles();
		for(File file : datas){
			if(file.isFile() && file.getName().contains(".txt")){
				LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
				lnr.skip(file.length());
				int lnum = lnr.getLineNumber();
				System.out.print(lnum+",");
			}
		}
		
	}
	
	public static List<String> getIds(String filename){
		List<String> list = null;
		Set<String> idSet = new HashSet<String>();

		FileReader fr = null;
		try {
			fr = new FileReader(filename);
		} catch (FileNotFoundException e) {
			System.out.println("�Ҳ�������Դ�ļ�"+filename);
			System.exit(1);
		}
		
		Scanner sc = new Scanner(fr);
		while(sc.hasNext()){
			String temp = sc.nextLine();
			idSet.add(temp.trim());
		
		}
		list = new ArrayList<String>(idSet);
		System.out.println("totally "+list.size()+" ids");
		
		return list;
	}
	
	public static List<String> getIdOr(String filename){
		List<String> list = null;
		Set<String> idSet = new HashSet<String>();

		FileReader fr = null;
		try {
			fr = new FileReader(filename);
		} catch (FileNotFoundException e) {
			System.out.println("�Ҳ�������Դ�ļ�"+filename);
			System.exit(1);
		}
		
		Scanner sc = new Scanner(fr);
		while(sc.hasNext()){
			String temp = sc.nextLine();
			idSet.add(temp.trim());
		
		}
		list = new ArrayList<String>(idSet);
		System.out.println("totally "+list.size()+" ids");
		
		return list;
	}
	
	public static  Map<String, Integer>  maxFlowOneDay(List<String> ids,String folderpath,String month,String day){
		Map<String, Integer>  idMax = new HashMap<String, Integer>();
		for(String id : ids){
			
			String max = DataGetter.maxFlowFromFile(folderpath+"/"+id+"_2015-"+month+"-"+day+".txt");
			System.out.println("id:"+ id+"//max:"+max);
			if (max != null) {
				idMax.put(id, Integer.parseInt(max.trim()));
			}
		}
		
		
		System.out.println(idMax.toString());
		return idMax;
	}
	
	public static String maxFlowFromFile(String  path){
		int max = 0;
		String t = null;
		FileReader fr = null;
		try {
			fr = new FileReader(path);
		} catch (FileNotFoundException e) {
			System.out.println("can't find:"+path);
			return null;
		}
		
		Scanner sc = new Scanner(fr);
		while(sc.hasNext()){
			String[] temp = sc.nextLine().trim().split(",");
			int i = Integer.parseInt(temp[1]);
			
			if(i > max){
				max = i;
				t = temp[0];
			}
		}
		sc.close();
		try {
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return t+"/"+ max;
		return max+"";
	}

	public static void main(String[] args) throws IOException {
		
		//DataGetter.getTimeFlow(60, "data/vpr-June/2015-06-20.txt","D:/data/vpr-June/timeflow0620.txt");
		List<String> ids = DataGetter.getIds("data/ids.txt");
		
		DataGetter.getTimeFlow("data/vpr-June",ids);
		/*ids.clear();
		String[] temp ={"301_03","39_02","473_01","478_01","482_00","482_03","492_03"};
		for (int i = 0; i < temp.length; i++) {
			ids.add(temp[i]);
		}*/
		
		/*int max = 0;
		for (int d = 1;d < 9;d++) {
			Map<String, Integer>  idMax = DataGetter.maxFlowOneDay(ids, "data/vpr-June/collection/individual", "06", String.format("%02d", d));
			
			for (int num : idMax.values()) {
				if (num > max) {
					max = num;
				}
			}
			
		}
		System.out.println(max);*/
		
		//DataGetter.getSum("data/vpr-June");
	}

}
