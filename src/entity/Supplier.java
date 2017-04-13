package entity;

import entity.Carrier;
import entity.Products;
import global.Category;

public class Supplier {
	private Products supply;//各个货品的总供应量
	private Carrier[] carriers;//拥有的车辆
	private float efficiency;//出入库效率
	private int[] stock;
	public Supplier(){
		supply = new Products();
		carriers = null;
		efficiency = 1.0f;
		stock = new int[Category.productCategoryCount];
	}
	
	public Supplier(Carrier[] carriers) {
		super();
		this.supply = Products.generateProducts();
		this.carriers = carriers;
		this.efficiency = 6.047f;
	}

	public Products getSupply(){
		return supply;
	}
	public void setSupply(Products _supply){
		supply = _supply;
	}
	public Carrier[] getCarriers(){
		return carriers;
	}
	public void setCarriers(Carrier[] _carriers){
		carriers = _carriers;
	}
	
	/**
	 * 生成一个计量中心，包含了两种车辆的信息以及不同品类货物的数量
	 * @return Supplier：计量中心对象
	 */
	public static Supplier generateSupplier(){
		
		Supplier supplier = new Supplier();
		
		int numCarriers = 5;
		Carrier[] carriers = new Carrier[numCarriers];
		int speed[] = {50,50,60,60,70};
		int vol[] = {200,200,150,150,120};
		int transTime[] = {10,10,8,8,7};
		int costPerKim[] = {10,10,8,8,7};
		for(int j = 0;j<numCarriers;j++)
		{
			carriers[j] = new Carrier(vol[j],speed[j],transTime[j]);
			carriers[j].setCostPerKim(costPerKim[j]);
		}
		
		supplier.setCarriers(carriers);
		
		Products supply = Products.generateProducts();
		
		for(int j = 0;j<Products.productCategoryCount;j++){
			supply.setAmount(j, 1000);
		}
		supplier.setSupply(supply);
		
		return supplier;
	}
}
