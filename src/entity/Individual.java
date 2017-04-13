package entity;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.xml.internal.bind.util.Which;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import utils.Algorithm;
import utils.CommonUtils;
import utils.FileWriterTools;
import global.Category;
import global.Global;


public class Individual {
	private int[][] gene;
	private float fitness;
	private float[][] distances;
	private ArrayList<Warehouse> warehouses;
	private Supplier supplier;
	private Carrier[] cars;

	private float[] carRestVolumn;
	private float[] warehousesRest;

	private int rectCount = Global.CAR_NUM * Global.WAREHOUSE_NUM;

	public HashMap<Integer, Integer> countToid;
	public HashMap<Integer, Integer> idTocount;

	private float[][] loadedCargo;
	private LastChanged lastChanged;
	public Individual(int id, Algorithm algorithm) {
		gene = new int[Global.CAR_NUM][Global.WAREHOUSE_NUM];
		loadedCargo = new float[Global.CAR_NUM][Global.WAREHOUSE_NUM];
		distances = algorithm.getDistances();
		supplier = algorithm.getSupplier();
		warehouses = algorithm.getWarehouses();
		cars = supplier.getCarriers();
		carRestVolumn = new float[cars.length];
		warehousesRest = new float[warehouses.size()];
		countToid = algorithm.getCountToid();
		idTocount = algorithm.getIdTocount();
		lastChanged = new LastChanged();
		setRest();
		generateGene();
		calculateFit();
	}
	public Individual(){
		lastChanged = new LastChanged();
	}
	// 随即生成个体基因,每一辆车对于每一个点，生成一个0-100的随机数，≥40则为0（不送这个点）,小于40则送这个点，设置为1
	// 如果送这个点，那么首先判断这个点的需求量，还有剩余量的话，然后判断是否送完这个点后，超出时间窗，
	// 是否送完后回到计量中心会超出工作时长，都合理则就设置为1，并且将剩余量清空或者将车辆的剩余量清空，
	// 循环直到所有车辆都满或者所有需求点的需求量清空或者矩阵循环完毕
	// 这样产生的初代必定是可行解
	/**
	 * 每个点要装多少： 10 20 要装的点有多少：0 1 0 1 0| 0 0 0 0 0 0 0
	 * 
	 * 每个点要装多少：5 5 要装的点有多少：1 0 1 0 0| 0 0 0 0 0 0 0
	 * 
	 * 这样交叉的话，可能会出现下面那辆车超出容量 而且由于车速不一样，或许交叉后会出现
	 */
	private void generateGene() {
		for (int i = 0; i < Global.CAR_NUM; i++) {
			for (int j = 0; j < Global.WAREHOUSE_NUM; j++) {
				gene[i][j] = 0;
			}
		}
		while ((!ifAllCarLoaded()) && (!ifAllWarehCleaned()) && (rectCount > 0)) {
			for (int carCount = 0; carCount < cars.length; carCount++) {
				for (int wareCount = 0; wareCount < warehouses.size(); wareCount++) {
					int random = (int) (Math.random() * 100);
					// 如果小于(车数量/需求点数量)*100
					if (random < ( 18/ Global.WAREHOUSE_NUM) * 100) {
						// 如果需求点没被清空
						if (warehousesRest[wareCount] > 0) {
							// 如果车辆可以运送
							if (carRestVolumn[carCount] > 0
									&& !ifOverTime(carCount)) {
								// 变动两者的剩余量,变动loadedCargo
								if (carRestVolumn[carCount] > warehousesRest[wareCount]) {
									carRestVolumn[carCount] -= warehousesRest[wareCount];
									warehousesRest[wareCount] = 0;
									loadedCargo[carCount][wareCount] += warehousesRest[wareCount];
								} else {
									warehousesRest[wareCount] -= carRestVolumn[carCount];
									carRestVolumn[carCount] = 0;
									loadedCargo[carCount][wareCount] += carRestVolumn[carCount];
								}
								gene[carCount][wareCount] = 1;
							}
							
						}
					}
					rectCount--;
				}
			}
		}
		
	/*	for (int i = 0; i < Global.CAR_NUM; i++) {
			for (int j = 0; j < Global.WAREHOUSE_NUM; j++) {
				System.out.print(gene[i][j]);
			}
			System.out.println();
			
		}	*/
	}
	// 这个函数已确定没问题
	// 计算个体的适应度
	// 成本最低：送过的需求量/所有路径相加
	// 时间最短：送过的需求量/所有时间相加
	// 最后得出来再×100，为了数据好看
	// 还要看是不是可行解（超出了车辆载重，超出了时间限制）
	// 先按成本最低写
	// 如果可行那么返回true，不可行返回false，并且fitness设置为-1
	public boolean calculateFit() {
		int lastCount = Global.WAREHOUSE_NUM;
		int toGoCount = 0;
		int checkedCount = 0;
		float allLength = 0f;
		float allCarLength = 0f;
		float allDemand = 0f;
		boolean ifFirst = true;
		float[] oneCarDemand = new float[Global.CAR_NUM];
		float[] oneCarTime = new float[Global.CAR_NUM];
		float lastLength = 0f;
		float lastDemand = 0f;
		ArrayList<Integer> warehouseList = new ArrayList<Integer>();
		//setRest();
		for (int i = 0; i < Global.CAR_NUM; i++) {
			warehouseList.clear();
			// 有多少个1
			for (int j = 0; j < Global.WAREHOUSE_NUM; j++) {
				if (gene[i][j] == 1) {
					toGoCount++;//记录总共这辆车怂了多少个点
				}
			}
//			// 没有1则直接跳过
			// 当前车辆给所有需求点送了多少量 
			for (int j = 0; j < Global.WAREHOUSE_NUM; j++) {
				if (gene[i][j] == 1) {	
						// 先将需求量累加
						allDemand += loadedCargo[i][j];
						oneCarDemand[i] = oneCarDemand[i] + loadedCargo[i][j];
						checkedCount++;//计算需求量
						warehouseList.add(j);					
				}
//				FileWriterTools.WriteToLog("Individual-changedLoadedCargo-individual" + this.id + "gene[" + i + "][" + j + "]:" + gene[i][j]
//						+ " loadedCargo:" + loadedCargo[i][j] + " warehouseRest:" + warehousesRest[j]
//								+ " carRest:" + carRestVolumn[i] + " carVolumn:" + cars[i].getVolumn());
			}
			//CommonUtils.MyLog("Individual", "calculateFit", "第",i,"辆车总共送",oneCarDemand[i]);
			int[] warehouseCount = new int[warehouseList.size()];//算距离
			for (int j = 0; j < warehouseList.size(); j++) {
				warehouseCount[j] = warehouseList.get(j);
			}
			allLength = getTravelLength(warehouseCount);//计算这辆车走的最短距离
			
			if(allLength != 1){
				allCarLength += allLength;
			}else{
				allCarLength += 0;
			}
			
//			// 更新这个车装的多少
			// 更新这辆车跑了多少
			if(allLength == 1){
				oneCarTime[i] = 0;
			}else{
				oneCarTime[i] = (allLength / cars[i].getSpeed());
			}
			
		/*	CommonUtils.MyLog("Individual", "calculateFit",
					"allLength and speed car", i, ":", allLength, " and ",
					cars[i].getSpeed());*/
			toGoCount = 0;
			checkedCount = 0;
			lastCount = Global.WAREHOUSE_NUM;
			ifFirst = true;
//			FileWriterTools.WriteToLog("oneCarLength:" + allLength);
			
		}
		//FileWriterTools.WriteToLog("allDemand:" + allDemand);
		for (int i = 0; i < Global.CAR_NUM; i++) {
			if (oneCarDemand[i] > cars[i].getVolumn()) {
				/*CommonUtils.MyLog(
						"Individual",
						"calculateFit",
						"over car volumn:" + oneCarDemand[i] + ">"
								+ cars[i].getVolumn());*/

				fitness = -1;
				return false;
			} else {
			/*	CommonUtils.MyLog(
						"Individual",
						"calculateFit",
						"one car volumn:" + oneCarDemand[i] + "<"
								+ cars[i].getVolumn(), "  workable");*/
			}
			if (oneCarTime[i] > Category.driverWorkTime) {
			/*	CommonUtils.MyLog("Individual", "calculate", "over car time:"
						+ oneCarTime[i] + ">" + Category.driverWorkTime);*/
				fitness = -1;
				return false;
			} else {
			/*	CommonUtils.MyLog("Individual", "calculate", "one car time:"
						+ oneCarTime[i] + "<" + Category.driverWorkTime,
						"  workable");*/
			}
		}
//		FileWriterTools.WriteToLog("allLength:" + allCarLength);
//		FileWriterTools.WriteToLog("allDemand:" + allDemand);
		// 扩大路径短的个体的优势
		if (allCarLength < 1) {
			fitness = 0;
		}else{
			fitness = (float) ((allDemand  +  (1/Math.pow(allCarLength, 2)) * Global.FITNESS_ENLARGE));
		}
//		FileWriterTools.WriteToLog(fitness + "");
		// 扩大适应度高的个体的优势
		// fitness = (float) Math.pow(fitness, 2);
		return true;
	}


