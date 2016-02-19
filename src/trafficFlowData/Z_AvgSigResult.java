package trafficFlowData;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class Z_AvgSigResult {
	
	public static class Err {
		private double mae;
		private double mre;
		private double rmse;
		
		public Err(double a,double b,double c) {
			this.mae = a;
			this.mre = b;
			this.rmse = c;
		}
		
		public Err(String[] errors) {
			for (int i = 0; i < errors.length; i++) {
				double e = Double.parseDouble(errors[i]);
				switch (i) {
				case 0:
					this.mae = e;
					break;
				case 1:
					this.mre = e;
					break;
				case 2:
					this.rmse = e;
					break;
				default:
					break;
				}
			}
		}
		
		public String  toString() {
			return this.mae+" "+this.mre+" "+this.rmse;
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		File f = new File("result/s23");
		Scanner sc = new Scanner(f);
		Err[] errorArray = new Err[40];
		int n = 0;
		while (sc.hasNext()) {
			System.out.println(n+"");
			String temp = (String) sc.nextLine();
			String[] errors = temp.split("\t");
			Err e = new Err(errors);
			errorArray[n] = e;
			n++;
		}
		Err[] avgErr = new Err[10];
		for (int i = 0; i < 10; i++) {
			double mae = 0;
			double mre = 0;
			double rmse = 0;
			for (int j = 0; j < 4; j++) {
				mae += errorArray[i*4+j].mae;
				mre += errorArray[i*4+j].mre;
				rmse += errorArray[i*4+j].rmse;
			}
			avgErr[i] = new Err(mae/4,mre/4,rmse/4);
		}
		
		for (int i = 0; i < avgErr.length; i++) {
			System.out.println(avgErr[i].toString());
		}
	}

}
