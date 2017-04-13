package entity;

import global.Category;

import java.util.Random;

public class Products {
	
    public static int productCategoryCount = Category.productCategoryCount;
	//occupation表示单位货品所占的空间
	private float[] occupation = new float[productCategoryCount];
	//amount表示不同货品的总量
	private int[]   amount = new int[productCategoryCount];
	public Products(){
		//spaceOccupation = new float[productCategoryCount];
		//amount   = new int[productCategoryCount];
	}
	public boolean setAmount(int _index, int _amount){
		if(_index<0 || _index>=productCategoryCount) return false;
		if(_amount<0) return false;
		amount[_index] = _amount;
		return true;
	}
	public boolean setOccupation(int _index, float _occupation){
		if(_index<0 || _index>=productCategoryCount) return false;
		if(_occupation<0) return false;
		occupation[_index] = _occupation;
		return true;
	}
	public int getAmount(int _index){
		if(_index<0 || _index>=productCategoryCount) return -1;
		return amount[_index];
	}
	public float getOccupation(int _index){
		if(_index<0 || _index>=productCategoryCount) return -1;
		return occupation[_index];
	}
	/**
	 * stimulate
	 * @param occupation
	 * @param amount
	 */
	public Products(float[] occupation, int[] amount) {
		super();
		this.occupation = occupation;
		this.amount = amount;
	}
	/**
	 * 随机生成货品
	 * @return Products:货品参数
	 */
	public static Products generateProducts(){
		Products products = new Products();
		Random rnd = new Random();
		
		products.setOccupation(0, Category.productCategory1);
		products.setAmount(0, 100);
		products.setOccupation(1, Category.productCategory2);
		products.setAmount(1, 120);
		products.setOccupation(2, Category.productCategory3);
		products.setAmount(2, 90);
		return products;
	}
}