	public boolean ifCarRebundant() {
		boolean result = false;

		return result;
	}

	// 是否超出时间限制
	private boolean ifOverTime(int whichCar) {
		boolean result = false;
		float distance = 0f;
		int lastCount = warehouses.size();
		for (int i = 0; i < Global.WAREHOUSE_NUM; i++) {
			if (gene[whichCar][i] == 1) {
				distance += distances[i][lastCount];
				lastCount = i;
			}
		}
		// 这辆车什么点都不走
		if (lastCount == warehouses.size()) {
			return false;
		}
		// 是否超出了时间窗
		if (distance / cars[whichCar].getSpeed()
				+ warehouses.get(countToid.get(lastCount)).getOpenTime() > warehouses
				.get(countToid.get(lastCount)).getCloseTime()) {
			result = true;
			return result;
		}
		// 是否超出工作时长
		distance += distances[lastCount][warehouses.size()];
		if (distance / cars[whichCar].getSpeed() > Category.driverWorkTime) {
			result = true;
			return result;
		}
		return result;
	}

	// 填充Rest
	private void setRest() {
		for (int i = 0; i < cars.length; i++) {
			carRestVolumn[i] = cars[i].getVolumn();
		}
		for (int i = 0; i < warehouses.size(); i++) {
			warehousesRest[i] = 0f;
			for (int j = 0; j < Category.productCategoryCount; j++) {
				warehousesRest[i] += warehouses.get(i).getDemands()
						.getAmount(j) * warehouses.get(i).getDemands().getOccupation(j);
				
				
			}

			//System.out.println(warehousesRest[i]);
		}
	}

