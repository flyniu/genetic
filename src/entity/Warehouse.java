package entity;

import java.util.Random;

public class Warehouse {
	
	private int id;
	private Products demands;//��ͬ��Ʒ��������
	private Products recycles;//��ͬ��Ʒ�Ļ�����
	private float handlingSpeed;//װжЧ��hour/unit
	
	private int priority;//���ȼ�
	private int openTime;//���ʹ��ڿ�ʼʱ��
	private int closeTime;//���ʹ��ڽ���ʱ��
	private boolean ifUrgent;//�Ƿ�ܽ���,�����ȼ���ͬʱ�������ĵ���������
	public boolean isIfUrgent() {
		return ifUrgent;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public void setIfUrgent(boolean ifUrgent) {
		this.ifUrgent = ifUrgent;
	}
	public int getOpenTime() {
		return openTime;
	}
	public void setOpenTime(int openTime) {
		this.openTime = openTime;
	}
	public int getCloseTime() {
		return closeTime;
	}
	public void setCloseTime(int closeTime) {
		this.closeTime = closeTime;
	}

	private static int warehouseCount = 0;
	
	public Warehouse(){
		id = warehouseCount++;
		demands = new Products();
		recycles = new Products();
		handlingSpeed = 1.0f;
		priority = 1;
		openTime = 8;
		closeTime = 18;
		ifUrgent = false;
	}
	/**
	 * stimulate
	 * @param id
	 * @param demands
	 * @param recycles
	 * @param handlingSpeed
	 * @param priority
	 * @param openTime
	 * @param closeTime
	 * @param ifUrgent
	 */
	public Warehouse(int id, Products demands, Products recycles,
			float handlingSpeed, int priority, int openTime, int closeTime,
			boolean ifUrgent) {
		super();
		this.id = id;
		this.demands = demands;
		this.recycles = recycles;
		this.handlingSpeed = handlingSpeed;
		this.priority = priority;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.ifUrgent = ifUrgent;
	}
	static public int getWarehouseCount(){
		return warehouseCount;
	}
	public void setDemands(Products _demands){
		demands = _demands;
	}
	public Products getDemands(){
		return demands;
	}
	public void setRecycles(Products _recycles){
		recycles = _recycles;
	}
	public Products getRecycles(){
		return recycles;
	}
	public float getHandlingSpeed(){
		return handlingSpeed;
	}
	public void setHandlingSpeed(float _handlingSpeed){
		handlingSpeed = _handlingSpeed;
		
	}
	
	public void setTimeWindow(int _openTime, int _closeTime){
		openTime = _openTime;
		closeTime = _closeTime;
		
	}
	public void getTimeWindow(int _openTime, int _closeTime){
		_openTime = openTime;
		_closeTime = closeTime;
	}
	public void setPriority(int _priority){
		priority = _priority;
		
	}
	public int getPriority(){
		return priority;
	}
	public int getId(){
		return id;
	}
	
	/**
	 * ����һ������վ��
	 * @return Warehouse����������
	 */
	public static Warehouse generateWarehouse(){
		Warehouse warehouse = new Warehouse();
		Random rnd = new Random();
		Products demands = Products.generateProducts();
		demands.setAmount(0, rnd.nextInt(50));
		demands.setAmount(1, rnd.nextInt(50));
		demands.setAmount(2, rnd.nextInt(50));
		warehouse.setDemands(demands);
		
		Products recycle = Products.generateProducts();
		recycle.setAmount(0, rnd.nextInt(10));
		recycle.setAmount(1, rnd.nextInt(10));
		recycle.setAmount(2, rnd.nextInt(10));
		warehouse.setRecycles(recycle);
		
		return warehouse;
	}
}
