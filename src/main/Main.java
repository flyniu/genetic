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
	 * 种群数量：50
	 * 最多迭代次数：5000
	 * 变异概率：0.001
	 * 交叉淘汰数组：5(0.1概率)
	 * 交叉和变异的最大迭代次数：3000
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//随即生成基因序列，一个基因序列是一个个体，共50个个体
		//计算每个个体的适应度（时间/成本）
		//计算每个个体的被选中交叉编【译的概率（个体适应度/总适应度）
		//产生随机数，落入该个体区间内的，选中进行交叉编译
		//随即选出某个位置进行交叉编译
		//直到共有50个个体后，随机选出5个（50*0.1）个体进行变异，变异位置随即
		//计算个体适应度
		//……
		//达到5000次
		//选出适应度最高的个体
		Main main = new Main();
		long timeStart = System.currentTimeMillis();
		RunAlgori runAlgori = new RunAlgori(main);
		Individual individual = runAlgori.Reproduction();
		long timeEnd = System.currentTimeMillis();
		long timeSpent = timeEnd - timeStart;
		int hour = (int) (timeSpent/(60*60*1000));
		int min = (int) ((timeSpent - hour*60*60*1000)/(60*1000));
		int second = (int) ((timeSpent - hour*60*60*1000 - min*60*1000)/(1000));
		System.out.println("用时：" + hour + "时" + min + "分" + second + "秒");
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