	// 是不是所有的车都装满了
	private boolean ifAllCarLoaded() {
		boolean result = true;
		for (int i = 0; i < carRestVolumn.length; i++) {
			if (carRestVolumn[i] != 0) {
				result = false;
				break;
			}
		}
		return result;
	}

	// 是否所有需求点都送完了
	private boolean ifAllWarehCleaned() {
		boolean result = true;
		for (int i = 0; i < warehousesRest.length; i++) {
			if (warehousesRest[i] != 0) {
				result = false;
				break;
			}
		}
		return result;
	}
	//这个已确定没问题
	private float getTravelLength(int[] warehousesCount) {
		if (warehousesCount.length == 0) {
			return 1;
		}
		//System.out.print("Individual-getTravelLength-warehousesCount:");
		//for (int i = 0; i < warehousesCount.length; i++) {
			//System.out.print(" " + warehousesCount[i]);
		//}
		//System.out.println();
		float length = 0f;
		int closestToSupplier = warehousesCount[0];
		int[] checkedCount = new int[warehousesCount.length];
		int closestPosition = 0;
		for (int i = 0; i < warehousesCount.length; i++) {
			if (distances[warehousesCount[i]][Global.WAREHOUSE_NUM] < distances[closestToSupplier][Global.WAREHOUSE_NUM]) {
				closestToSupplier = warehousesCount[i];
				closestPosition = i;
			}
		}
		checkedCount[closestPosition] = 1;
		int i = 0;
		int nowClosest = closestPosition;
		boolean ifFirst = true;
		int addCount = 0;
		while (true) {

			// i是记录当前在warehouseCount的哪个一点上
			if (i == warehousesCount.length) {
				i = 0;
			}
			// 如果是第一次进入循环
			if (ifFirst) {
				ifFirst = false;

				length += distances[closestToSupplier][Global.WAREHOUSE_NUM];
				/*CommonUtils.MyLog("Individual", "getTravelLength",
						"first length : ",
						distances[closestToSupplier][Global.WAREHOUSE_NUM],
						" waresouse:", closestToSupplier);*/
			} else {
				float tempLength = 99999f;
				// 其他情况，先找出离当前点最近的点，第一次进入循环后，当前最近的点为closestToSuppliere
				for (int j = 0; j < warehousesCount.length; j++) {
					// 走过的点就跳过
					if (checkedCount[j] == 1) {
						continue;
					}

					// 遍历所有的点，找出最近的，离ClosestSupplier记录最近的点
					if (distances[warehousesCount[j]][closestToSupplier] < tempLength) {
						nowClosest = j;
						tempLength = distances[warehousesCount[j]][closestToSupplier];
					}

				}
				// 更新长度
				// CommonUtils.MyLog("Individual",
				// "getTravelLength","nowClosest",nowClosest," closestToSupplier ",closestToSupplier);
				length += distances[warehousesCount[nowClosest]][closestToSupplier];
				/*CommonUtils
						.MyLog("Individual",
								"getTravelLength",
								"middle length : ",
								distances[warehousesCount[nowClosest]][closestToSupplier],
								" waresouse:", warehousesCount[nowClosest]);*/
				// 找出了这个点
				closestToSupplier = warehousesCount[nowClosest];
				// 更新checkedcount
				checkedCount[nowClosest] = 1;
			}
			// 是否所有的点都走过了，0是没走过 1是走过
			for (int j = 0; j < warehousesCount.length; j++) {
				if (checkedCount[j] == 1) {
					addCount += 1;
				}
			}
		/*	CommonUtils
					.MyLog("Individual", "getTravelLength", "addCount",
							addCount, " warehousesCount.length",
							warehousesCount.length);*/
			// 如果所有的点都被走过了，那么就跳出循环
			if (addCount == warehousesCount.length) {
				length += distances[closestToSupplier][Global.WAREHOUSE_NUM];
		/*		CommonUtils.MyLog("Individual", "getTravelLength",
						"end length : ",
						distances[Global.WAREHOUSE_NUM][closestToSupplier],
						" waresouse:", closestToSupplier);*/
				break;
			}
			addCount = 0;
		}
		return length;
	} 
	//这个函数已确认没问题
	public void changedCarsLoadedCargo() {
		// 先将从1到0的点，需求量释放
		ArrayList<Integer> beforeChanged = lastChanged.getBeforeChanged();
		ArrayList<Integer> afterChanged = lastChanged.getAfterChanged();
		ArrayList<Integer> whichCar = lastChanged.getWhichCar();
		ArrayList<Integer> whichGene = lastChanged.getWhichWarehouse();
		ArrayList<Integer> wareHQueue = new ArrayList<Integer>();		
			for (int i = 0; i < beforeChanged.size(); i++) {
				/*CommonUtils.MyLog("Individual", "changedCarsLoadedCargo","before:",beforeChanged.get(i),
						"  after:",afterChanged.get(i));		*/
				if (!ifHas(wareHQueue, whichGene.get(i))) {
					wareHQueue.add(whichGene.get(i));
				}
				
				if (beforeChanged.get(i) == 1 && afterChanged.get(i) == 0) {
					//CommonUtils.MyLog("Individual", "changedCarsLoadedCargo","1->0");
					carRestVolumn[whichCar.get(i)] += loadedCargo[whichCar.get(i)][whichGene
							.get(i)];
					warehousesRest[whichGene.get(i)] += loadedCargo[whichCar.get(i)][whichGene
							.get(i)];
					loadedCargo[whichCar.get(i)][whichGene.get(i)] = 0f;
				}
			}
			//FileWriterTools.WriteBefore("wareHqueue:" + wareHQueue.size() + " " + wareHQueue.get(0));
			//FileWriterTools.WriteAfter("wareHqueue:" + wareHQueue.size() + " " + wareHQueue.get(0));
			int theCar = 0;
			int theWarehouse = 0;
			// 首先来看看都有哪些变了
			
			for (int i = 0; i < wareHQueue.size(); i++) {
				//CommonUtils.MyLog("Individual", "changedCarsLoadedCargo",
						//"现在是仓库",wareHQueue.get(i));
				theWarehouse = wareHQueue.get(i);
				// 如果这个点的剩余需求量已经为0，那么就不用再派车了，同时把基因变成0
				//FileWriterTools.WriteBefore(wareHQueue.get(i) + " wareH loop");
				//FileWriterTools.WriteAfter(wareHQueue.get(i) + " wareH loop");
				// 遍历这个数组里面的所有车
				for (int j = 0; j < whichCar.size(); j++) {
					//FileWriterTools.WriteBefore("whichCar loop" + j);
					//FileWriterTools.WriteAfter("whichCar loop" + j);
					if (whichGene.get(j) == theWarehouse) {
						if (warehousesRest[theWarehouse] == 0) {
							//FileWriterTools.WriteBefore("whichCar loop warehousesrest = 0" + j);
							//FileWriterTools.WriteAfter("whichCar loop warehousesrest = 0" + j);
						/*	CommonUtils.MyLog("Individual",
									"changedCarsLoadedCargo", whichCar.get(j),
									"车的", whichGene.get(j), "基因由已经剩余需求量为0，无需再配送");*/
							theCar = whichCar.get(j);
							gene[theCar][theWarehouse] = 0;
							continue;
						}
						if (beforeChanged.get(j) == 1 && afterChanged.get(j) == 0) {
							//FileWriterTools.WriteBefore("whichCar loop before = 1 after = 0" + j);
							//FileWriterTools.WriteAfter("whichCar loop before = 1 after = 0" + j);
							continue;
						} else if (beforeChanged.get(j) == afterChanged.get(j)) {
							//FileWriterTools.WriteBefore("whichCar loop before = after" + j);
							//FileWriterTools.WriteAfter("whichCar loop before = after" + j);
							continue;
						} else if(beforeChanged.get(j) == 0 && afterChanged.get(j) == 1) {
							/*CommonUtils.MyLog("Individual",
									"changedCarsLoadedCargo", whichCar.get(j),
									"车给", whichGene.get(j), "仓库送");*/
							// 满足下面的条件则被判定为当前车送这个点
							
							
				//上面判断已经完成		//if (whichGene.get(j) == theWarehouse) {
								/*CommonUtils.MyLog("Individual",
										"changedCarsLoadedCargo", whichCar.get(j),
										"车给", whichGene.get(j), "仓库送");
								theCar = whichCar.get(j);*/
								// 如果在循环里已经有其他车把这个点送完了，那么基因置0，略过下面的步骤，调到下一轮
								if (warehousesRest[theWarehouse] == 0) { 
									/*CommonUtils.MyLog("Individual",
											"changedCarsLoadedCargo",
											whichGene.get(j), "仓库已经被其他车送完了");*/
									gene[theCar][theWarehouse] = 0;
									//FileWriterTools.WriteBefore("whichCar loop warehousesrest = 0" + j);
									//FileWriterTools.WriteAfter("whichCar loop warehousesrest = 0" + j);
									continue;
								}
								// 这辆车已经满了，也置0
								if (carRestVolumn[theCar] == 0) {
									/* CommonUtils.MyLog("Individual",
								"changedCarsLoadedCargo",
											whichCar.get(j), "车已经满了");*/
									gene[theCar][theWarehouse] = 0;									
									//FileWriterTools.WriteBefore("whichCar loop car full" + j);
									//FileWriterTools.WriteAfter("whichCar loop car full" + j);
									continue;
								}
								//if (loadedCargo[theCar][theWarehouse] ==0)
							/*	CommonUtils.MyLog("Individual",
										"changedCarsLoadedCargo", whichCar.get(j),
									   "车剩余容量", carRestVolumn[theCar], "仓库",
										theWarehouse, "剩余需求量",
										warehousesRest[theWarehouse]);*/
								//FileWriterTools.WriteBefore("whichCar loop loading" + j);
								//FileWriterTools.WriteAfter("whichCar loop loading" + j);
								// 车不满，且需求点不空，开始装
								if (carRestVolumn[theCar] > warehousesRest[theWarehouse]) {
									loadedCargo[theCar][theWarehouse] = warehousesRest[theWarehouse];
									carRestVolumn[theCar] = carRestVolumn[theCar] - warehousesRest[theWarehouse];
									warehousesRest[theWarehouse] = 0;
				
									
								} else {
									loadedCargo[theCar][theWarehouse] = carRestVolumn[theCar];
									warehousesRest[theWarehouse] = warehousesRest[theWarehouse] - carRestVolumn[theCar];									
									carRestVolumn[theCar] = 0;
								}
							}
						//}
					}

				}

			}
//			if(lastChanged.getChangedType() == 0){
//				FileWriterTools.WriteAfter("after");
//				for(int i = 0;i<whichGene.size();i++){
//					FileWriterTools.WriteAfter("gene:" + whichGene.get(i) + " before:" + beforeChanged.get(i)
//							 + " after:" + afterChanged.get(i));
//				}
//				for(int i = 0;i<Global.CAR_NUM;i++){
//					for(int j = 0;j<Global.WAREHOUSE_NUM;j++){
//						FileWriterTools.WriteAfter("wareRest:" + warehousesRest[j] + 
//								" carRest:" + carRestVolumn[i] + 
//								" loadedCargo:" + loadedCargo[i][j]);
//					}
//				}
//			}
	}
	
