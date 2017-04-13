package entity;

import java.util.ArrayList;
import java.util.HashMap;


public class Schedule {
	//�������
	public int carrierIndex;
	//�ó���Ҫ�ܵ������
	public ArrayList<Integer> warehouseIndices;
	//�ó���Ҫ�ܵľ���
	public float transDistance;
	//�ó������û����������������KΪ�����id��VΪ������
	public HashMap<Integer, Float> shippedVolumn;
	//ÿһ�˵�������
	public HashMap<Integer,Float> roundVolumn;
	//��һ��ģ���һ�����գ�1234567��
	public int whichDay;
	//�����ų̵�ʱ��
	public String howLong;
	//����·���ܼ���
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
		System.out.println("��" + whichDay + "��" + ",�������="+carrierIndex + 
				",�������="+transDistance + ",����ʱ��=" + howLong);
		System.out.print("����վ�㣺");
		ArrayList<Integer> indices = new ArrayList<Integer>();
		
		for(int j = 0;j<round.size();j++){
			indices = round.get(j);
			System.out.print("  round" + j + ":");
			for(int l = 0;l<indices.size();l++){
				System.out.print("   "+indices.get(l));
			}
			
		}
		System.out.println();
		System.out.print("������:");
		float allVolumn = 0.0f;
		for(int j = 0;j<shippedVolumn.size();j++){
			System.out.print("վ���ţ�" + warehouseIndices.get(j) + " ������:" + shippedVolumn.get(warehouseIndices.get(j)) + " ;");
			allVolumn += shippedVolumn.get(warehouseIndices.get(j));
		}
		System.out.println();
		System.out.print("��������" + allVolumn);
		System.out.println();
		System.out.println();
	}
}
