package entity;

import java.util.ArrayList;
import java.util.HashMap;


public class Schedule {
	//车辆编号
	public int carrierIndex;
	//该车需要跑的需求点
	public ArrayList<Integer> warehouseIndices;
	//该车需要跑的距离
	public float transDistance;
	//该车相对于没各需求点的运输量，K为需求点id，V为运输量
	public HashMap<Integer, Float> shippedVolumn;
	//每一趟的运输量
	public HashMap<Integer,Float> roundVolumn;
	//哪一天的（周一到周日，1234567）
	public int whichDay;
	//这趟排程的时长
	public String howLong;
	//这条路线跑几趟
	public HashMap<Integer, ArrayList<Integer>> round;
	public Schedule(){
		carrierIndex = -1;
		transDistance = 0.0f;
		shippedVolumn = new HashMap<Integer, Float>();
		warehouseIndices = new ArrayList<Integer>();
		whichDay = 1;
		round = new HashMap<Integer, ArrayList<Integer>>();
		roundVolumn = new HashMap<Integer, Float>();
		howLong = "";
	}
//	@Override
//	public String toString() {
//		JSONObject object = new JSONObject();
//		
//		
//	}
	
	public void ShowSchedule(){
		System.out.println("第" + whichDay + "天" + ",车辆编号="+carrierIndex + 
				",运输距离="+transDistance + ",运输时间=" + howLong);
		System.out.print("配送站点：");
		ArrayList<Integer> indices = new ArrayList<Integer>();
		
		for(int j = 0;j<round.size();j++){
			indices = round.get(j);
			System.out.print("  round" + j + ":");
			for(int l = 0;l<indices.size();l++){
				System.out.print("   "+indices.get(l));
			}
			
		}
		System.out.println();
		System.out.print("运输量:");
		float allVolumn = 0.0f;
		for(int j = 0;j<shippedVolumn.size();j++){
			System.out.print("站点编号：" + warehouseIndices.get(j) + " 运输量:" + shippedVolumn.get(warehouseIndices.get(j)) + " ;");
			allVolumn += shippedVolumn.get(warehouseIndices.get(j));
		}
		System.out.println();
		System.out.print("总云量：" + allVolumn);
		System.out.println();
		System.out.println();
	}
}