	//list中是否含有one
	private boolean ifHas(ArrayList<Integer> list,int one){
		for(int i = 0;i<list.size();i++){
			if(list.get(i) == one){
				return true;
			}
		}
		return false;
	}
	protected class LastChanged{
		//是哪种变换，0是交叉，1是变异
		protected int changedType;
		//变换的基因位于哪辆车
		protected ArrayList<Integer> whichCar = new ArrayList<Integer>();
		//变换的基因是哪个需求点
		protected ArrayList<Integer> whichWarehouse = new ArrayList<Integer>();
		//变化前的基因
		protected ArrayList<Integer> beforeChanged = new ArrayList<Integer>();
		//变化后的基因
		protected ArrayList<Integer> afterChanged = new ArrayList<Integer>();
		public void clear(){
			whichCar.clear();
			whichWarehouse.clear();
			beforeChanged.clear();
			afterChanged.clear();
		}
		public int getChangedType() {
			return changedType;
		}
		public void setChangedType(int changedType) {
			this.changedType = changedType;
		}
		public ArrayList<Integer> getWhichCar() {
			return whichCar;
		}
		public void addWhichCar(int whichCar) {
			this.whichCar.add(whichCar);
			//System.out.println(this.whichCar.size());
		}
		public ArrayList<Integer> getWhichWarehouse() {
			return whichWarehouse;
		}
		public void addWhichWarehouse(int whichWarehouse) {
			this.whichWarehouse.add(whichWarehouse);
			//System.out.println(this.whichWarehouse.size());
		}
		public ArrayList<Integer> getAfterChanged() {
			return afterChanged;
		}
		public void addAfterChanged(int afterChanged) {
			this.afterChanged.add(afterChanged);
			//System.out.println(this.afterChanged.size());
		}
		public ArrayList<Integer> getBeforeChanged() {
			return beforeChanged;
		}
		public void addBeforeChanged(int beforeChanged) {
			this.beforeChanged.add(beforeChanged);
			//System.out.println(this.beforeChanged.size());
		}
		public void setWhichCar(ArrayList<Integer> whichCar) {
			this.whichCar = whichCar;
		}
		public void setWhichWarehouse(ArrayList<Integer> whichWarehouse) {
			this.whichWarehouse = whichWarehouse;
		}
		public void setBeforeChanged(ArrayList<Integer> beforeChanged) {
			this.beforeChanged = beforeChanged;
		}
		public void setAfterChanged(ArrayList<Integer> afterChanged) {
			this.afterChanged = afterChanged;
		}
		
	}
	


