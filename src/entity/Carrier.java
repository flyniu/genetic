package entity;

public class Carrier {
	private int volumn;//汽车最大运输量
	private int speed;//汽车速度
	private int maxTransTime;//最大运输时间
	private int wages;//司机的薪资
	public int getWages() {
		return wages;
	}
	/**
	 * stimulate
	 * @param volumn
	 * @param speed
	 * @param wages
	 * @param costPerKim
	 * @param carId
	 */
	public Carrier(int volumn, int speed, int wages, int costPerKim, int carId) {
		super();
		this.volumn = volumn;
		this.speed = speed;
		this.wages = wages;
		this.costPerKim = costPerKim;
		this.carId = carId;
	}

	public void setWages(int wages) {
		this.wages = wages;
	}
	private int costPerKim;//每公里所消耗的钱
	public int getCostPerKim() {
		return costPerKim;
	}

	public void setCostPerKim(int costPerKim) {
		this.costPerKim = costPerKim;
	}
	private int carId;
	private int whichDay;//在哪天跑
	public int getWhichDay() {
		return whichDay;
	}

	public void setWhichDay(int whichDay) {
		this.whichDay = whichDay;
	}

	public int getCarId() {
		return carId;
	}

	public void setCarId(int carId) {
		this.carId = carId;
	}
	private static int carCount = 0;
	public Carrier(){
		volumn = 0;
		speed = 0;
		maxTransTime = 0;
		whichDay = 1;
		carId = carCount++;
		wages = 4000;
	}
	
	public Carrier(int _volumn, int _speed, int _maxTransTime){
		carId = carCount++;
		volumn = _volumn;
		speed = _speed;
		maxTransTime = _maxTransTime;
		wages = 4000;
	}
	public int getVolumn(){		
		return volumn;
	}
	public int getSpeed(){
		return speed;
	}
	public int getTransTime(){
		return maxTransTime;
	}	

	public void setVolumn(int _volumn){
		volumn = _volumn;
	}
	public void setSpeed(int _speed){
		speed = _speed;
	}
	public void setTransTime( int _maxTransTime){
		maxTransTime = _maxTransTime;
	}
}
