package utils;

import java.util.ArrayList;
import java.util.HashMap;

import entity.Individual;
import entity.Supplier;
import entity.Warehouse;
import external_data_interface.DataInterface;

public class RunAlgori {
	DataInterface dataInterface;
	
	Supplier supplier;
	ArrayList<Warehouse> allWarehList;
	float[][] distances;
	
	HashMap<Integer, Integer> countToid;
	HashMap<Integer, Integer> idTocount;
	
	public RunAlgori(DataInterface dataInterface){
		this.dataInterface = dataInterface;
		//supplier = dataInterface.getSupplier(supplier);
		//allWarehList = dataInterface.getWarehouses(allWarehList);
		//distances = dataInterface.getDistance();
		//CountVersusId();
	}
	public Individual Reproduction(){
		supplier = dataInterface.getSupplier(supplier);
		allWarehList = dataInterface.getWarehouses(allWarehList);
		distances = dataInterface.getDistance();
		CountVersusId();
		
		Algorithm algorithm = new Algorithm(distances, allWarehList, supplier, countToid, idTocount);
		Individual individual = algorithm.getBestChild();
		//return null;
		return individual;
	}
	private void CountVersusId(){
		countToid = new HashMap<Integer, Integer>();
		idTocount = new HashMap<Integer, Integer>();
		for(int i = 0;i<=allWarehList.size();i++){
			if(i == allWarehList.size()){
				countToid.put(i, i);
				idTocount.put(i, i);
			}else{
				countToid.put(i, allWarehList.get(i).getId());
				idTocount.put(allWarehList.get(i).getId(), i);
				System.out.println("**key:" + i + "value:" + allWarehList.get(i).getId());
			}
			
		}
	}
}