	public int[][] getGene() {
		return gene;
	}

	public void setGene(int[][] gene) {
		this.gene = gene;
	}

	public float getFitness() {
		return fitness;
	}

	public void setFitness(float fitness) {
		this.fitness = fitness;
	}

	public void setOneGene(int i, int j, int value) {
		gene[i][j] = value;
	}
	public void setChangedType(int type){
		lastChanged.setChangedType(type);
	}
	public void addWhichCarChanged(int cars){
		lastChanged.addWhichCar(cars);
	}
	public void addWhichWarehChanged(int warehouses){
		lastChanged.addWhichWarehouse(warehouses);
	}
	public void addAfterChanged(int after){
		lastChanged.addAfterChanged(after);
	}
	public void addBeforeChanged(int before){
		lastChanged.addBeforeChanged(before);
	}
	public void lastChangedClear(){
		lastChanged.clear();
	}
	public float[][] getDistances() {
		return distances;
	}
	public void setDistances(float[][] distances) {
		this.distances = distances;
	}
	public ArrayList<Warehouse> getWarehouses() {
		return warehouses;
	}
	public void setWarehouses(ArrayList<Warehouse> warehouses) {
		this.warehouses = warehouses;
	}
	public Supplier getSupplier() {
		return supplier;
	}
	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}
	public Carrier[] getCars() {
		return cars;
	}
	public void setCars(Carrier[] cars) {
		this.cars = cars;
	}
	public float[] getCarRestVolumn() {
		return carRestVolumn;
	}
	public void setCarRestVolumn(float[] carRestVolumn) {
		this.carRestVolumn = carRestVolumn;
	}
	public float[] getWarehousesRest() {
		return warehousesRest;
	}
	public void setWarehousesRest(float[] warehousesRest) {
		this.warehousesRest = warehousesRest;
	}
	public int getRectCount() {
		return rectCount;
	}
	public void setRectCount(int rectCount) {
		this.rectCount = rectCount;
	}
	public HashMap<Integer, Integer> getCountToid() {
		return countToid;
	}
	public void setCountToid(HashMap<Integer, Integer> countToid) {
		this.countToid = countToid;
	}
	public HashMap<Integer, Integer> getIdTocount() {
		return idTocount;
	}
	public void setIdTocount(HashMap<Integer, Integer> idTocount) {
		this.idTocount = idTocount;
	}
	public float[][] getLoadedCargo() {
		return loadedCargo;
	}
	public void setLoadedCargo(float[][] loadedCargo) {
		this.loadedCargo = loadedCargo;
	}
	public void setWhichType(int type){
		lastChanged.setChangedType(type);
	}
	public int getWhichType(){
		return lastChanged.getChangedType();
	}
	public void setWhichCar(ArrayList<Integer> whichCar){
		lastChanged.setWhichCar(whichCar);
	}
	public ArrayList<Integer> getWhichCar(){
		return lastChanged.getWhichCar();
	}
	public void setWhichWare(ArrayList<Integer> whichWarehouses){
		lastChanged.setWhichWarehouse(whichWarehouses);
	}
	public ArrayList<Integer> getWhichWare(){
		return lastChanged.getWhichWarehouse();
	}
	public void setBeforeChanged(ArrayList<Integer> before){
		lastChanged.setBeforeChanged(before);
	}
	public ArrayList<Integer> getBefore(){
		return lastChanged.getBeforeChanged();
	}
	public void setAfterChanged(ArrayList<Integer> after){
		lastChanged.setAfterChanged(after);
	}
	public ArrayList<Integer> getAfter(){
		return lastChanged.getAfterChanged();
	}
}
