package main;

import java.util.ArrayList;
import java.util.List;

import utils.Algorithm;
import utils.ExcelTools;
import utils.RunAlgori;
import entity.Carrier;
import entity.Individual;
import entity.Schedule;
import entity.Supplier;
import entity.Warehouse;
import external_data_interface.DataInterface;
import global.Global;

public class Main implements DataInterface{
	static Supplier supplier ;
	static ArrayList<Warehouse> allWarehList;
	/**
	 * @param args
	 * ��Ⱥ������50
	 * ������������5000
	 * ������ʣ�0.001
	 * ������̭���飺5(0.1����)
	 * ����ͱ����������������3000
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//�漴���ɻ������У�һ������������һ�����壬��50������
		//����ÿ���������Ӧ�ȣ�ʱ��/�ɱ���
		//����ÿ������ı�ѡ�н���ࡾ��ĸ��ʣ�������Ӧ��/����Ӧ�ȣ�
		//���������������ø��������ڵģ�ѡ�н��н������
		//�漴ѡ��ĳ��λ�ý��н������
		//ֱ������50����������ѡ��5����50*0.1��������б��죬����λ���漴
		//���������Ӧ��
		//����
		//�ﵽ5000��
		//ѡ����Ӧ����ߵĸ���
		Main main = new Main();
		long timeStart = System.currentTimeMillis();
		RunAlgori runAlgori = new RunAlgori(main);
		Individual individual = runAlgori.Reproduction();
		long timeEnd = System.currentTimeMillis();
		long timeSpent = timeEnd - timeStart;
		int hour = (int) (timeSpent/(60*60*1000));
		int min = (int) ((timeSpent - hour*60*60*1000)/(60*1000));
		int second = (int) ((timeSpent - hour*60*60*1000 - min*60*1000)/(1000));
		System.out.println("��ʱ��" + hour + "ʱ" + min + "��" + second + "��");
		System.out.println("gene:");
		for(int i = 0;i<Global.CAR_NUM;i++){
			for(int j = 0;j<Global.WAREHOUSE_NUM;j++){
				System.out.print(individual.getGene()[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println("loaded cargo:");
		for(int i = 0;i<Global.CAR_NUM;i++){
			for(int j = 0;j<Global.WAREHOUSE_NUM;j++){
				System.out.print(individual.getLoadedCargo()[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println("best one fitness is:" + individual.getFitness());
	}
	@Override
	public float[][] getDistance() {
		// TODO Auto-generated method stub
		float[][] distances = ExcelTools.getDistancesFromExcel();
		for (int x = 0; x < distances.length; x++) {
			for (int y = 0; y < distances[x].length; y++) {
				System.out.print(distances[x][y] + "  ");
			}
			System.out.println();
		}
		return distances;
	}

	@Override
	public Supplier getSupplier(Supplier supplier) {
		// TODO Auto-generated method stub
		supplier = ExcelTools.getSupplierFromExcel();
		Carrier[] carriers = supplier.getCarriers();
		for (int x = 0; x < carriers.length; x++) {
			//System.out.println("carrier" + i + ": " + carriers[i].getVolumn());
			//System.out.println(carriers[x].getCarId() + "  "+ carriers[x].getVolumn());
		}
		return supplier;
	}

	@Override
	public ArrayList<Warehouse> getWarehouses(ArrayList<Warehouse> warehouses) {
		// TODO Auto-generated method stub
		warehouses = ExcelTools.getWarehouseFromExcel();
		for (int i = 0; i < warehouses.size(); i++) {
			//System.out.println(warehouses.get(i).getId() + " * "
				//	+ warehouses.get(i).getDemands().getAmount(0) + " * ");
		}
		//System.out.println();
		return warehouses;
	}
	

	@Override
	public void saveSchedule(String type, List<Schedule> schedules) {
		// TODO Auto-generated method stub
		System.out.println("save " + type + " schedules to database");
	}

	@Override
	public void savePlannedSchedule(String type, List<Schedule> schedules) {
		// TODO Auto-generated method stub
		System.out.println("save planned " + type +  " schedules to database");
	}
}
